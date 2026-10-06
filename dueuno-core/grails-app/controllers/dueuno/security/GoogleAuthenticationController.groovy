/*
 * Copyright 2021 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
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
import dueuno.application.TAuthenticationProvider
import dueuno.security.TUser
import dueuno.tenant.TenantService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.context.SecurityContextRepository

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.Locale

/**
 * OAuth 2.0 / OpenID Connect login for Google.
 */
@Slf4j
@Secured(['permitAll'])
class GoogleAuthenticationController {

    AuthenticationProviderService authenticationProviderService
    AuthenticationUserProvisioningService authenticationUserProvisioningService
    TenantService tenantService
    SessionAuthenticationStrategy sessionAuthenticationStrategy
    SecurityContextRepository securityContextRepository

    private static final String STATE_SESSION_KEY = 'dueuno.google.state'
    private static final String NONCE_SESSION_KEY = 'dueuno.google.nonce'
    private static final String VERIFIER_SESSION_KEY = 'dueuno.google.verifier'

    def start() {
        TAuthenticationProvider provider = authenticationProviderService.getByProviderKey('google')
        if (!configured(provider)) {
            redirect controller: 'authentication', action: 'login'
            return
        }

        String state = UUID.randomUUID().toString()
        String nonce = UUID.randomUUID().toString()
        String verifier = randomToken() + randomToken()
        session[STATE_SESSION_KEY] = state
        session[NONCE_SESSION_KEY] = nonce
        session[VERIFIER_SESSION_KEY] = verifier

        String challenge = base64Url(MessageDigest.getInstance('SHA-256').digest(verifier.getBytes(StandardCharsets.US_ASCII)))
        String scopes = provider.scopes ?: 'openid email profile'
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
        String query = parameters.collect { String key, String value ->
            "${encode(key)}=${encode(value)}"
        }.join('&')
        redirect uri: "${provider.authorizationUri ?: 'https://accounts.google.com/o/oauth2/v2/auth'}?${query}"
    }

    def callback() {
        TAuthenticationProvider provider = authenticationProviderService.getByProviderKey('google')
        String expectedState = session[STATE_SESSION_KEY] as String
        String expectedNonce = session[NONCE_SESSION_KEY] as String
        String verifier = session[VERIFIER_SESSION_KEY] as String
        session.remove(STATE_SESSION_KEY)
        session.remove(NONCE_SESSION_KEY)
        session.remove(VERIFIER_SESSION_KEY)

        if (!configured(provider) || !params.code || !expectedState || !expectedNonce || !verifier ||
            !MessageDigest.isEqual(expectedState.getBytes(StandardCharsets.UTF_8), (params.state as String ?: '').getBytes(StandardCharsets.UTF_8))) {
            rejectLogin()
            return
        }

        try {
            Map token = requestTokens(provider, params.code as String, verifier)
            String idToken = token.id_token as String
            if (!idToken) throw new IllegalArgumentException('Google did not return an ID token')

            Map claims = verifyIdToken(provider, idToken)
            if (claims.nonce != expectedNonce || !isVerifiedEmail(claims.email_verified)) {
                throw new IllegalArgumentException('Google identity verification failed')
            }

            String email = claims.email as String
            if (!email) throw new IllegalArgumentException('Google did not return a verified email address')
            String host = request.getHeader('host')
            String tenantId = tenantService.getByHost(host)?.tenantId ?: tenantService.defaultTenantId
            UserDetails userDetails
            tenantService.withTenant(tenantId) {
                List<TUser> users = TUser.findAllByEmail(email)
                if (users.size() > 1) throw new UsernameNotFoundException('No unique application user matches this Google account')
                String username = users
                    ? users.first().username
                    : email
                if (!users && TUser.findByUsername(username)) {
                    throw new UsernameNotFoundException('A different application user already uses this Google email as username')
                }
                userDetails = authenticationUserProvisioningService.ensureUser(username, [
                    firstname: claims.given_name,
                    lastname: claims.family_name,
                    email: email,
                ])
            }
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
            log.warn('Google sign-in failed: {}', e.message)
            rejectLogin()
        }
    }

