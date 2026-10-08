/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package dueuno.security

import dueuno.application.AuthenticationProviderService
import dueuno.application.TConnectionSource
import dueuno.elements.core.Elements
import dueuno.tenant.TTenant
import grails.testing.gorm.DataTest
import spock.lang.Specification

class UserIdentityFetchSpec extends Specification implements DataTest {

    Class<?>[] getDomainClassesToMock() {
        [TUser, TUserAuthenticationIdentity, TAuthenticationProvider, TTenant, TConnectionSource, TRoleGroup] as Class<?>[]
    }

    void 'fetching identities does not break user pagination'() {
        given:
        TTenant tenant = createTenant()
        TRoleGroup group = new TRoleGroup(tenant: tenant, name: 'USERS').save(failOnError: true)
        TUser firstUser = createUser(tenant, group, 'alice')
        TUser secondUser = createUser(tenant, group, 'bob')
        TUser thirdUser = createUser(tenant, group, 'carol')
        createIdentity(firstUser, 'subject-1')
        createIdentity(firstUser, 'subject-2')
        createIdentity(secondUser, 'subject-3')

        when:
        Map fetch = [tenant: 'join', defaultGroup: 'join', authenticationIdentities: 'join']
        List<TUser> firstPage = TUser.where { tenant == tenant }
            .list(max: 1, offset: 0, sort: 'username', order: 'asc', fetch: fetch)
        List<TUser> secondPage = TUser.where { tenant == tenant }
            .list(max: 1, offset: 1, sort: 'username', order: 'asc', fetch: fetch)
        List<TUser> thirdPage = TUser.where { tenant == tenant }
            .list(max: 1, offset: 2, sort: 'username', order: 'asc', fetch: fetch)

        then:
        firstPage*.username == ['alice']
        firstPage.first().authenticationIdentities*.subject.sort() == ['subject-1', 'subject-2']
        Elements.toMap(firstPage.first()).authenticationIdentities*.subject.sort() == ['subject-1', 'subject-2']
        secondPage*.username == ['bob']
        secondPage.first().authenticationIdentities*.subject == ['subject-3']
        thirdPage*.username == ['carol']
        !thirdPage.first().authenticationIdentities
    }

    void 'deletes an authentication identity by id'() {
        given:
        TTenant tenant = createTenant()
        TRoleGroup group = new TRoleGroup(tenant: tenant, name: 'USERS').save(failOnError: true)
        TUser user = createUser(tenant, group, 'alice')
        TUserAuthenticationIdentity identity = new TUserAuthenticationIdentity(
            user: user,
            providerType: AuthenticationProviderType.GOOGLE,
            issuer: 'https://issuer.example.org',
            subject: 'subject-1',
        ).save(failOnError: true)
        AuthenticationIdentityService service = new AuthenticationIdentityService()

        when:
        service.delete(identity.id)

        then:
        TUserAuthenticationIdentity.get(identity.id) == null
    }

    void 'lists authentication identities for one user'() {
        given:
        TTenant tenant = createTenant()
        TRoleGroup group = new TRoleGroup(tenant: tenant, name: 'USERS').save(failOnError: true)
        TUser user = createUser(tenant, group, 'alice')
        TUser otherUser = createUser(tenant, group, 'bob')
        createIdentity(user, 'subject-1')
        createIdentity(user, 'subject-2')
        createIdentity(otherUser, 'subject-3')
        AuthenticationIdentityService service = new AuthenticationIdentityService()

        when:
        List<TUserAuthenticationIdentity> identities = service.listByUser(user.id)

        then:
        identities*.subject == ['subject-1', 'subject-2']
    }

    void 'creates and updates an authentication identity with one argument map'() {
        given:
        TTenant tenant = createTenant()
        TRoleGroup group = new TRoleGroup(tenant: tenant, name: 'USERS').save(failOnError: true)
        TUser user = createUser(tenant, group, 'alice')
        TUser updatedUser = createUser(tenant, group, 'bob')
        TAuthenticationProvider provider = new TAuthenticationProvider(
            tenant: tenant,
            providerType: AuthenticationProviderType.GOOGLE,
            sequence: 1,
            enabled: true,
        ).save(failOnError: true)
        SecurityService securityService = Stub(SecurityService) {
            isSuperAdmin() >> true
        }
        AuthenticationIdentityService service = new AuthenticationIdentityService(
            authenticationProviderService: new AuthenticationProviderService(),
            securityService: securityService,
        )

        when:
        TUserAuthenticationIdentity identity = service.create(
            providerId: provider.id,
            user: user.id,
            issuer: 'https://issuer.example.org',
            subject: 'subject-1',
        )

        then:
        identity.id
        service.listByProvider(provider.id)*.id == [identity.id]

        when:
        TUserAuthenticationIdentity updatedIdentity = service.update(
            id: identity.id,
            user: updatedUser.id,
            issuer: 'https://updated-issuer.example.org',
            subject: 'subject-2',
        )

        then:
        updatedIdentity.user.id == updatedUser.id
        updatedIdentity.issuer == 'https://updated-issuer.example.org'
        updatedIdentity.subject == 'subject-2'
    }

    private static TTenant createTenant() {
        TConnectionSource connectionSource = new TConnectionSource(
            name: 'test',
            driverClassName: 'org.h2.Driver',
            dbCreate: 'create-drop',
            username: 'sa',
        ).save(failOnError: true)

        return new TTenant(
            tenantId: 'TEST',
            host: 'test.example.org',
            deletable: true,
            connectionSource: connectionSource,
        ).save(failOnError: true)
    }

    private static TUser createUser(TTenant tenant, TRoleGroup group, String username) {
        return new TUser(
            tenant: tenant,
            username: username,
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
            guiStyle: 'ROUNDED',
            animations: true,
        ).save(failOnError: true)
    }

    private static void createIdentity(TUser user, String subject) {
        new TUserAuthenticationIdentity(
            user: user,
            providerType: AuthenticationProviderType.GOOGLE,
            issuer: 'https://issuer.example.org',
            subject: subject,
        ).save(failOnError: true)
    }

}
