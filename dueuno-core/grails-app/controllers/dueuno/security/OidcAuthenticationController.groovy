/*
 * Copyright 2021 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.security

import dueuno.application.AuthenticationProviderService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import org.springframework.security.authentication.*
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.context.SecurityContextRepository

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Duration
import java.time.Instant

/**
 * OpenID Connect login shared by all configured OIDC providers.
 */
@Slf4j
@Secured(['permitAll'])
class OidcAuthenticationController {

    AuthenticationProviderService authenticationProviderService
    AuthenticationUserProvisioningService authenticationUserProvisioningService
    SessionAuthenticationStrategy sessionAuthenticationStrategy
    SecurityContextRepository securityContextRepository

    private static final String FLOW_SESSION_KEY = 'dueuno.oidc.flows'

    def start() {
        AuthenticationProviderType providerType = AuthenticationProviderType.fromName(params.providerType as String)
        if (!providerType?.isOidcProvider()) {
            rejectLogin()
            return
        }
        String providerTypeName = providerType.name()
        TAuthenticationProvider provider = authenticationProviderService.getByProviderType(providerType)
        if (!configured(provider)) {
            rejectLogin()
            return
        }

        try {
            Map metadata = metadata(provider)
            String authorizationEndpoint = metadata.authorization_endpoint as String
            if (!authorizationEndpoint) throw new IllegalArgumentException('OIDC authorization endpoint is missing')

            String state = UUID.randomUUID().toString()
            String nonce = UUID.randomUUID().toString()
            String verifier = randomToken() + randomToken()
            Map flows = new LinkedHashMap((session[FLOW_SESSION_KEY] as Map) ?: [:])
            flows[state] = [providerType: providerTypeName, nonce: nonce, verifier: verifier]
            session[FLOW_SESSION_KEY] = flows

            String challenge = base64Url(MessageDigest.getInstance('SHA-256').digest(verifier.getBytes(StandardCharsets.US_ASCII)))
            String scopes = (provider.scopes ?: 'openid email profile').trim()
            if (!scopes.split('\\s+').contains('openid')) scopes = "openid ${scopes}".trim()
            Map<String, String> parameters = [
                client_id            : provider.clientId,
                redirect_uri         : provider.redirectUri,
                response_type        : 'code',
                scope                : scopes,
                state                : state,
                nonce                : nonce,
                code_challenge       : challenge,
                code_challenge_method: 'S256',
            ]
            String query = parameters.collect { String key, String value -> "${encode(key)}=${encode(value)}" }.join('&')
            String separator = authorizationEndpoint.contains('?') ? '&' : '?'
            redirect uri: "${authorizationEndpoint}${separator}${query}"
        } catch (Exception e) {
            log.warn('Unable to start OIDC sign-in for provider {}: {}', providerTypeName, e.message)
            rejectLogin()
        }
    }

