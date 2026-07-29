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

import dueuno.elements.components.*
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.controls.Select
import dueuno.elements.controls.TextField
import dueuno.elements.core.ComponentEvent
import dueuno.elements.ElementsController
import dueuno.elements.style.TextDefault
import grails.gorm.multitenancy.CurrentTenant

@CurrentTenant
class TabsController implements ElementsController {

    def index() {
        def c = buildForm(params)
        display content: c, modal: true
    }

    private buildForm(Map args = [:]) {
        def c = args.create
                ? createContent(ContentCreate)
                : createContent(ContentEdit)

        c.header.nextButton.submit = ['f2', 'f1', 'table1']

        c.form.with {
            addField(
                    class: Button,
                    id: 'tabs',
                    displayLabel: false,
                    action: 'onShow',
                    text: "Show!",
                    stretch: true,
                    group: true,
                    loading: false,
            ).component.addAction(action: 'onHide', text: "Hide!")
        }

        Form f1 = c.addComponent(Form, 'f1')
        f1.with {
            display = true
            validate = F1
            addField(
                    class: Separator,
                    id: 'form.1',
                    squeeze: true,
            )
            addField(
                    class: TextField,
                    id: 't1',
                    help: 'Hai letto il manuale? No? Bravo, e come pensi di sapere le cose? Per scienza infusa?',
            )
            addField(
                    class: TextField,
                    id: 't2',
            )
            addField(
                    class: TextField,
                    id: 't3',
            )
            addField(
                    class: Select,
                    id: 's1',
                    options: [
                            1: 'Pippo',
                            2: 'Pluto',
                            3: 'Paperino',
                            4: 'Paperina',
                            5: 'Topolino',
                            6: 'Zio Paperone',
                            7: 'Mio Nonno',
                            8: 'Tua Nonna',
                    ]
            )
        }

        Form f2 = c.addComponent(Form, 'f2')
        f2.with {
            display = false
            validate = F2
            addField(
                    class: Separator,
                    id: 'form.2',
                    squeeze: true,
            )
            addField(
                    class: TextField,
                    id: 't1',
            )
            addField(
                    class: TextField,
                    id: 't2',
            )
            addField(
                    class: TextField,
                    id: 't3',
            )
        }

        Table table1 = c.addComponent(Table, 'table1')
        table1.with {
            title.text = 'Title'
            sortable = [
                    col1: 'asc',
            ]
            columns = [
                    'col1',
                    'col2',
                    'col3',
                    'col4',
                    'col5',
                    'col6',
                    'col7',
                    'col8',
                    'col9',
            ]
            actions.removeDefaultAction()
            body.eachRow { row ->
            }
            max = 5
            submit = [
                    'col1',
                    'col2',
                    'col3',
            ]
            body = [
                    [col1:1, col2:2, col3:3, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:4, col2:5, col3:6, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
                    [col1:7, col2:8, col3:9, col4:3, col5:3, col6:3, col7:3, col8:3, col9:3],
            ]
//            paginate = 20
        }
        table1.display = true

        Table table2 = c.addComponent(Table, 'table2')
        table2.display = false
        table2.with {
            filters.with {
                FormField tx = addField(
                        class: TextField,
                        id: 'find',
                        label: TextDefault.FIND,
                        cols: 12,
                )
                tx.component.addAction(
                        action: 'edit',
                )
                tx.component.addAction(
                        action: 'create',
                )
            }
        }

        if (args.f1) {
            f1.values = args.f1
        }

        if (args.f2) {
            f2.values = args.f2
        }

        return c
    }

    def onShow() {
        def t = createTransition()
        t.set('t1Field', 'visible', true)
        t.set('t2Field', 'visible', false)

        t.set('f1', 'display', true)
        t.set('f2', 'display', false)

        t.set('table1', 'display', true)
        t.set('table2', 'display', false)

        display transition: t
    }

    def onHide() {
        def t = createTransition()
        t.set('t1Field', 'visible', false)
        t.set('t2Field', 'visible', true)

        t.set('f1', 'display', false)
        t.set('f2', 'display', true)

        t.set('table1', 'display', false)
        t.set('table2', 'display', true)

        display transition: t
    }

    def onEdit(TabsValidator validator) {
        validator.f1.validate()
        validator.f2.validate()

        if (validator.f1.hasErrors() || validator.f2.hasErrors() ) {
            display errors: [f1: validator.f1, f2: validator.f2]

        } else {
            def t = createTransition()
            t.confirmMessage('Sicuro di voler continuare?', new ComponentEvent(
                    controller: 'table',
                    action: 'index',
                    params: [f1: params.f1, f2: params.f2, table1: params.table1],
                    modal: true,
            ))
            display transition: t

//            display action: 'index', params: [f1: params.f1, f2: params.f2]
        }
    }
}
