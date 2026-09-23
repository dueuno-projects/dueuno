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
package dueuno.elements.components

import grails.core.DefaultGrailsApplication
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import org.grails.core.support.GrailsApplicationDiscoveryStrategy
import org.springframework.context.support.GenericApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

class TableRowSpec extends Specification {

    GenericApplicationContext applicationContext

    static class Company {
        Long id
    }

    def setup() {
        def webRequest = GrailsWebMockUtil.bindMockWebRequest()
        webRequest.controllerName = 'sandbox'
        webRequest.actionName = 'index'
        applicationContext = new GenericApplicationContext()
        applicationContext.refresh()
        DefaultGrailsApplication application = new DefaultGrailsApplication()
        application.mainContext = applicationContext
        Holders.addApplicationDiscoveryStrategy([
            findGrailsApplication: { application },
            findApplicationContext: { applicationContext },
        ] as GrailsApplicationDiscoveryStrategy)
    }

    def cleanup() {
        RequestContextHolder.resetRequestAttributes()
        Holders.clear()
        applicationContext.close()
    }

    void 'resolves nested keys before eachRow independently of key order'() {
        given:
        Table table = new Table(id: 'table')
        table.keys = keyColumns
        table.columns = ['name']
        List observed = []
        table.body.eachRow { TableRow row, Map values ->
            observed.add(values.'company.id')
        }

        when:
        table.body.setRows([
            [id: 1L, name: 'First', company: [id: 21L]],
            [id: 2L, name: 'Second', company: new Company(id: 42L)],
        ] as Collection)

        then:
        observed == [21L, 42L]
        table.body.rows.collect { it.getKeys() } == [
            [id: 1L, 'company.id': 21L, personId: 1L],
            [id: 2L, 'company.id': 42L, personId: 2L],
        ]

        where:
        keyColumns << [
            ['company.id', 'personId', 'id'],
            ['id', 'personId', 'company.id'],
        ]
    }

    void 'preserves explicit nested keys and never replaces missing nested values with the row id'() {
        given:
        Table table = new Table(id: 'table')
        table.keys = ['id', 'company.id', 'personId']
        List observed = []
        table.body.eachRow { TableRow row, Map values ->
            observed.add(values.'company.id')
        }

        when:
        table.body.setRows([
            [id: 1L, company: [id: 21L], 'company.id': 99L],
            [id: 2L, company: null],
            [id: 3L, company: [id: null]],
            [id: 4L],
            [id: 5L, company: [id: 0L]],
            [id: 6L, company: [id: 21L], 'company.id': null],
        ] as Collection)

        then:
        observed == [99L, null, null, null, 0L, null]
        table.body.rows.collect { it.values.personId } == [1L, 2L, 3L, 4L, 5L, 6L]
    }
}
