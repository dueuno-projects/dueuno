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
package apptest

import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.*
import dueuno.elements.style.TextDefault
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class PublicPageController implements ElementsController {

    PersonService personService

    def index() {

        def c = createContent(ContentTable)
        c.header.addNextButton(
            action: 'create',
            text: TextDefault.NEW,
            icon: 'fa-plus',
        )

        c.table.with {
            filters.with {
                /*addField(
                        class: Select,
                        id: 'user1',
                        optionsFromRecordset: TPerson.list(),
                        keys: ['username'],
                )*/
                addField(
                    class: TextField,
                    id: 'name',
                )
                addField(
                    class: TextField,
                    id: 'textfield1',
                )
                addField(
                    class: TextField,
                    id: 'textfield2',
                )
            }

            groupActions.addAction(action: 'groupAction1')
            groupActions.addAction(action: 'groupAction2')
            groupActions.addAction(action: 'groupAction3')

            actions.addAction(action: 'action1.with.a.very.very.long.name')
            actions.addAction(action: 'action2')
            actions.addAction(controller: 'controller3')

            sortable = [
                name   : 'asc',
                address: 'desc',
            ]
            columns = [
                'company',
                'name',
                'picture',
                'address',
                'postcode',
                'salary',
                'distanceKm',
                'dateStart',
                'dateEnd',
                'active',
            ]

            max = 5

            actions.unsetTailAction()

            body.eachRow { TableRow row, Map values ->
                values.company = "${values.company.name}"
                if (values.picture) row.cells.picture.icon = 'fa-file'
                if (values.name == 'aaa') {
                    if (row.actions.hasActions()) row.actions.addSeparator()
                    row.actions.addAction(controller: 'myController', action: 'rowAction3', params: [x: 1, y: 2])
                }
            }
        }

        // QUERY
        //
        def query = TPerson.where {} // def user1 = user1 }

        def filters = c.table.filterParams
        //if (filters.user1) query = query.where {user1.username == filters.user1}
        if (filters.name) query = query.where { name =~ "%${filters.textfield}%" }

        // VALUES
        //
        c.table.body = query.list(params)
        c.table.paginate = query.count()

        println params
        display content: c
    }

    private buildForm(TPerson obj) {
        def c = obj
            ? createContent(ContentEdit)
            : createContent(ContentCreate)
        c.form.with {
            validate = TPerson
            addField(
                class: Select,
                id: 'company',
                optionsFromRecordset: TCompany.list(),
            )
            addField(
                class: TextField,
                id: 'name',
            )
            addField(
                class: TextField,
                id: 'address',
                help: 'Runtime help message',
                label: 'Indirizzo (runtime label)',
            )
            addField(
                class: NumberField,
                id: 'postcode',
            )
            addField(
                class: MoneyField,
                id: 'salary',
            )
            addField(
                class: QuantityField,
                id: 'distanceKm',
            )
            addField(
                class: DateField,
                id: 'dateStart',
            )
            addField(
                class: DateField,
                id: 'dateEnd',
            )
            addField(
                class: Checkbox,
                id: 'active',
            )
            addField(
                class: Upload,
                id: 'picture',
            )
        }

        if (obj) {
            c.form.values = obj
        }

        return c
    }

    def create() {
        def c = buildForm()
        display content: c, modal: true, focus: 'address'
    }

    def edit() {
        def obj = personService.get(params.id)
        def c = buildForm(obj)
        display content: c, modal: true, focus: 'address'
    }

    def onCreate() {
        def obj = personService.create(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        display action: 'index'
    }

    def onEdit() {
        def obj = personService.update(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        display action: 'index'
    }

    def onDelete() {
        try {
            personService.delete(params.id)
            display action: 'index'

        } catch (e) {
            e.printStackTrace()
            display exception: e
        }
    }

}
