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

import dueuno.application.ApplicationService
import dueuno.elements.ElementsController
import dueuno.elements.components.ProgressBar
import dueuno.elements.components.Table
import dueuno.elements.components.TableRow
import dueuno.elements.controls.SelectX
import dueuno.elements.controls.TextField
import dueuno.elements.style.TextAlign
import dueuno.elements.style.TextStyle
import dueuno.types.Money
import jakarta.annotation.PostConstruct

class TableController implements ElementsController {

    ApplicationService applicationService
    PersonService personService

    @PostConstruct
    void init() {
        applicationService.registerPrettyPrinter(TCompany, 'Dipendente in \'${it.name}\'')
        applicationService.registerPrettyPrinter('NOME_FIGO', '${it} sei un figo!')

        applicationService.registerTransformer('MAIUSCOLO') { value ->
            return value.toString().toUpperCase()
        }
        applicationService.registerTransformer('MULTIPLY') { value ->
            println 'BEFORE ' + value
            Object result = value ? value * 100 : new Money(1000)
            println 'AFTER ' + result
            return result
        }
    }

    def index() {
        def c = createTable()
        println params
        display content: c
    }

    private createTable() {
        def c = createContent()
        def table = c.addComponent(Table)
        table.with {
            actionbar.addAction(
                action: 'action1',
                //confirmMessage: 'Confermare?',
            )

            filters.with {
                addField(
                    class: SelectX,
                    id: 'user1',
                    optionsFromRecordset: personService.list(),
                    keys: ['id'],
                )
                addField(
                    class: TextField,
                    id: 'textfield',
                )
            }

            rowStriped = true
            columns = [
                'name',
                'postcode',
                'address',
                'empty',
                'company',
                'salary',
                'active',
            ]
            sortable = [
                name: 'asc',
            ]
            labels = [
                email : 'Runtime generated label EMAIL',
                active: '', // Per non mostrare l'etichetta
                empty : '',
            ]
            transformers = [
                address: 'MAIUSCOLO',
                salary : 'MULTIPLY',
            ]
            prettyPrinters = [
                name: 'NOME_FIGO',
            ]
            prettyPrinterProperties = [
                salary  : [decimals: 0, symbolicCurrency: false, prefixedUnit: true],
                postcode: [renderZero: '-'],
            ]

            actions.addAction(action: 'commonAction')

            groupActions.addAction(action: 'groupAction1')
            groupActions.addAction(action: 'groupAction2')

            max = 4
            header.eachRow { TableRow row ->
                row.cells.postcode.backgroundColor = '#000077'
                row.cells.postcode.textColor = '#ffffff'
                row.cells.postcode.textAlign = TextAlign.END
                row.cells.name.textAlign = TextAlign.END
                row.cells.name.prettyPrinterProperties.textArgs = [3]
            }

            body.eachRow { row ->
                processMyRow(row)
            }

            // Assignment of "header", "body" and "footer" data must be done after ".eachRow" setup
            body = personService.list(params)
            footer = [
                [actions: 'Subtotale', name: '10.000', postcode: '200'],
                [actions: 'Totale', name: '10.000', postcode: '200'],
            ]
            paginate = personService.count()
        }

        /*page.addComponent(class: Table, id: 'table2').with {
            removeDefaultAction()
        }

        page.table2.body = [['':'']]*/

        return c
    }

    private processMyRow(TableRow row) {
        // coloro colonna
        row.cells.name.backgroundColor = '#00dd00'

        row.first?.actions.removeAllActions()
        row.first?.with {
            backgroundColor = 'rgba(204, 204, 0, 0.3)'
        }

        row.last?.with {
            actions.removeTailAction()
            actions.removeAction(action: 'commonAction')
            removeSelection()

            textColor = 'red'
            textStyle = [TextStyle.LINE_THROUGH, TextStyle.BOLD]

            actions.defaultAction?.text = 'Click me!'
            actions.defaultAction?.infoMessage = 'Ciao carissimo, come stai?'
            actions.defaultAction?.on(event: 'click', action: 'onActionClick')

            // Cell features
            cells.postcode.textAlign = TextAlign.CENTER
            cells.postcode.icon = 'fa-envelope'
            cells.postcode.url = 'mailto:g.sartori@gmail.com'

            cells.name.textAlign = TextAlign.CENTER
            //cells.username.html = '<a href="http://www.google.it">' + asset.image(src: "elements/brand/logo.png") + '</a>'

            // coloro riga
            backgroundColor = 'rgba(204, 0, 0, 0.3)'

            //coloro cella
            cells.address.backgroundColor = '#000077'
            cells.address.textColor = '#ffffff'

            //decoloro cella
            //row.cells.firstname.cssClass = ''
        }

        if (row.values.name == 'admin') {
            row.cells.name.prettyPrinterProperties.prettyPrinter = null

            row.actions.removeDefaultAction()
            row.actions.removeAction(action: 'commonAction')

            //row.values.lastname = LocalTime.now()
            /*row.cells.lastname.control = [
                    class: TimeField,
                    id: 'TimeField1',
            ]*/
            row.cells.address.component = [
                class: ProgressBar,
                id   : 'progress1',
                max  : 100,
                now  : 20,
            ]
        }

        if (row.values.name == 'user1') {
            //row.hasSelection = false
            row.values.address = 'runtime-overriden@email.com'

            row.actions.addSeparator()
            row.actions.addAction(action: 'rowAction')
            row.cells.postcode.component = [
                class: TextField,
                id   : 'TextField1',
            ]
            row.cells.postcode.colspan = 2
        }

        if (row.values.name == 'user2') {
            row.actions.removeDefaultAction()
            row.actions.removeTailAction()
        }

        if (row.actions.hasMenuActions()) {
            row.actions.addSeparator('test.separator.label')
            row.actions.addAction(controller: 'myAdminController', action: 'rowActionAdmin', params: [x: 1, y: 2])
        }
    }

    def onActionClick() {
        def t = createTable()

        t.table.body.eachRow { row ->

            processMyRow(row)

            if (row.values.username == 'user2') {
                // coloro riga
                row.backgroundColor = '#aa0000'
            }
        }

        def newActionButton = t.table.addControl(
            class: Button,
            id: 'newAction',
            isDefaultAction: true,
        )
        newActionButton.addAction(action: 'action2')

        t.replace(component: 'table.dataset', with: t.table.dataset)
        t.replace(component: 'table.actionbar.action1', with: newActionButton)

        display transition: t
    }
}
