/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package dueuno.elements

import dueuno.tenant.TenantService
import grails.gorm.multitenancy.Tenants
import grails.testing.web.taglib.TagLibUnitTest
import org.mockito.MockedStatic
import org.mockito.Mockito
import spock.lang.Specification
import spock.lang.Unroll

class TenantTagLibSpec extends Specification implements TagLibUnitTest<TenantTagLib> {

    @Unroll
    void 'renders effective tenant #tenantId when the request host belongs to test'() {
        given:
        request.serverName = 'test.localhost'
        tagLib.tenantService = Stub(TenantService) {
            getTenantIdByRequest(_) >> 'test'
        }
        MockedStatic<Tenants> tenants = Mockito.mockStatic(Tenants)
        tenants.when({ Tenants.currentId() }).thenReturn(tenantId)

        expect:
        applyTemplate('<tenant:current/>') == expected

        cleanup:
        tenants.close()

        where:
        tenantId                       | expected
        TenantService.defaultTenantId  | TenantService.defaultTenantId
        'test'                         | 'test'
        'other'                        | 'other'
        null                           | TenantService.defaultTenantId
    }
}
