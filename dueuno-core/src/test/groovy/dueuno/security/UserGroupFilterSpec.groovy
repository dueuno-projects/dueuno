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

import dueuno.application.TConnectionSource
import dueuno.tenant.TTenant
import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class UserGroupFilterSpec extends Specification {

    @Shared
    @AutoCleanup
    HibernateDatastore datastore
    TransactionStatus transaction

    void setupSpec() {
        datastore = new HibernateDatastore([
            'dataSource.url': 'jdbc:h2:mem:userGroupFilter;DB_CLOSE_DELAY=-1',
            'dataSource.driverClassName': 'org.h2.Driver',
            'dataSource.username': 'sa',
            'dataSource.password': '',
            'dataSource.dbCreate': 'create-drop',
        ], TUser, TUserAuthenticationIdentity, TAuthenticationProvider, TTenant, TConnectionSource, TRoleGroup, TUserRoleGroup)
    }

    void setup() {
        transaction = datastore.transactionManager.getTransaction(new DefaultTransactionDefinition())
    }

    void cleanup() {
        datastore.transactionManager.rollback(transaction)
    }

    void 'filters users by membership in any selected group without duplicates'() {
        given:
        TTenant tenant = createTenant()
        TRoleGroup users = new TRoleGroup(tenant: tenant, name: 'USERS').save(failOnError: true)
        TRoleGroup editors = new TRoleGroup(tenant: tenant, name: 'EDITORS').save(failOnError: true)
        TRoleGroup reviewers = new TRoleGroup(tenant: tenant, name: 'REVIEWERS').save(failOnError: true)
        TUser alice = createUser(tenant, users, 'alice')
        TUser bob = createUser(tenant, users, 'bob')
        createUser(tenant, editors, 'carol')
        TUserRoleGroup.create(alice, editors)
        TUserRoleGroup.create(alice, reviewers)
        TUserRoleGroup.create(bob, reviewers)
        SecurityService service = Spy(SecurityService) {
            isSuperAdmin() >> true
        }

        expect:
        service.listAllUser([groups: [editors.id.toString()]], [sort: 'username'])*.username == ['alice']
        service.countAllUser(groups: [editors.id.toString()]) == 1
        service.listAllUser([groups: [editors.id, reviewers.id]], [sort: 'username'])*.username == ['alice', 'bob']
        service.countAllUser(groups: [editors.id, reviewers.id]) == 2
        service.listAllUser([groups: [reviewers.id]], [sort: 'username', max: 1, offset: 1])*.username == ['bob']
        service.countAllUser(groups: []) == 3
        service.countAllUser(groups: [Long.MAX_VALUE]) == 0
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
            mobileFontSize: 16,
            guiStyle: 'ROUNDED',
            animations: true,
        ).save(failOnError: true)
    }

}
