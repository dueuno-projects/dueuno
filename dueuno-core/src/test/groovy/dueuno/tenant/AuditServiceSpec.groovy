/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.tenant

import dueuno.audit.TAuditLog
import dueuno.security.CryptoService
import dueuno.security.SecurityService
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.grails.datastore.mapping.multitenancy.resolvers.FixedTenantResolver
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockServletContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification
import spock.lang.Unroll

class AuditServiceSpec extends Specification implements ServiceUnitTest<AuditService>, DataTest {

    Closure doWithConfig() {
        { config ->
            config.grails.gorm.multiTenancy.mode = 'DATABASE'
            config.grails.gorm.multiTenancy.tenantResolverClass = FixedTenantResolver
        }
    }

    Class<?>[] getDomainClassesToMock() {
        [TAuditLog] as Class<?>[]
    }

    void setup() {
        service.securityService = Stub(SecurityService) {
            getCurrentUsername() >> 'audit-user'
        }
        service.cryptoService = Stub(CryptoService) {
            getTenantAESKey() >> new byte[32]
        }
        bindRequest('192.0.2.1', 'Original browser')
    }

    void cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Unroll
    void 'verifies an unchanged record from IP #ip and browser #userAgent'() {
        given:
        service.log(AuditOperation.EDIT, 'Message', 'Example', 'Before', 'After')
        TAuditLog record = TAuditLog.first()
        bindRequest(ip, userAgent)

        expect:
        service.verifyLogIntegrity(record)

        where:
        ip            | userAgent
        '192.0.2.1'   | 'Original browser'
        '192.0.2.2'   | 'Original browser'
        '192.0.2.1'   | 'Another browser'
        '192.0.2.2'   | 'Another browser'
    }

    @Unroll
    void 'rejects a record with modified #field'() {
        given:
        service.log(AuditOperation.EDIT, 'Message', 'Example', 'Before', 'After')
        TAuditLog record = TAuditLog.first()
        record[field] = changedValue
        record.save(flush: true, failOnError: true)

        expect:
        !service.verifyLogIntegrity(record)

        where:
        field         | changedValue
        'ip'          | '192.0.2.2'
        'userAgent'   | 'Another browser'
        'message'     | 'Modified message'
        'objectName'  | 'Another object'
        'stateBefore' | 'Modified before'
        'stateAfter'  | 'Modified after'
        'username'    | 'another-user'
        'operation'   | AuditOperation.DELETE
        'digest'      | '0' * 64
    }

    void 'verifies a record with a missing user agent and optional values'() {
        given:
        bindRequest('192.0.2.1', null)
        service.log(AuditOperation.LOGIN, 'Login')
        TAuditLog record = TAuditLog.first()
        bindRequest('192.0.2.2', 'Another browser')

        expect:
        service.verifyLogIntegrity(record)
    }

    private void bindRequest(String ip, String userAgent) {
        MockServletContext servletContext = new MockServletContext()
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext)
        request.remoteAddr = ip
        if (userAgent != null) {
            request.addHeader('User-Agent', userAgent)
        }
        RequestContextHolder.setRequestAttributes(new GrailsWebRequest(
            request, new MockHttpServletResponse(), servletContext))
    }
}
