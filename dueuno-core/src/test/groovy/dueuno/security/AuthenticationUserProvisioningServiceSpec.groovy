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

import dueuno.application.TConnectionSource
import dueuno.tenant.TTenant
import dueuno.tenant.TenantService
import grails.testing.gorm.DataTest
import grails.util.GrailsWebMockUtil
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

import java.time.LocalDateTime

class AuthenticationUserProvisioningServiceSpec extends Specification implements DataTest {

    void setup() {
        GrailsWebMockUtil.bindMockWebRequest()
    }

    void cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    Class<?>[] getDomainClassesToMock() {
        [TUser, TUserAuthenticationIdentity, TAuthenticationProvider, TTenant, TConnectionSource, TRoleGroup] as Class<?>[]
    }

    void 'uses LDAP username when email is missing and email when available'() {
        given:
        TUser user = createTestUser()
        TenantService tenantService = Stub(TenantService) {
            getTenantIdByRequest(_) >> 'TEST'
        }
        SecurityService securityService = Mock(SecurityService)
        UserDetailsService userDetailsService = Mock(UserDetailsService)
        UserDetails expectedDetails = Stub(UserDetails)
        AuthenticationUserProvisioningService service = new AuthenticationUserProvisioningService(
            tenantService: tenantService,
            securityService: securityService,
            userDetailsService: userDetailsService,
        )

        when:
        UserDetails details = service.ensureLdapUser('Alice', [email: email])

        then:
        1 * securityService.getUserByTenantIdAndUsername('TEST', expectedUsername) >> user
        0 * securityService.createUser(_)
        1 * userDetailsService.loadUserByUsername(expectedUsername) >> expectedDetails
        details.is(expectedDetails)
        TUser.get(user.id).username == expectedUsername
        TUser.get(user.id).email == email
        TUser.get(user.id).ldapUid == 'Alice'
        TUser.get(user.id).ldapLastAccess != null

        where:
        email                 | expectedUsername
        null                  | 'Alice'
        ''                    | 'Alice'
        '   '                 | 'Alice'
        ' Alice@Example.org ' | 'alice@example.org'
    }

    void 'blocks OIDC provisioning when the identity has no email'() {
        given:
        AuthenticationUserProvisioningService service = new AuthenticationUserProvisioningService()

        when:
        TAuthenticationProvider provider = new TAuthenticationProvider(
            providerType: AuthenticationProviderType.GOOGLE,
        )
        service.ensureOidcUser(
            provider,
            'https://issuer.example.org',
            'subject-1',
            [preferred_username: 'alice@example.org', upn: 'alice@example.org'],
        )

        then:
        UsernameNotFoundException e = thrown()
        e.message == 'Authenticated identity has no email'
    }

    void 'updates last access when an OIDC identity signs in again'() {
        given:
        TUser user = createTestUser()
        TTenant tenant = user.tenant
        TAuthenticationProvider provider = new TAuthenticationProvider(
            tenant: tenant,
            providerType: AuthenticationProviderType.GOOGLE,
            sequence: 1,
            enabled: true,
        ).save(failOnError: true)
        LocalDateTime previousAccess = LocalDateTime.of(2000, 1, 1, 0, 0)
        TUserAuthenticationIdentity identity = new TUserAuthenticationIdentity(
            user: user,
            provider: provider,
            issuer: 'https://issuer.example.org',
            subject: 'subject-1',
            lastAccess: previousAccess,
        ).save(failOnError: true)
        UserDetailsService userDetailsService = Stub(UserDetailsService) {
            loadUserByUsername('alice@example.org') >> (null as UserDetails)
        }
        AuthenticationUserProvisioningService service = new AuthenticationUserProvisioningService(
            userDetailsService: userDetailsService,
        )

        when:
        service.ensureOidcUser(
            provider,
            identity.issuer,
            identity.subject,
            [email: user.username],
        )

        then:
        TUserAuthenticationIdentity.get(identity.id).lastAccess > previousAccess
    }

    private TUser createTestUser() {
        TConnectionSource connectionSource = new TConnectionSource(
            name: 'test',
            driverClassName: 'org.h2.Driver',
            dbCreate: 'create-drop',
            username: 'sa',
        ).save(failOnError: true)
        TTenant tenant = new TTenant(
            tenantId: 'TEST',
            host: 'test.example.org',
            deletable: true,
            connectionSource: connectionSource,
        ).save(failOnError: true)
        TRoleGroup group = new TRoleGroup(tenant: tenant, name: 'USERS').save(failOnError: true)
        TUser user = new TUser(
            tenant: tenant,
            username: 'alice@example.org',
            password: 'password',
            enabled: true,
            accountExpired: false,
            accountLocked: false,
            passwordExpired: false,
            deletable: true,
            defaultGroup: group,
            sessionDuration: 60,
            rememberMeDuration: 60,
            prefixedUnit: false,
            symbolicCurrency: false,
            symbolicQuantity: false,
            invertedMonth: false,
            twelveHours: false,
            firstDaySunday: false,
            fontSize: 14,
            mobileFontSize: 16,
            guiStyle: 'ROUNDED',
            animations: true,
        ).save(failOnError: true)
        return user
    }
}
