/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
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
package apptest.gormissue

import grails.gorm.multitenancy.CurrentTenantHolder
import grails.gorm.transactions.Transactional
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.multitenancy.exceptions.TenantException
import org.grails.datastore.mapping.transactions.TransactionCapableDatastore
import spock.lang.Specification

class GormTenantTransactionManagerIssueSpec extends Specification {

    void 'generated transaction manager lookup fails with tenants bound to different datastores'() {
        given:
        TransactionCapableDatastore defaultDatastore = Stub(TransactionCapableDatastore)
        Datastore tenantDatastore = Stub(Datastore)
        TransactionalServiceFixture service = new TransactionalServiceFixture()
        service.setTargetDatastore(defaultDatastore)

        when:
        CurrentTenantHolder.withTenant(defaultDatastore, 'test') {
            CurrentTenantHolder.withTenant(tenantDatastore, 'default') {
                service.update()
            }
        }

        then:
        TenantException exception = thrown()
        exception.message.startsWith('Cannot resolve a single current tenant: [')
        exception.message.contains('test')
        exception.message.contains('default')
        exception.message.endsWith('are bound on this thread for different datastores. Use get(Datastore) to select the tenant of the datastore being operated on.')
    }
}

@Transactional
class TransactionalServiceFixture {

    void update() {
        // The generated transaction manager lookup runs before this method body.
    }
}
