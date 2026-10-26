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
import dueuno.elements.components.Form
import dueuno.elements.components.Label
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentForm
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.DateField
import dueuno.elements.controls.Select
import dueuno.elements.controls.TextField
import dueuno.elements.style.TextDefault
import dueuno.elements.style.TextWrap
import dueuno.security.SecurityService
import grails.plugin.springsecurity.annotation.Secured

import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDate

/**
 * @author Gianluca Sartori
 */
@Secured(['ROLE_ADMIN'])
class AuditController implements ElementsController {

    private static final Integer INTEGRITY_PREVIEW_LIMIT = 100

    AuditService auditService
    SecurityService securityService
    TenantService tenantService

    def index() {
        def c = createContent(ContentTable)
        c.header.removeNextButton()
        c.table.actionbar.addAction(
            action: 'verifyIntegrity',
            text: 'audit.verifyAll',
            icon: 'fa-stamp',
        )
        c.table.with {
            filters.with {
                fold = false
                addField(
                    class: TextField,
                    id: 'tenantNoFilter',
                    value: tenantService.currentTenantId,
                    label: 'user.tenant',
                    icon: 'fa-house-user',
                    readonly: true,
                    cols: 2,
                )
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
                    optionsFromList: securityService.listAllUsername(tenant: tenantService.currentTenant.id),
                    renderTextPrefix: false,
                    search: true,
                    cols: 3,
                )
                addField(
                    class: Select,
                    id: 'operation',
                    optionsFromEnum: AuditOperation,
                    cols: 3,
                )
                addField(
                    class: TextField,
                    id: 'find',
                    label: TextDefault.FIND,
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

    def verifyIntegrity() {
        List<TAuditLog> invalidLogs = auditService.listInvalidLogIntegrity()
        if (invalidLogs) {
            def c = createContent(ContentTable)
            c.header.removeNextButton()
            c.header.addNextButton(
                action: 'downloadInvalidLogs',
                text: TextDefault.DOWNLOAD,
                icon: 'fa-download',
                submit: [],
                targetNew: true,
            )
            Form infoForm = c.addComponentBefore('table', Form, 'integrityInfo')
            infoForm.addField(
                class: Label,
                id: 'description',
                html: 'audit.verifyIntegrity.description',
                displayLabel: false,
            )
            c.table.with {
                title.text = 'audit.verifyIntegrity.preview'
                title.display = true
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
                body.eachRow { TableRow row, Map values ->
                    row.cells.operation.tag = true
                    row.cells.message.textWrap = TextWrap.SOFT_WRAP
                    row.cells.stateBefore.textWrap = TextWrap.SOFT_WRAP
                    row.cells.stateAfter.textWrap = TextWrap.SOFT_WRAP
                    row.cells.requestInfo.textWrap = TextWrap.SOFT_WRAP
                    row.cells.userAgent.textWrap = TextWrap.SOFT_WRAP
                }
                body = invalidLogs.take(INTEGRITY_PREVIEW_LIMIT)
            }
            display content: c, modal: true, large: true, closeButton: true

        } else {
            def c = createContent(ContentForm)
            c.header.removeNextButton()
            c.form.addField(
                class: Label,
                id: 'result',
                text: 'audit.integrity.valid',
                icon: 'fa-award',
                displayLabel: false,
            )
            display content: c, modal: true, closeButton: true
        }
    }

    def downloadInvalidLogs() {
        List<TAuditLog> invalidLogs = auditService.listInvalidLogIntegrity()
        List<String> columns = [
            'id',
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
            'digest',
        ]
        List<String> headers = columns.collect { String column ->
            message("audit.${column}")
        }

        OutputStreamWriter writer = new OutputStreamWriter(
            getDownloadOutputStream('audit-integrity-invalid.csv'),
            StandardCharsets.UTF_8,
        )
        response.setContentType('text/csv; charset=UTF-8')
        try {
            writer.write('\uFEFF')
            writer.write(toCsvRow(headers))
            for (TAuditLog obj in invalidLogs) {
                writer.write(toCsvRow([
                    obj.id,
                    obj.dateCreated,
                    obj.username,
                    obj.operation,
                    obj.message,
                    obj.objectName,
                    obj.stateBefore,
                    obj.stateAfter,
                    obj.ip,
                    obj.port,
                    obj.requestInfo,
                    obj.userAgent,
                    obj.digest,
                ]))
            }
            writer.flush()
        } finally {
            writer.close()
        }
    }

    private String toCsvRow(List values) {
        return values.collect { Object value -> toCsvValue(value) }.join(',') + '\r\n'
    }

    private String toCsvValue(Object value) {
        String text = value == null ? '' : value.toString()
        String leadingText = text.stripLeading()
        if (leadingText && leadingText.substring(0, 1) in ['=', '+', '-', '@']) {
            text = "'${text}"
        }
        return '"' + text.replace('"', '""') + '"'
    }

}
