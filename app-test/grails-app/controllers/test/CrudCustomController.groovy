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
package test

import dueuno.elements.components.Form
import dueuno.elements.components.Header
import dueuno.elements.components.Table
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.controls.*
import dueuno.elements.ElementsController
import dueuno.elements.style.TextAlign
import dueuno.elements.style.TextDefault

class CrudCustomController implements ElementsController {

    PersonService personService
    CompanyService companyService

    def index() {

        def c = createContent()
        c.header.addNextButton(
                action: 'create',
                text: TextDefault.NEW,
                icon: 'fa-plus',
        )

        def form1 = c.addComponent(Form, 'form1')
        form1.with {
            sticky = true
            addField(
                    class: TextField,
                    id: 'test1',
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'test2',
                    optionsFromList: ['PIPPO', 'PLUTO', 'PAPERINO'],
                    cols: 6,
            )
        }

//        def form2 = c.addComponent(Form, 'form2')
//        form2.with {
//            sticky = true
//            addField(
//                    class: TextField,
//                    id: 'test1',
//                    cols: 6,
//            )
//            addField(
//                    class: Select,
//                    id: 'test2',
//                    optionsFromList: ['PIPPO', 'PLUTO', 'PAPERINO'],
//                    cols: 6,
//            )
//        }
//
//        def form3 = c.addComponent(Form, 'form3')
//        form3.with {
//            sticky = true
//            addField(
//                    class: TextField,
//                    id: 'test1',
//                    cols: 6,
//            )
//            addField(
//                    class: Select,
//                    id: 'test2',
//                    optionsFromList: ['PIPPO', 'PLUTO', 'PAPERINO'],
//                    cols: 6,
//            )
//        }
//
        def table = c.addComponent(Table)
        table.with {
            sticky = true
            filters.with {
                addField(
                        class: DateField,
                        id: 'dateFrom',
                        cols: 3,
                )
                addField(
                        class: DateField,
                        id: 'dateTo',
                        cols: 3,
                )
            }

            groupActions.addAction(action: 'groupAction1')
            groupActions.addAction(action: 'groupAction2')
            groupActions.addAction(action: 'groupAction3')

            actions.addAction(action: 'action1.with.a.very.very.long.name')
            actions.addAction(action: 'action2')
            actions.addAction(controller: 'controller3')

            columns = [
                    'dateCreated',
                    'company',
                    'name',
                    'picture',
                    'address',
                    'postcode',
            ]

            max = 2

            actions.unsetTailAction()

            hasHeader = false

            body.eachRow { TableRow row, Map values ->
                row.cells['postcode'].textAlign = TextAlign.END
                values.company = "${values.company.name}"
                if (values.picture) row.cells.picture.icon = 'fa-file'
                if (values.name == 'aaa') {
                    if (row.actions.hasActions()) row.actions.addSeparator()
                    row.actions.addAction(controller: 'myController', action: 'rowAction3', params: [x: 1, y: 2])
                }
            }
        }

        c.addComponent(Header, 'h2')
        c.addComponent(Header, 'h3')
        c.addComponent(Header, 'h4')
        def table2 = c.addComponent(Table, 'table2')
        table2.with {
            rowStriped = true
            filters.with {
                /*addField(
                        class: Select,
                        id: 'user1',
                        optionsFromRecordset: TPerson.list(),
                        keys: ['username'],
                )*/
                addField(
                        class: DateField,
                        id: 'dateFrom',
                        cols: 3,
                )
                addField(
                        class: DateField,
                        id: 'dateTo',
                        cols: 3,
                )
            }

            groupActions.addAction(action: 'groupAction1')
            groupActions.addAction(action: 'groupAction2')
            groupActions.addAction(action: 'groupAction3')

            actions.addAction(action: 'action1.with.a.very.very.long.name')
            actions.addAction(action: 'action2')
            actions.addAction(controller: 'controller3')

            sortable = [
                    address: 'asc',
                    name: 'desc',
            ]
            columns = [
                    'dateCreated',
                    'company',
                    'name',
                    'picture',
                    'address',
            ]

            max = 3

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

        c.addComponent(Header, 'h5')
        def table3 = c.addComponent(Table, 'table3')
        table3.with {
            filters.with {
                /*addField(
                        class: Select,
                        id: 'user1',
                        optionsFromRecordset: TPerson.list(),
                        keys: ['username'],
                )*/
                addField(
                        class: DateField,
                        id: 'dateFrom',
                        cols: 3,
                )
                addField(
                        class: DateField,
                        id: 'dateTo',
                        cols: 3,
                )
            }

            groupActions.addAction(action: 'groupAction1')
            groupActions.addAction(action: 'groupAction2')
            groupActions.addAction(action: 'groupAction3')

            actions.addAction(action: 'action1.with.a.very.very.long.name')
            actions.addAction(action: 'action2')
            actions.addAction(controller: 'controller3')

            sortable = [
                    name: 'asc',
                    address: 'desc',
            ]
            columns = [
                    'dateCreated',
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

            max = 4

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

        // VALUES
        //
        println actionSession
        table.body = personService.list(table.filterParams, table.fetchParams)
        table.paginate = personService.count(table.filterParams)
        table2.body = personService.list(table2.filterParams, table2.fetchParams)
        table2.footer = [[dateCreated: 'TOTAL']]
        table2.paginate = personService.count(table2.filterParams)
        table2.pagination.reset()
        table3.body = personService.list(table3.filterParams, table3.fetchParams)
        table3.paginate = personService.count(table3.filterParams)

        display content: c
    }

    private buildForm(Map args = [:]) {
        def c = args.create
                ? createContent(ContentCreate)
                : createContent(ContentEdit)
        c.form.with {
            validate = TPerson
            addField(
                    class: Select,
                    id: 'company',
                    optionsFromRecordset: companyService.list(),
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
        return c
    }

    def create() {
        def c = buildForm(create: true)
        c.form['name'].readonly = true
        c.form['name'].value = 'Test Name'

        display content: c, modal: true, large: true
    }

    def onCreate() {
        def obj = personService.create(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        if (params.embedded) {
            display returnPoint(person: obj.id) + [modal: true]
        } else {
            display action: 'index'
        }
    }

    def edit() {
        def c = buildForm(create: false)
        def obj = personService.get(params.id)
        c.form.values = obj
        display content: c, modal: true, large: true
    }

    def onEdit() {
        def obj = personService.update(params)
        if (obj.hasErrors()) {
            display errors: obj
        } else {
            display action: 'index'
        }
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