    def callback() {
        String state = params.state as String
        Map flows = new LinkedHashMap((session[FLOW_SESSION_KEY] as Map) ?: [:])
        Map flow = state ? flows.remove(state) as Map : null
        session[FLOW_SESSION_KEY] = flows

        AuthenticationProviderType requestedProviderType = AuthenticationProviderType.fromName(params.providerType as String)
        if (!flow || (params.providerType && requestedProviderType?.name() != flow.providerType) || params.error) {
            rejectLogin()
            return
        }

        AuthenticationProviderType providerType = AuthenticationProviderType.fromName(flow.providerType as String)
        if (!providerType?.isOidcProvider()) {
            rejectLogin()
            return
        }
        String providerTypeName = providerType.name()
        TAuthenticationProvider provider = authenticationProviderService.getByProviderType(providerType)
        String code = params.code as String
        if (!configured(provider) || !code) {
            rejectLogin()
            return
        }

        try {
            Map metadata = metadata(provider)
            Map tokens = requestTokens(provider, metadata, code, flow.verifier as String)
            String idToken = tokens.id_token as String
            if (!idToken) throw new IllegalArgumentException('OIDC provider did not return an ID token')

            Map claims = verifyIdToken(provider, metadata, idToken)
            if (claims.nonce != flow.nonce) throw new IllegalArgumentException('OIDC nonce validation failed')
            claims = addUserInfoClaims(metadata, tokens, claims)

            String issuer = claims.iss as String
            String subject = claims.sub as String
            if (!issuer || !subject) throw new IllegalArgumentException('OIDC identity is missing issuer or subject')

            UserDetails userDetails = authenticationUserProvisioningService.ensureOidcUser(
                provider,
                issuer,
                subject,
                claims,
            )
            validateUser(userDetails)

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.authorities,
            )
            sessionAuthenticationStrategy.onAuthentication(authentication, request, response)
            def context = SecurityContextHolder.createEmptyContext()
            context.authentication = authentication
            SecurityContextHolder.context = context
            securityContextRepository.saveContext(context, request, response)
            redirect uri: '/authentication/afterLogin'
        } catch (Exception e) {
            log.warn('OIDC sign-in failed for provider {}: {}', providerTypeName, e.message)
            rejectLogin()
        }
    }

    private Map metadata(TAuthenticationProvider provider) {
        Map result
        if (provider.discoveryUri) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(provider.discoveryUri))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build()
            HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) throw new IllegalArgumentException('Unable to load OIDC discovery metadata')
            result = JSON.parse(response.body()) as Map
        } else {
            result = [:]
        }

        if (provider.issuerUri) result.issuer = provider.issuerUri
        if (provider.authorizationUri) result.authorization_endpoint = provider.authorizationUri
        if (provider.tokenUri) result.token_endpoint = provider.tokenUri
        if (provider.jwksUri) result.jwks_uri = provider.jwksUri
        if (provider.userInfoUri) result.userinfo_endpoint = provider.userInfoUri

        if (!result.issuer || !result.authorization_endpoint || !result.token_endpoint || !result.jwks_uri) {
            throw new IllegalArgumentException('OIDC issuer, authorization, token and JWKS endpoints are required')
        }
        return result
    }

    private Map requestTokens(TAuthenticationProvider provider, Map metadata, String code, String verifier) {
        Map<String, String> values = [
            code         : code,
            redirect_uri : provider.redirectUri,
            grant_type   : 'authorization_code',
            code_verifier: verifier,
        ]
        String authenticationMethod = provider.clientAuthenticationMethod ?: 'client_secret_basic'
        String authorizationHeader
        if (authenticationMethod == 'client_secret_basic') {
            String credentials = "${encode(provider.clientId)}:${encode(provider.clientSecret)}"
            authorizationHeader = "Basic ${Base64.encoder.encodeToString(credentials.getBytes(StandardCharsets.UTF_8))}"
        } else if (authenticationMethod == 'client_secret_post') {
            values.client_id = provider.clientId
            values.client_secret = provider.clientSecret
        } else {
            throw new IllegalArgumentException('Unsupported OIDC client authentication method')
        }
        String form = values.collect { String key, String value -> "${encode(key)}=${encode(value)}" }.join('&')
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(metadata.token_endpoint as String))
            .timeout(Duration.ofSeconds(15))
            .header('Content-Type', 'application/x-www-form-urlencoded')
            .POST(HttpRequest.BodyPublishers.ofString(form))
        if (authorizationHeader) requestBuilder.header('Authorization', authorizationHeader)
        HttpRequest request = requestBuilder.build()
        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) throw new IllegalArgumentException('OIDC authorization code exchange failed')
        return JSON.parse(response.body()) as Map
    }

    private Map verifyIdToken(TAuthenticationProvider provider, Map metadata, String idToken) {
        String[] parts = idToken.split('\\.')
        if (parts.length != 3) throw new IllegalArgumentException('Invalid OIDC ID token')
        Map header = JSON.parse(new String(Base64.urlDecoder.decode(parts[0]), StandardCharsets.UTF_8)) as Map
        Map claims = JSON.parse(new String(Base64.urlDecoder.decode(parts[1]), StandardCharsets.UTF_8)) as Map
        if (header.alg != 'RS256') throw new IllegalArgumentException('Unsupported OIDC ID token signing algorithm')

        List<Map> keys = signingKeys(metadata.jwks_uri as String)
        List<Map> candidates = keys.findAll { Map candidate -> !header.kid || candidate.kid == header.kid }
        if (candidates.size() != 1) throw new IllegalArgumentException('OIDC signing key was not found')
        Map key = candidates.first()
        BigInteger modulus = new BigInteger(1, Base64.urlDecoder.decode(key.n as String))
        BigInteger exponent = new BigInteger(1, Base64.urlDecoder.decode(key.e as String))
        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance('RSA').generatePublic(new RSAPublicKeySpec(modulus, exponent))
        Signature signature = Signature.getInstance('SHA256withRSA')
        signature.initVerify(publicKey)
        signature.update("${parts[0]}.${parts[1]}".getBytes(StandardCharsets.US_ASCII))
        if (!signature.verify(Base64.urlDecoder.decode(parts[2]))) throw new IllegalArgumentException('Invalid OIDC ID token signature')

        String expectedIssuer = metadata.issuer as String
        if (expectedIssuer.contains('{tenantid}')) {
            if (!claims.tid) throw new IllegalArgumentException('OIDC token is missing its tenant identifier')
            expectedIssuer = expectedIssuer.replace('{tenantid}', claims.tid as String)
        }
        if (claims.iss != expectedIssuer) throw new IllegalArgumentException('Invalid OIDC ID token issuer')

        Object audience = claims.aud
        boolean audienceMatches = audience instanceof Collection
            ? ((Collection) audience).contains(provider.clientId)
            : audience == provider.clientId
        if (!audienceMatches) throw new IllegalArgumentException('Invalid OIDC ID token audience')

        long now = Instant.now().epochSecond
        if (!(claims.exp instanceof Number) || ((Number) claims.exp).longValue() <= now - 60) {
            throw new IllegalArgumentException('Expired OIDC ID token')
        }
        if (!(claims.iat instanceof Number) || ((Number) claims.iat).longValue() > now + 60) {
            throw new IllegalArgumentException('Invalid OIDC ID token issue time')
        }
        if (claims.nbf instanceof Number && ((Number) claims.nbf).longValue() > now + 60) {
            throw new IllegalArgumentException('OIDC ID token is not yet valid')
        }
        if (audience instanceof Collection && ((Collection) audience).size() > 1 && claims.azp != provider.clientId) {
            throw new IllegalArgumentException('Invalid OIDC ID token authorized party')
        }
        if (!claims.sub) throw new IllegalArgumentException('OIDC ID token is missing its subject')
        return claims
    }

    private List<Map> signingKeys(String jwksUri) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(jwksUri))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build()
        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) throw new IllegalArgumentException('Unable to load OIDC signing keys')
        Map result = JSON.parse(response.body()) as Map
        return result.keys as List<Map>
    }

    private Map addUserInfoClaims(Map metadata, Map tokens, Map claims) {
        String userInfoEndpoint = metadata.userinfo_endpoint as String
        String accessToken = tokens.access_token as String
        if (!userInfoEndpoint || !accessToken) return claims

        HttpRequest request = HttpRequest.newBuilder(URI.create(userInfoEndpoint))
            .timeout(Duration.ofSeconds(10))
            .header('Authorization', "Bearer ${accessToken}")
            .GET()
            .build()
        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            log.warn('OIDC user information endpoint returned status {}', response.statusCode())
            return claims
        }
        Map userInfo = JSON.parse(response.body()) as Map
        if (userInfo.sub != claims.sub) throw new IllegalArgumentException('OIDC user information subject does not match the ID token')

        Map merged = new LinkedHashMap(claims)
        for (String name in ['email', 'email_verified', 'given_name', 'family_name', 'preferred_username', 'upn']) {
            if (userInfo.containsKey(name)) merged[name] = userInfo[name]
        }
        return merged
    }

    private boolean configured(TAuthenticationProvider provider) {
        return provider?.enabled && provider.clientId && provider.clientSecret && provider.redirectUri &&
            (provider.discoveryUri || (provider.issuerUri && provider.authorizationUri && provider.tokenUri && provider.jwksUri))
    }

    private void validateUser(UserDetails userDetails) {
        if (!userDetails.enabled) throw new DisabledException('User is disabled')
        if (!userDetails.accountNonLocked) throw new LockedException('User account is locked')
        if (!userDetails.accountNonExpired) throw new AccountExpiredException('User account has expired')
        if (!userDetails.credentialsNonExpired) throw new CredentialsExpiredException('User credentials have expired')
    }

    private String randomToken() {
        byte[] bytes = new byte[32]
        new java.security.SecureRandom().nextBytes(bytes)
        return base64Url(bytes)
    }

    private String base64Url(byte[] value) {
        return Base64.urlEncoder.withoutPadding().encodeToString(value)
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8)
    }

    private HttpClient httpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
    }

    private void rejectLogin() {
        redirect controller: 'authentication', action: 'login', params: [login_error: '1']
    }
}
