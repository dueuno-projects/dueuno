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

import java.time.LocalDateTime

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
    UserDetails ensureLdapUser(String ldapUid, Map profile = [:]) {
        if (!ldapUid) {
            throw new UsernameNotFoundException('Authenticated identity has no username')
        }

        String email = profile.email as String
        String username = email?.trim()?.toLowerCase(Locale.ROOT)
        if (!username) {
            throw new UsernameNotFoundException('Authenticated identity has no email')
        }
        String firstname = (profile.firstname as String)?.trim()
        String lastname = (profile.lastname as String)?.trim()
        if (!firstname) firstname = null
        if (!lastname) lastname = null

        String host = hasRequest() ? request.getHeader('host') : null
        String tenantId = tenantService.getByHost(host)?.tenantId ?: tenantService.defaultTenantId
        UserDetails details
        tenantService.withTenant(tenantId) {
            TUser user = TUser.findByUsername(username)
            LocalDateTime lastAccess = LocalDateTime.now()
            if (!user) {
                user = securityService.createUser(
                    tenantId: tenantId,
                    username: username,
                    password: securityService.generatePassword(),
                    firstname: firstname,
                    lastname: lastname,
                    email: email,
                    telephone: profile.telephone,
                    note: profile.note,
                    failOnError: true,
                )
            }

            user.username = username
            user.email = email
            user.ldapUid = ldapUid
            user.ldapLastAccess = lastAccess
            user.save(flush: true, failOnError: true)
            details = userDetailsService.loadUserByUsername(username)
        }

        return details
    }

    @Transactional
    @CompileDynamic
    UserDetails ensureOidcUser(TAuthenticationProvider provider, String issuer, String subject, Map profile) {
        if (!provider?.providerType?.isOidcProvider() || !issuer || !subject) {
            throw new UsernameNotFoundException('OIDC identity has no provider, issuer or subject')
        }

        String email = profile.email as String
        String username = email?.trim()?.toLowerCase(Locale.ROOT)
        if (!username) {
            throw new UsernameNotFoundException('Authenticated identity has no email')
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

            details = loadOrCreateUser(
                username,
                [
                    firstname: profile.given_name,
                    lastname : profile.family_name,
                    email    : email,
                ],
            )

            TUser user = TUser.findByUsername(username)
            if (!user) {
                throw new UsernameNotFoundException('Unable to load the provisioned OIDC user')
            }

            new TUserAuthenticationIdentity(
                user: user,
                provider: provider,
                issuer: issuer,
                subject: subject,
            ).save(flush: true, failOnError: true)
        }

        return details
    }

    @CompileDynamic
    private UserDetails loadOrCreateUser(String username, Map profile) {
        if (!username) {
            throw new UsernameNotFoundException('Authenticated identity has no username')
        }

        String firstname = (profile.firstname as String)?.trim()
        String lastname = (profile.lastname as String)?.trim()
        if (!firstname) {
            firstname = null
        }
        if (!lastname) {
            lastname = null
        }

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

}
