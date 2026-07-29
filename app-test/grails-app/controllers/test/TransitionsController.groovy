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

import dueuno.elements.components.Button
import dueuno.elements.components.Form
import dueuno.elements.components.Label
import dueuno.elements.components.Separator
import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.*
import dueuno.elements.core.ComponentEvent
import dueuno.elements.ElementsController
import dueuno.security.SecurityService
import dueuno.elements.style.TextStyle
import dueuno.types.Money
import dueuno.types.Quantity
import dueuno.types.QuantityUnit

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TransitionsController implements ElementsController {

    SecurityService securityService
    PersonService personService

    def index() {
        // PAGE SETUP
        //
        def c = createContent(ContentForm)
        c.header.nextButton.text = 'Reload'
        c.header.nextButton.icon = 'fa-rotate-right'

        c.form.with {
            if (params.transitioned) {
                addField(
                        class: Label,
                        id: 'transitioned',
                        text: 'Hai confermato, bravo!',
                        textColor: '#aa0000',
                        backgroundColor: "rgba(${primaryBackgroundColorInt.join(', ')}, ${primaryBackgroundColorAlpha})",
                        tag: true,
                        displayLabel: false,
                        cols: 12,
                )
            }

            addField(
                    class: Button,
                    id: 'messageButton',
                    action: 'showMessage',
                    displayLabel: false,
                    cols: 4,
            )
            addField(
                    class: Button,
                    id: 'errorButton',
                    action: 'showError',
                    displayLabel: false,
                    cols: 4,
            )
            addField(
                    class: Button,
                    id: 'confirmButton',
                    action: 'showConfirm',
                    displayLabel: false,
                    cols: 4,
            )
            addField(
                    class: Button,
                    id: 'btn1',
                    cols: 6,
            ).display = true
            addField(
                    class: Button,
                    id: 'btn2',
                    icon: 'fa-user',
                    cols: 6,
            ).display = false
            addField(
                    class: Button,
                    id: 'btn3',
                    cols: 6,
            ).display = true
            addField(
                    class: Button,
                    id: 'btn4',
                    cols: 6,
            ).display = false

            addField(
                    class: Label,
                    id: 'label',
                    text: 'Replace me or add something below me',
                    tag: true,
                    backgroundColor: primaryBackgroundColor,
                    cols: 12,
            )

            addField(
                    class: Select,
                    id: 'user1',
                    optionsFromRecordset: personService.list(),
                    keys: ['id'],
                    loading: true,
                    onChange: 'onSelectChange',
                    submit: 'form',
                    params: [id: 10],
                    textStyle: TextStyle.LINE_THROUGH,
                    cols: 12,
            )
//            .on(event: 'change', submit: 'form', params: [test: [a:1, b:2]], action: 'onSelectChange')

            addField(
                    class: Select,
                    id: 'select2',
                    readonly: true,
//                    onChange: 'onSelect2Change',
                    textStyle: TextStyle.BOLD,
                    cols: 12,
            ).on(event: 'change', submit: 'form', action: 'onSelect2Change')

            addField(
                    class: Select,
                    id: 'select3',
                    textStyle: TextStyle.ITALIC,
                    cols: 12,
            )
            addField(
                    class: MoneyField,
                    id: 'moneyfield',
                    decimals: 2,
                    cols: 12,
            )
            addField(
                    class: TextField,
                    id: 'textfield',
                    cols: 12,
            )
            addField(
                    class: NumberField,
                    id: 'numberfield',
                    decimals: 5,
                    cols: 12,
            )
            addField(
                    class: QuantityField,
                    id: 'quantityfield',
                    unit: QuantityUnit.KM,
                    cols: 12,
            )
            addField(
                    class: DateTimeField,
                    id: 'datetimefield',
                    value: LocalDateTime.now(),
                    cols: 12,
            )
            addField(
                    class: DateField,
                    id: 'datefield',
                    value: LocalDate.now(),
                    cols: 12,
            )
            addField(
                    class: TimeField,
                    id: 'timefield',
                    value: LocalTime.now(),
                    cols: 12,
            )

            addField(
                    class: Textarea,
                    id: 'textarea',
                    maxSize: 160,
                    cols: 12,
            )
            addField(
                    class: Checkbox,
                    id: 'checkbox',
                    cols: 12,
            )
            addField(
                    class: MultipleCheckbox,
                    id: 'multiple',
                    optionsFromRecordset: personService.list(max: 5),
                    keys: ['id'],
                    cols: 12,
            )
        }

        def formReplace = c.addComponent(Form, 'formReplace')
        formReplace.with {
            addField(class: TextField, id: 'f1', value: 'A value...', cols: 6)
            addField(class: TextField, id: 'f2', value: 'Another value...', cols: 6)
        }

        c.form['select2'].value = 'user3'
        c.form['textfield'].value = 'My text'
        c.form['moneyfield'].value = new Money(33)
        c.form['numberfield'].value = 55.2
        c.form['quantityfield'].value = new Quantity(12, QuantityUnit.L)
//        c.form['datefield'].value = LocalDate.now()
//        c.form['timefield'].value = LocalTime.now()
        c.form['multiple'].value = ['admin', 'user']

        // RENDERING
        //
        display content: c, modal: true
    }

    def showMessage() {
        def t = createTransition()
        t.infoMessage('transitions.messagebox.ciao', ['Mondo!'])
        display transition: t
    }

    def showError() {
        def t = createTransition()
        t.errorMessage('transitions.messagebox.error', ['Non ha funzionato!'])
        display transition: t
    }

    def showConfirm() {
        def onConfirm = new ComponentEvent(
                controller    : 'transitions',
                action        : 'index',
                params: [transitioned: true],
//                url           : 'https://www.google.com',
        )
        def t = createTransition()
        t.confirmMessage('transitions.messagebox.confirm', [securityService.currentUser.fullname], onConfirm)
        display transition: t
    }

    def removeForm() {
        def t = createTransition()
        t.remove(params.formName)
        display transition: t
    }

    def onSelectChange() {
        println params

        sleep(1000)
        def t = createTransition()

        t.addComponent(
                class: Label,
                id: 'label',
                tag: true,
                backgroundColor: 'green',
        )
        t.replace('label', 'label')
        t.set('content.nextButton', 'text', "New is cool! B-)")
        t.set('content.actions', 'text', "Act cool! B-)")

        List formNames = actionSession['formName'] ?: []
        if (formNames) {
            actionSession['formName'].add('formAppend' + formNames.size())
        } else {
            formNames = actionSession['formName'] = ['formAppend']
        }
        String formName = formNames.last()
        def formAppend = t.addComponent(Form, formName)
        formAppend.with {
            addField(class: Separator, id: 'sa1', text: "An appended form '${formName}'", squeeze: true, cols: 12)
            addField(class: TextField, id: 'fa1', value: 'An appended field...', cols: 4)
            addField(class: TextField, id: 'fa2', value: 'A second appended field...', cols: 4)
            addField(class: TextField, id: 'fa3', value: 'A third appended field...', cols: 4)
            addField(
                    class: Button,
                    id: 'removeButton',
                    action: 'removeForm',
                    params: [formName: formName],
                    text: "Remove '${formName}'",
                    displayLabel: false,
                    loading: false,
                    cols: 12,
            )
        }
        t.append('formReplace', formName)

        t.set('btn1Field', 'display', false)
        t.set('btn2Field', 'display', true)
        t.set('btn2', 'icon', 'fa-solid fa-gear')
        t.set('btn2Field', 'display', true)
        t.set('btn3Field', 'display', false)
        t.set('btn4Field', 'display', true)
        t.set('btn4Field', 'display', true)
        t.set('select2', 'options',
                Select.optionsFromRecordset(
                        recordset: personService.list(),
                        keys: ['id'],
                ))
        t.set('modal.select2', 'readonly', false)
        t.setValue('modal.select2', 3, true)

        //setting value and currency
        t.setValue('moneyfield', new Money(44, 'USD'))
        t.set('modal.moneyfield', 'readonly', true)

        //setting value and decimals
        t.setValue('numberfield', 123.45)
        t.set('numberfield', 'decimals', 3)

        //setting value and unit
        t.setValue('quantityfield', new Quantity(500, QuantityUnit.M))

        t.setValue('textfield', 'Test 12345')

        t.setValue('checkbox', true)
        t.set('checkbox', 'readonly', true)

        t.setValue('datetimefield', LocalDateTime.now().plusDays(3))
        t.setValue('datefield', LocalDate.now())
        t.set('datefield', 'readonly', true)
        t.setValue('timefield', LocalTime.now())
        t.set('timefield', 'readonly', true)

        t.set('multiple', 'options', ['user2', 'user3'])
        t.set('multiple', 'readonly', ['admin'])

        t.setValue('searchfield', 'admin')

        t.set('moneyfieldField', 'label', 'New label')
        t.set('moneyfieldField', 'nullable', false)

        t.set('textfieldField', 'error', 'This field has an error')
        //c.set('textfieldWrapper', 'error', false)

        t.set('textarea', 'readonly', true)

        t.loading(false)
        display transition: t
    }

    def onSelect2Change() {
        def t = createTransition()
        t.set('select3', 'options',
                Select.optionsFromRecordset(
                        recordset: personService.list(),
                        keys: ['name'],
                ))
        t.setValue('select3', 'admin')
        //c.set('select3', 'readonly', false)

        display transition: t
    }

    def onSearchfieldSearch() {
        def t = createTransition()
        t.set('searchfield', 'options',
                Select.optionsFromRecordset(
                        recordset: personService.list(name: params.searchfield),
                        keys: ['id'],
                )
        )

        display transition: t
    }

    def onConfirm() {
        println params
        display action: 'index'
    }
}
