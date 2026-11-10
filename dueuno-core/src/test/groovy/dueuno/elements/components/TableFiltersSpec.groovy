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

import dueuno.elements.controls.TextField
import grails.core.DefaultGrailsApplication
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import org.grails.core.support.GrailsApplicationDiscoveryStrategy
import org.springframework.context.support.GenericApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

class TableFiltersSpec extends Specification {

    GenericApplicationContext applicationContext

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

    void 'resetPagination preserves filters while restoring the first page and default page size'() {
        given:
        Table table = new Table(id: 'table')
        table.filters.addField(class: TextField, id: 'name')
        table.filters.setValue('name', 'Alice')
        table.pagination.actionSession[table.id + 'RequestedOffset'] = 40
        table.pagination.setOffset(40)
        table.pagination.setMax(50)

        when:
        table.resetPagination()

        then:
        table.pagination.offset == 0
        table.fetchParams.offset == 0
        table.pagination.actionSession[table.id + 'RequestedOffset'] == 0
        table.pagination.max == 20
        table.filters.getValue('name') == 'Alice'
        table.filters.actionSession['name'] == 'Alice'
        table.pagination.goNext.params._21TableOffset == 20
    }

    void 'resetPagination preserves explicit pagination requests'() {
        given:
        Table table = new Table(id: 'table')
        table.requestParams._21Table = table.id
        table.requestParams._21TableOffset = 40
        table.pagination.setOffset(40)

        when:
        table.resetPagination()

        then:
        table.pagination.offset == 40
        table.fetchParams.offset == 40
    }

    void 'resetFilters clears saved filters without resetting pagination'() {
        given:
        Table table = new Table(id: 'table')
        table.filters.addField(class: TextField, id: 'name')
        table.filters.setValue('name', 'Alice')
        table.pagination.actionSession[table.id + 'RequestedOffset'] = 40
        table.pagination.setOffset(40)

        when:
        table.resetFilters()

        then:
        !table.filters.actionSession.containsKey('name')
        table.pagination.offset == 40
        table.fetchParams.offset == 40
    }

    void 'resetOffset synchronizes pagination without changing the page size'() {
        given:
        Table table = new Table(id: 'table')
        table.pagination.actionSession[table.id + 'RequestedOffset'] = 100
        table.pagination.setOffset(100)
        table.pagination.setMax(50)

        when:
        table.pagination.resetOffset()

        then:
        table.pagination.offset == 0
        table.fetchParams.offset == 0
        table.requestParams.offset == 0
        table.pagination.max == 50
        table.pagination.goPrev.params._21TableOffset == -50
        table.pagination.goNext.params._21TableOffset == 50
    }

    void 'resetMax restores default page size and preserves offset and filters'() {
        given:
        Table table = new Table(id: 'table')
        table.filters.addField(class: TextField, id: 'name')
        table.filters.setValue('name', 'Alice')
        table.pagination.actionSession[table.id + 'RequestedOffset'] = 100
        table.pagination.actionSession[table.id + 'RequestedMax'] = 50
        table.pagination.setOffset(100)
        table.pagination.setMax(50)

        when:
        table.pagination.resetMax()

        then:
        table.pagination.max == 20
        table.fetchParams.max == 20
        table.requestParams.max == 20
        table.pagination.actionSession[table.id + 'RequestedMax'] == 20
        table.pagination.offset == 100
        table.filters.getValue('name') == 'Alice'
        table.pagination.goPrev.params._21TableOffset == 80
        table.pagination.goNext.params._21TableOffset == 120
    }

    void 'resetMax preserves an explicit page size request'() {
        given:
        Table table = new Table(id: 'table')
        table.requestParams._21Table = table.id
        table.requestParams._21TableMax = 50
        table.pagination.setMax(50)

        when:
        table.pagination.resetMax()

        then:
        table.pagination.max == 50
        table.fetchParams.max == 50
        table.requestParams._21TableMax == 50
    }

}
