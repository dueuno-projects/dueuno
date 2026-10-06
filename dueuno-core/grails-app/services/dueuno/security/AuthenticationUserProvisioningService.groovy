/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.security

import dueuno.elements.WebRequestAware
import dueuno.tenant.TenantService
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Loads or creates the local user associated with an authenticated identity.
 */
@CompileStatic
class AuthenticationUserProvisioningService implements WebRequestAware {

    TenantService tenantService
    SecurityService securityService
    UserDetailsService userDetailsService

    @Transactional
    @CompileDynamic
    UserDetails ensureUser(String username, Map profile = [:]) {
        if (!username) throw new UsernameNotFoundException('Authenticated identity has no username')

        String firstname = (profile.firstname as String)?.trim()
        String lastname = (profile.lastname as String)?.trim()
        if (!firstname) firstname = null
        if (!lastname) lastname = null

        String host = hasRequest() ? request.getHeader('host') : null
        String tenantId = tenantService.getByHost(host)?.tenantId ?: tenantService.defaultTenantId
        UserDetails details
        tenantService.withTenant(tenantId) {
            try {
                details = userDetailsService.loadUserByUsername(username)
            } catch (UsernameNotFoundException ignored) {
                securityService.createUser(
                    tenantId: tenantId,
                    username: username,
                    password: securityService.generatePassword(),
                    firstname: firstname,
                    lastname: lastname,
                    email: profile.email,
                    telephone: profile.telephone,
                    note: profile.note,
                    failOnError: true,
                )
                details = userDetailsService.loadUserByUsername(username)
            }
        }
        return details
    }

    @Transactional
    @CompileDynamic
    UserDetails ensureOidcUser(AuthenticationProviderType providerType, String issuer, String subject, Map profile) {
        if (!providerType?.isOidcProvider() || !issuer || !subject) {
            throw new UsernameNotFoundException('OIDC identity has no provider, issuer or subject')
        }

        String host = hasRequest() ? request.getHeader('host') : null
        String tenantId = tenantService.getByHost(host)?.tenantId ?: tenantService.defaultTenantId
        UserDetails details
        tenantService.withTenant(tenantId) {
            TUserAuthenticationIdentity identity = TUserAuthenticationIdentity.findByIssuerAndSubject(issuer, subject)
            if (identity) {
                details = userDetailsService.loadUserByUsername(identity.user.username)
                return
            }

            String email = firstEmailClaim(profile)
            String verifiedEmailClaim = (profile.email as String)?.trim()
            boolean verifiedEmail = isVerifiedEmail(profile.email_verified) && verifiedEmailClaim?.contains('@')
            String username
            TUser user
            if (verifiedEmail && profile.email) {
                List<TUser> users = TUser.findAllByEmailIlike(verifiedEmailClaim)
                if (users.size() > 1) {
                    throw new UsernameNotFoundException('No unique local user matches the verified OIDC email')
                }
                if (users) {
                    user = users.first()
                    username = user.username
                } else {
                    username = verifiedEmailClaim.toLowerCase(Locale.ROOT)
                    if (TUser.findByUsername(username)) {
                        throw new UsernameNotFoundException('A different local user already uses the OIDC email as username')
                    }
                }
            } else {
                String digest = HexFormat.of().formatHex(
                    MessageDigest.getInstance('SHA-256').digest("${issuer}|${subject}".getBytes(StandardCharsets.UTF_8)),
                )
                username = "oidc_${providerType.name().toLowerCase(Locale.ROOT)}_${digest}"
                if (TUser.findByUsername(username)) {
                    throw new UsernameNotFoundException('An OIDC username exists without a matching external identity')
                }
            }

            details = ensureUser(username, [
                firstname: profile.given_name,
                lastname : profile.family_name,
                email     : email,
            ])
            user = TUser.findByUsername(username)
            if (!user) throw new UsernameNotFoundException('Unable to load the provisioned OIDC user')

            new TUserAuthenticationIdentity(
                user: user,
                providerType: providerType,
                issuer: issuer,
                subject: subject,
            ).save(flush: true, failOnError: true)
        }
        return details
    }

    private String firstEmailClaim(Map claims) {
        for (String name in ['email', 'preferred_username', 'upn']) {
            String value = claims[name] as String
            if (value?.contains('@')) return value.trim()
        }
        return null
    }

    private boolean isVerifiedEmail(Object value) {
        return value == Boolean.TRUE || value?.toString()?.toLowerCase(Locale.ROOT) == 'true'
    }

}
