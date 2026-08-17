/*
 * Copyright 2021 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.tenant

import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.DateField
import dueuno.elements.controls.Select
import dueuno.elements.controls.TextField
import dueuno.elements.style.TextDefault
import dueuno.elements.style.TextWrap
import dueuno.security.SecurityService
import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate

/**
 * @author Gianluca Sartori
 */
@Secured(['ROLE_ADMIN'])
class AuditController implements ElementsController {

    AuditService auditService
    SecurityService securityService

    def index() {
        def c = createContent(ContentTable)
        c.header.removeNextButton()
        c.table.with {
            filters.with {
                fold = false
                addField(
                    class: DateField,
                    id: 'dateFrom',
                    defaultValue: LocalDate.now(),
                    cols: 2,
                )
                addField(
                    class: DateField,
                    id: 'dateTo',
                    defaultValue: LocalDate.now(),
                    cols: 2,
                )
                addField(
                    class: Select,
                    id: 'username',
                    optionsFromList: securityService.listAllUsername(),
                    renderTextPrefix: false,
                    search: true,
                    cols: 2,
                )
                addField(
                    class: Select,
                    id: 'operation',
                    optionsFromEnum: AuditOperation,
                    cols: 2,
                )
                addField(
                    class: TextField,
                    id: 'find',
                    label: TextDefault.FIND,
                    cols: 4,
                )
            }
            sortable = [
                dateCreated: 'desc',
            ]
            columns = [
                'dateCreated',
                'username',
                'operation',
                'message',
                'objectName',
                'stateBefore',
                'stateAfter',
                'ip',
                'port',
                'requestInfo',
                'userAgent',
            ]
            actions.removeAllActions()
            actions.addDefaultAction(action: 'verify', icon: 'fa-bookmark', tooltip: 'audit.verify.tooltip')
            body.eachRow { TableRow row, Map values ->
                row.cells.operation.tag = true
                row.cells.message.textWrap = TextWrap.SOFT_WRAP
                row.cells.stateBefore.textWrap = TextWrap.SOFT_WRAP
                row.cells.stateAfter.textWrap = TextWrap.SOFT_WRAP
                row.cells.requestInfo.textWrap = TextWrap.SOFT_WRAP
                row.cells.userAgent.textWrap = TextWrap.SOFT_WRAP
            }
        }

        def filters = c.table.filterParams
        c.table.body = auditService.list(filters, c.table.fetchParams)
        c.table.paginate = auditService.count(filters)

        display content: c
    }

    def verify() {
        def obj = auditService.get(params.id)
        def verified = auditService.verifyLogIntegrity(obj)

        if (verified) {
            display message: 'audit.log.verified'

        } else {
            display errorMessage: 'audit.log.corrupted'

        }
    }

}
