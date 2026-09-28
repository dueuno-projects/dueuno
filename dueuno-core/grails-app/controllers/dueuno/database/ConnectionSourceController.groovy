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
package dueuno.database

import dueuno.application.ApplicationService
import dueuno.application.ConnectionSourceService
import dueuno.application.TConnectionSource
import dueuno.elements.ElementsController
import dueuno.elements.components.Label
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.Checkbox
import dueuno.elements.controls.Select
import dueuno.elements.controls.TextField
import dueuno.elements.style.Color
import grails.plugin.springsecurity.annotation.Secured

import java.sql.Driver

/**
 * @author Gianluca Sartori
 */
@Secured(['ROLE_DEVELOPER'])
class ConnectionSourceController implements ElementsController {

    ApplicationService applicationService
    ConnectionSourceService connectionSourceService

    def index() {
        applicationService.registerPrettyPrinter(Driver, '${it.class.name}')

        def c = createContent(ContentTable)
        c.table.with {
            columns = [
                'name',
                'embedded',
                'tenant',
                'readOnly',
                'dbCreate',
                'driverClassName',
                'url',
            ]

            body.eachRow { TableRow row, Map values ->
                if (values.embedded || values.tenant) {
                    row.actions.removeTailAction()
                }
            }
        }

        c.table.body = connectionSourceService.list()

        display content: c
    }

    private buildForm(TConnectionSource obj = null) {
        def c = obj
            ? createContent(ContentEdit)
            : createContent(ContentCreate)

        def isReadonly = obj?.embedded || obj?.tenant

        if (isReadonly) {
            c.header.removeNextButton()
        }

        c.form.with {
            validate = TConnectionSource
            readonly = isReadonly

            if (obj && !isReadonly) {
                addField(
                    class: Label,
                    id: 'info',
                    textColor: Color.WARNING_TEXT,
                    backgroundColor: Color.WARNING_BACKGROUND,
                    text: 'connectionSource.edit.info',
                    displayLabel: false,
                    cols: 12,
                )
            }
            addField(
                class: TextField,
                id: 'name',
                readonly: obj,
                cols: 12,
            )
            addField(
                class: Select,
                id: 'driverClassName',
                optionsFromList: connectionSourceService.listAvailableDrivers(),
                textPrefix: 'jdbc',
                search: true,
                cols: 6,
            )
            addField(
                class: TextField,
                id: 'dialect',
                cols: 6,
            )
            addField(
                class: Select,
                id: 'dbCreate',
                optionsFromList: connectionSourceService.listAvailableSchemaGenerators(),
                renderTextPrefix: false,
                cols: 6,
            )
            addField(
                class: Checkbox,
                id: 'readOnly',
                cols: 6,
            )
            addField(
                class: TextField,
                id: 'url',
                cols: 12,
            )
            addField(
                class: TextField,
                id: 'username',
                cols: 6,
            )
            addField(
                class: TextField,
                id: 'password',
                cols: 6,
            )
        }

        if (obj) {
            c.form.values = obj
        }

        return c
    }

    def create() {
        def c = buildForm()
        display content: c, modal: true
    }

    def onCreate() {
        def obj = connectionSourceService.create(params)
        if (obj.hasErrors()) {
            display errors: obj

        } else {
            display action: 'index'
        }
    }

    def edit() {
        def obj = connectionSourceService.get(params.id)
        def c = buildForm(obj)
        display content: c, modal: true
    }

    def onEdit() {
        def obj = connectionSourceService.update(params)
        if (obj.hasErrors()) {
            display errors: obj

        } else {
            display action: 'index'
        }
    }

    def onDelete() {
        try {
            connectionSourceService.delete(params.id)
            display action: 'index'

        } catch (e) {
            display exception: e
        }
    }

    def h2Console() {
        redirect uri: '/h2-console'
    }
}
