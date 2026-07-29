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

import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.Select
import dueuno.elements.core.Elements
import dueuno.elements.ElementsController
import dueuno.elements.style.TextDefault

class MessageBoxDemoController implements ElementsController {

    def index() {
        def c = createContent(ContentForm)
        c.with {
            header.removeNextButton()
            form.with {
                addField(
                        class: 'Label',
                        id: 'paraghraph',
                ).value = 'Works with "TPerson" entities. Search or create.'
                addField(
                        class: 'Select',
                        id: 'select',
                        onSearch: 'onSelectSearch',
                        onCreate: 'onSelectCreate',
                )
//                .on(event: 'search', action: 'onSelectSearch')
//                .on(event: 'create', action: 'onSelectCreate')
            }
        }

        display content: c
    }

    def onSelectCreate() {
        def t = createTransition()
//        t.loading(false)
        t.popup.with {
            label = 'default.create.label'
            labelArgs = ['TPerson']
            form.with {
                addField(
                        class: 'TextField',
                        id: 'name',
                        label: 'TPerson.name',
                )
                addField(
                        class: 'NumberField',
                        id: 'postcode',
                        label: 'TPerson.postcode',
                )
                addField(
                        class: 'MoneyField',
                        id: 'salary',
                        label: 'TPerson.salary',
                )
                addKeyField(id: 'checkbox', value: false)
            }
            nextButton.with {
                label = TextDefault.CREATE
                on(event: 'click', action: 'onSelectCreateExec')
            }
        }
        t.popup.show()
        display transition: t
    }

    def onSelectCreateExec(TDemo obj) {
        def t = createTransition()

        if (obj.hasErrors()) {
            t.popup.validate(obj)
            display transition: t
            return
        }

        obj.save(flush: true, failOnError: true)

        t.set('select', 'options',
                Select.optionsFromRecordset(
                        recordset: TPerson.where{ id == obj.id }.list(),
                )
        )
        t.setValue('select', obj.id)
        t.popup.hide()
        display transition: t
    }

    def onSelectSearch() {
        render Elements.encodeAsJSON(Select.optionsFromRecordset(
                recordset: TPerson.where {name =~ "%${params.select}%"}.list(),
        ))

        //Select.searchResults { options: optionsFromRecordset ...
    }
}
