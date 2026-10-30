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
package dueuno.application

import dueuno.tenant.TTenant
import dueuno.tenant.TenantService
import grails.testing.gorm.DataTest
import org.springframework.mock.web.MockServletContext
import spock.lang.Specification

class ApplicationServiceTenantFlowSpec extends Specification implements DataTest {

    @Override
    Class<?>[] getDomainClassesToMock() {
        return [TTenant] as Class<?>[]
    }

    void 'provisions and updates every existing tenant through ApplicationService'() {
        given:
        RecordingApplicationService service = new RecordingApplicationService(
            servletContext: new MockServletContext(),
        )
        StubTenantService tenantService = new StubTenantService([
            new TTenant(tenantId: 'tenant-one'),
            new TTenant(tenantId: 'tenant-two'),
        ])
        tenantService.applicationService = service

        when:
        tenantService.provisionAllTenants()
        tenantService.updateAllTenants()

        then:
        service.provisionedTenants == ['tenant-one', 'tenant-two']
        service.updatedTenants == ['tenant-one', 'tenant-two']
    }

    void 'executes a tenant boot closure inside the selected tenant scope'() {
        given:
        List<String> calls = []
        RecordingTenantService tenantService = new RecordingTenantService(calls)
        ApplicationService service = new ApplicationService(
            tenantService: tenantService,
            servletContext: new MockServletContext(),
        )
        service.registerBootEvent('beforeTenantInit') { String tenantId ->
            calls << "closure:${tenantId}"
        }

        when:
        service.executeBootEvents('tenant-one', 'beforeTenantInit')

        then:
        calls == ['enter:tenant-one', 'closure:tenant-one', 'exit:tenant-one']
    }

    void 'executes no-argument boot closures without tenant arguments'() {
        given:
        List<String> calls = []
        RecordingTenantService tenantService = new RecordingTenantService(calls)
        ApplicationService service = new ApplicationService(
            tenantService: tenantService,
            servletContext: new MockServletContext(),
        )
        service.registerBootEvent('beforeInit') {
            calls << 'closure'
        }

        when:
        service.executeBootEvents('default', 'beforeInit')

        then:
        calls == ['enter:default', 'closure', 'exit:default']
    }

    private static class RecordingApplicationService extends ApplicationService {
        List<String> provisionedTenants = []
        List<String> updatedTenants = []

        @Override
        void executeOnPluginTenantInstall(String tenantId) {
            provisionedTenants << tenantId
        }

        @Override
        void executeOnUpdate(String tenantId) {
            updatedTenants << tenantId
        }
    }

    private static class StubTenantService extends TenantService {
        List<TTenant> tenants

        StubTenantService(List<TTenant> tenants) {
            this.tenants = tenants
        }

        @Override
        List<TTenant> list(Map filters = [:], Map fetchParams = [:]) {
            return tenants
        }

        @Override
        void withTenant(String tenantId, Closure closure) {
            closure.call(tenantId)
        }
    }

    private static class RecordingTenantService extends TenantService {
        List<String> calls

        RecordingTenantService(List<String> calls) {
            this.calls = calls
        }

        @Override
        void withTenant(String tenantId, Closure closure) {
            calls << "enter:${tenantId}"
            closure.call(tenantId)
            calls << "exit:${tenantId}"
        }
    }
}
