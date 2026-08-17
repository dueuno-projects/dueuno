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

import dueuno.application.ConnectionSourceService
import dueuno.elements.ElementsController
import dueuno.elements.components.Label
import dueuno.elements.components.Separator
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.SelectX
import dueuno.elements.controls.TextField
import dueuno.elements.style.Color
import dueuno.elements.style.TextTransform
import dueuno.utils.EnvUtils
import grails.plugin.springsecurity.annotation.Secured

/**
 * @author Gianluca Sartori
 */
@Secured(['ROLE_SUPERADMIN'])
class TenantController implements ElementsController {

    TenantService tenantService
    ConnectionSourceService connectionSourceService

    def index() {
        def c = createContent(ContentTable)
        c.table.with {
            filters.with {
            }
            sortable = [
                tenantId: 'asc',
            ]
            columns = [
                'tenantId',
                'description',
                'host',
                'connectionSource.url',
            ]
            labels = [
                'connectionSource.url': 'tenant.connection.info',
            ]

            actions.tailAction.confirmMessage = 'tenant.confirm.delete'

            body.eachRow { TableRow row, Map values ->
                if (!values.deletable) {
                    row.actions.removeTailAction()
                }
            }
        }

        Map filters = c.table.filterParams
        c.table.body = tenantService.list(filters, c.table.fetchParams)
        c.table.paginate = tenantService.count(filters)

        display content: c
    }

    private buildForm(TTenant obj = null) {
        def c = obj
            ? createContent(ContentEdit)
            : createContent(ContentCreate)

        def isReadonly = obj?.tenantId == tenantService.defaultTenantId
        if (isReadonly) {
            c.header.removeNextButton()
        }

        c.form.with {
            validate = TTenant
            if (!obj && EnvUtils.isDevelopment()) {
                addField(
                    class: Label,
                    id: 'info',
                    html: 'tenant.info',
                    color: Color.WARNING_TEXT,
                    backgroundColor: Color.WARNING_BACKGROUND,
                    displayLabel: false,
                    tag: true,
                )
            }
            addField(
                class: TextField,
                id: 'tenantId',
                textTransform: TextTransform.UPPERCASE,
                invalidChars: ' ',
                cols: 6,
            )
            addField(
                class: TextField,
                id: 'host',
                cols: 6,
            )
            addField(
                class: TextField,
                id: 'description',
            )
            addField(
                class: Separator,
                id: 'connection.info',
                icon: 'fa-database',
            )
            addField(
                class: SelectX,
                id: 'connectionSource.driverClassName',
                optionsFromList: connectionSourceService.listAvailableDrivers(),
                textPrefix: 'jdbc',
                search: true,
                cols: 6,
            )
            addField(
                class: TextField,
                id: 'connectionSource.dialect',
                cols: 6,
            )
            addField(
                class: TextField,
                id: 'connectionSource.url',
            )
            addField(
                class: TextField,
                id: 'connectionSource.username',
                cols: 6,
            )
            addField(
                class: TextField,
                id: 'connectionSource.password',
                cols: 6,
            )
        }

        if (obj) {
            c.form.values = obj
            if (isReadonly) {
                c.form.readonly = true
            }
        }

        return c
    }

    def create() {
        def c = buildForm()
        display content: c, modal: true
    }

    def onCreate() {
        def obj
        try {
            obj = tenantService.create(params)
        } catch (Exception e) {
            display exception: e
            return
        }

        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        tenantService.provision(obj.tenantId)
        display action: 'index'
    }

    def edit() {
        def obj = tenantService.get(params.id)
        def c = buildForm(obj)
        display content: c, modal: true
    }

    def onEdit() {
        def obj = tenantService.update(params)
        if (obj.hasErrors()) {
            display errors: obj

        } else {
            display action: 'index'
        }
    }

    def onDelete() {
        try {
            tenantService.delete(params.id)
            display action: 'index'

        } catch (e) {
            display exception: e
        }
    }
}