    private Map requestTokens(TAuthenticationProvider provider, String code, String verifier) {
        Map<String, String> values = [
            code         : code,
            client_id    : provider.clientId,
            client_secret: provider.clientSecret,
            redirect_uri : provider.redirectUri,
            grant_type   : 'authorization_code',
            code_verifier: verifier,
        ]
        String form = values.collect { String key, String value -> "${encode(key)}=${encode(value)}" }.join('&')
        HttpRequest tokenRequest = HttpRequest.newBuilder(URI.create(provider.tokenUri ?: 'https://oauth2.googleapis.com/token'))
            .timeout(Duration.ofSeconds(15))
            .header('Content-Type', 'application/x-www-form-urlencoded')
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build()
        HttpResponse<String> tokenResponse = httpClient().send(tokenRequest, HttpResponse.BodyHandlers.ofString())
        if (tokenResponse.statusCode() != 200) throw new IllegalArgumentException('Google token exchange failed')
        return JSON.parse(tokenResponse.body()) as Map
    }

    private Map verifyIdToken(TAuthenticationProvider provider, String idToken) {
        String[] parts = idToken.split('\\.')
        if (parts.length != 3) throw new IllegalArgumentException('Invalid Google ID token')
        Map header = JSON.parse(new String(Base64.urlDecoder.decode(parts[0]), StandardCharsets.UTF_8)) as Map
        Map claims = JSON.parse(new String(Base64.urlDecoder.decode(parts[1]), StandardCharsets.UTF_8)) as Map
        if (header.alg != 'RS256') throw new IllegalArgumentException('Unsupported Google ID token algorithm')

        Map key = googleKeys().find { Map candidate -> candidate.kid == header.kid } as Map
        if (!key) throw new IllegalArgumentException('Google signing key was not found')
        BigInteger modulus = new BigInteger(1, Base64.urlDecoder.decode(key.n as String))
        BigInteger exponent = new BigInteger(1, Base64.urlDecoder.decode(key.e as String))
        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance('RSA').generatePublic(new RSAPublicKeySpec(modulus, exponent))
        Signature signature = Signature.getInstance('SHA256withRSA')
        signature.initVerify(publicKey)
        signature.update("${parts[0]}.${parts[1]}".getBytes(StandardCharsets.US_ASCII))
        if (!signature.verify(Base64.urlDecoder.decode(parts[2]))) throw new IllegalArgumentException('Invalid Google ID token signature')

        List<String> issuers = [provider.issuerUri ?: 'https://accounts.google.com', 'accounts.google.com']
        if (!issuers.contains(claims.iss as String)) throw new IllegalArgumentException('Invalid Google ID token issuer')
        Object audience = claims.aud
        Boolean audienceMatches = audience instanceof Collection
            ? ((Collection) audience).contains(provider.clientId)
            : audience == provider.clientId
        if (!audienceMatches) throw new IllegalArgumentException('Invalid Google ID token audience')
        long now = Instant.now().epochSecond
        if (!claims.exp || ((Number) claims.exp).longValue() <= now) throw new IllegalArgumentException('Expired Google ID token')
        if (claims.iat && ((Number) claims.iat).longValue() > now + 60) throw new IllegalArgumentException('Invalid Google ID token issue time')
        if (audience instanceof Collection && ((Collection) audience).size() > 1 && claims.azp != provider.clientId) {
            throw new IllegalArgumentException('Invalid Google ID token authorized party')
        }
        return claims
    }

    private List<Map> googleKeys() {
        HttpRequest keysRequest = HttpRequest.newBuilder(URI.create('https://www.googleapis.com/oauth2/v3/certs'))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build()
        HttpResponse<String> keysResponse = httpClient().send(keysRequest, HttpResponse.BodyHandlers.ofString())
        if (keysResponse.statusCode() != 200) throw new IllegalArgumentException('Unable to load Google signing keys')
        Map response = JSON.parse(keysResponse.body()) as Map
        return response.keys as List<Map>
    }

    private boolean isVerifiedEmail(Object value) {
        return value == Boolean.TRUE || value?.toString()?.toLowerCase(Locale.ROOT) == 'true'
    }

    private void validateUser(UserDetails userDetails) {
        if (!userDetails.enabled) throw new DisabledException('User is disabled')
        if (!userDetails.accountNonLocked) throw new LockedException('User account is locked')
        if (!userDetails.accountNonExpired) throw new AccountExpiredException('User account has expired')
        if (!userDetails.credentialsNonExpired) throw new CredentialsExpiredException('User credentials have expired')
    }

    private boolean configured(TAuthenticationProvider provider) {
        return provider?.enabled && provider.clientId && provider.clientSecret && provider.redirectUri
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
