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
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.Checkbox
import dueuno.elements.controls.Select
import dueuno.elements.controls.TextField
import dueuno.types.Money

class TableStressTestController implements ElementsController {

    PersonService personService

    private rowsQty = 100
    private colsQtyPerType = 50
    private colsType = [
        'String',
        'BigDecimal',
        'Money',
        'Boolean',
    ]

    private List generateColumns() {
        if (controllerSession.cols)
            return controllerSession.cols

        def cols = []
        for (type in colsType) {
            for (int colIndex in 1..colsQtyPerType) {
                cols.add("$type-$colIndex")
            }
        }

        controllerSession.cols = cols
        return cols
    }

    private List generateTestRecordset() {

        def cols = generateColumns()
        def rows = []
        for (int rowIndex in 1..rowsQty) {
            def row = [:]
            for (column in cols) {
                switch (column.split('-')[0]) {
                    case 'String':
                        row[column] = "Test String"
                        break
                    case 'BigDecimal':
                        row[column] = new BigDecimal("123456789.12345")
                        break
                    case 'Money':
                        row[column] = new Money(123456789.12345, 'USD')
                        break
                    case 'Boolean':
                        row[column] = true
                        break
                }
            }
            rows.add(row)
        }

        return rows
    }

    def index() {

        def c = createContent(ContentTable)
        c.with {
            table.with {

                rowStriped = true
                rowHighlight = false

                filters.with {
                    addField(
                        class: Checkbox,
                        id: 'checkbox',
                    )
                    addField(
                        class: Select,
                        id: 'user1',
                        optionsFromRecordset: personService.list(),
                        keys: ['id'],
                    )
                    addField(
                        class: TextField,
                        id: 'textfield',
                    )
                }

                actionbar.with {
                    addAction(
                        action: 'index2',
                        text: 'HTML Table',
                    )
                }

                groupActions.addAction(action: 'groupAction1')
//                groupActions.addAction(action: 'groupAction2')
//                groupActions.addAction(controller: 'groupAction3', confirmMessage: 'Messaggio di conferma, sei sicuro?')

                actions.addAction(action: 'action1.with.a.very.very.long.name')
                actions.addAction(action: 'action2')
                actions.addAction(controller: 'controller3')

//                rowActions = false

                //max = rowsQty
                columns = generateColumns()

                /*body.with {
                    eachRow { row, values ->
                        if (values.numberfield == 23) {
                            if (row.actions.hasActions()) row.actions.addSeparator()
                            row.actions.addAction(controller: 'myController', action: 'rowAction3', params: [x: 1, y: 2])
                        }
                    }
                }*/
            }
        }

        def rs = generateTestRecordset()
        c.table.body = rs
        c.table.paginate = 500

        display content: c
    }


    def index2() {
        render view: 'htmlTable', model: [columns: generateColumns(), recordset: generateTestRecordset()]
    }

}
