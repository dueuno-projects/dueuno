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

import dueuno.core.PrettyPrinter
import dueuno.core.PrettyPrinterProperties
import dueuno.elements.components.*
import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.*
import dueuno.core.ApplicationService
import dueuno.elements.ElementsController
import dueuno.elements.style.Color
import dueuno.elements.style.TextAlign
import dueuno.elements.style.TextStyle
import dueuno.elements.style.TextTransform
import dueuno.elements.style.TextWrap
import dueuno.types.QuantityService
import dueuno.types.QuantityUnit

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FormController implements ElementsController {

    ApplicationService applicationService
    QuantityService quantityService
    PersonService personService

    def index() {
        def c = createContent(ContentForm)
        c.header.removeNextButton()
        c.form.with {
            addField(
                    class: Button,
                    id: 'button1',
                    action: 'edit',
                    submit: 'form',
                    stretch: true,
                    cols: 12,
            )
            addField(
                    class: Separator,
                    id: 'separator',
                    text: 'Options',
                    center: true,
                    underline: true,
                    cols: 12,
            )
            addField(
                    class: Checkbox,
                    id: 'modal',
                    displayLabel: false,
                    help: 'Questo è un messaggio di aiuto in una bottiglia',
                    cols: 6,
            )
            addField(
                    class: Checkbox,
                    id: 'wide',
                    displayLabel: false,
                    cols: 3,
            )
            addField(
                    class: Checkbox,
                    id: 'fullscreen',
                    displayLabel: false,
                    cols: 3,
            )
            addField(
                    class: Checkbox,
                    id: 'closeButton',
                    displayLabel: false,
                    cols: 6,
            )
            addField(
                    class: Checkbox,
                    id: 'isReadonly',
                    displayLabel: false,
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'animate',
                    optionsFromList: ['fade', 'next', 'back'],
                    textStyle: [TextStyle.BOLD, TextStyle.MONOSPACE],
                    defaultValue: 'fade',
                    displayLabel: false,
                    cols: 6,
            )
        }

        display content: c
    }

    def edit() {
        def modal = requireParam('modal')
        def wide = requireParam('wide')
        def fullscreen = requireParam('fullscreen')
        def closeButton = requireParam('closeButton')
        def isReadonly = requireParam('isReadonly')
        def animate = requireParam('animate')

        applicationService.registerTransformer('T_PERSON') {
            return "PERSON_TRANS [${it.id}] - ${it.id}"
        }
        applicationService.registerTransformer('CUSTOM_TRANSFORMER') {
            return "CUSTOM_TRANS [${it.key}] - ${it.id} ${it.lastname}"
        }

        applicationService.registerPrettyPrinter('OBJ2TEXT', '${it.name}')

        setReturnPoint()

        println "PERSON: " + params.person

        def c = createContent(ContentForm)
        c.header.with {
            addBackButton(
                    action: 'index',
                    animate: (animate ? 'back' : null)
            )

            nextButton.with {
                addAction(action: 'pluto')
            }
        }

        c.form.with {
            readonly = isReadonly
            def user1 = addField(
                    class: Select,
                    id: 'user1',
                    optionsFromRecordset: personService.list(),
                    keys: ['id'],
                    value: params.person,
                    help: 'Questo è un messaggio di aiuto per te che non sai cosa diavolo fare',
            )/*.on(
                    event: 'create',
                    controller: 'crud',
                    action: 'create',
                    modal: modal,
                    animate: (animate ? 'next' : null),
                    wide: wide,
                    params: [
                            embedded: true,
                    ],
            )*/
            user1.component.addAction(
                    action: 'index',
                    icon: 'fa-truck',
            )
            user1.component.addAction(
                    action: 'index',
                    icon: 'fa-car',
            )
            user1.component.addAction(
                    controller: 'crud',
                    action: 'create',
                    text: '',
                    icon: 'fa-plus',
                    modal: modal,
                    animate: animate,
                    wide: wide,
                    fullscreen: fullscreen,
                    params: [
                            embedded: true,
                            animate : animate,
                    ],
            )
            addField(
                    class: Select,
                    id: 'userTrans',
                    optionsFromRecordset: personService.list(),
                    transformer: 'T_PERSON',
                    textStyle: [TextStyle.LINE_THROUGH, TextStyle.MONOSPACE],
            )
            addField(
                    class: Select,
                    id: 'curtomTrans',
                    optionsFromRecordset: [
                            [key: 1, name: 'Gianluca', lastname: 'Sartori'],
                            [key: 2, name: 'Francesco', lastname: 'Piceghello'],
                    ],
                    transformer: 'CUSTOM_TRANSFORMER',
                    search: false,
                    keys: ['key'],
                    value: 2,
            )
            addField(
                    class: TextField,
                    id: 'textfield',
                    textTransform: TextTransform.UPPERCASE,
                    help: 'Questo è un messaggio di aiuto per te che non sai cosa diavolo fare',
            )
            addField(
                    class: TextField,
                    id: 'textfield2',
                    value: [name: 'pippo'],
                    prettyPrinter: 'OBJ2TEXT',
                    help: 'Questo è un messaggio di aiuto per te che non sai cosa diavolo fare',
                    helpCollapsed: true,
            )
            def textfieldActions = addField(
                    class: TextField,
                    id: 'textfieldActions',
                    prefix: 'PIPPO',
                    textStyle: [TextStyle.LINE_THROUGH, TextStyle.MONOSPACE],
            )
            textfieldActions.component.addAction(
                    action: 'index',
                    submit: 'form',
                    icon: 'fa-truck',
            )
            textfieldActions.component.addAction(
                    action: 'index',
                    submit: 'form',
                    icon: 'fa-car',
            )
            addField(
                    class: NumberField,
                    id: 'numberfield',
                    value: 0,
                    cols: 6,
            ).component.addAction(
                    action: 'increment',
                    submit: 'form',
                    icon: 'fa-plus',
                    text: '',
            ).addAction(
                    action: 'decrement',
                    submit: 'form',
                    icon: 'fa-minus',
                    text: '',
            )
            addField(
                    class: EmailField,
                    id: 'emailfield',
                    placeholder: 'me@mail.com',
                    cols: 6,
            )
            addField(
                    class: UrlField,
                    id: 'urlfield',
                    cols: 6,
            )
            addField(
                    class: PasswordField,
                    id: 'passwordfield',
                    cols: 6,
            )
            addField(
                    class: QuantityField,
                    id: 'quantityfield',
                    defaultUnit: QuantityUnit.KM,
                    availableUnits: quantityService.listAllUnits(),
                    cols: 6,
            )
            addField(
                    class: MoneyField,
                    id: 'moneyfield',
                    cols: 6,
            )
            addField(
                    class: Upload,
                    id: 'upload1',
            )

            addField(
                    class: Upload,
                    id: 'upload2',
                    dragAndDrop: false,
            )
            addField(
                    class: Button,
                    id: 'button',
                    stretch: true,
                    primary: true,
            ).component.addAction(action: 'anotherAction1')
                    .addAction(action: 'anotherAction2')
                    .addAction(action: 'anotherAction3')
                    .addAction(action: 'anotherAction4')

            def buttonGroupField = addField(
                    class: Button,
                    id: 'buttonGroup',
                    stretch: true,
                    group: true,
                    backgroundColor: Color.DANGER_BACKGROUND,
                    textColor: Color.DANGER_TEXT,
            )
            buttonGroupField.component.addAction(action: 'anotherAction1', backgroundColor: Color.WARNING_BACKGROUND, textColor: Color.WARNING_TEXT)
            buttonGroupField.component.addAction(action: 'anotherAction2', backgroundColor: Color.SUCCESS_BACKGROUND, textColor: Color.SUCCESS_TEXT)
            buttonGroupField.component.addAction(action: 'anotherAction3', backgroundColor: Color.INFO_BACKGROUND, textColor: Color.INFO_TEXT)
            buttonGroupField.component.addAction(action: 'anotherAction4', backgroundColor: mainBackgroundColor, textColor: Color.DISABLED_TEXT)
            addField(
                    class: Separator,
                    id: 'separator',
                    icon: 'fa-car',
            )
            addField(
                    class: Separator,
                    id: 'anotherSeparator',
                    icon: 'fa-truck',
            )
            addField(
                    class: DateField,
                    id: 'datefield',
                    help: 'Questo è un messaggio di aiuto per te che non sai cosa diavolo fare',
                    cols: 6,
            )
            addField(
                    class: TimeField,
                    id: 'timefield',
                    cols: 6,
            )
            addField(
                    class: DateTimeField,
                    id: 'datetimefield',
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'user2',
                    optionsFromRecordset: personService.list(),
                    multiple: true,
                    keys: ['id'],
                    search: false,
            )
            addField(
                    class: Checkbox,
                    id: 'checkbox',
                    textArgs: ['ARG_1'],
                    help: 'Questo è un messaggio di aiuto per te che non sai cosa diavolo fare',
                    onChange: 'onChangeCheckbox',
                    cols: 6,
            )
            addField(
                    class: Checkbox,
                    id: 'checkbox2',
                    value: true,
                    readonly: true,
                    cols: 6,
            )
            addField(
                    class: Checkbox,
                    id: 'simplecheckbox',
                    simple: true,
            )
            /*addField(
                    class: MultipleCheckbox,
                    id: 'multiple',
                    checkboxes: ['1', '2', '3', '4', '5'],
                    textPrefix: 'checkbox',
                    //simple: true,
            )*/
//            addField(
//                    class: Keypad,
//                    id: 'keypad',
//            )
            addField(
                    class: Textarea,
                    id: 'textarea',
                    maxSize: 100,
                    rows: 5,
            )

            addField(
                    class: Label,
                    id: 'label',
                    label: 'The label of a label',
            )
            addField(
                    class: Label,
                    id: 'label2',
                    textAlign: TextAlign.END,
            )
            addField(
                    class: Label,
                    id: 'info',
                    textAlign: TextAlign.CENTER,
                    backgroundColor: '#eab676',
                    displayLabel: false,
            )
            addField(
                    class: Label,
                    id: 'label3',
                    textWrap: TextWrap.LINE_WRAP,
                    tag: false,
            )
            addField(
                    class: Label,
                    id: 'paragraph',
                    tag: false,
            )

            addField(
                    class: Label,
                    id: 'errorColor',
                    textWrap: TextWrap.NO_WRAP,
                    textColor: Color.DANGER_TEXT,
                    backgroundColor: Color.DANGER_BACKGROUND,
                    cols: 2,
            )
            addField(
                    class: Label,
                    id: 'warningColor',
                    textWrap: TextWrap.NO_WRAP,
                    textColor: Color.WARNING_TEXT,
                    backgroundColor: Color.WARNING_BACKGROUND,
                    cols: 2,
            )
            addField(
                    class: Label,
                    id: 'successColor',
                    textWrap: TextWrap.NO_WRAP,
                    textColor: Color.SUCCESS_TEXT,
                    backgroundColor: Color.SUCCESS_BACKGROUND,
                    cols: 2,
            )
            addField(
                    class: Label,
                    id: 'infoColor',
                    textWrap: TextWrap.NO_WRAP,
                    textColor: Color.INFO_TEXT,
                    backgroundColor: Color.INFO_BACKGROUND,
                    cols: 2,
            )
            addField(
                    class: Label,
                    id: 'disabledColor',
                    textWrap: TextWrap.NO_WRAP,
                    textColor: Color.DISABLED_TEXT,
                    backgroundColor: mainBackgroundColor,
                    cols: 4,
            )
        }

        c.form['datefield'].value = LocalDate.now()
        c.form['timefield'].value = LocalTime.now()
        c.form['datetimefield'].value = LocalDateTime.now()
        c.form['label'].text = """
            [label.text] Lorem ipsum
                    dolor sit amet
        """
        c.form['label2'].text = """[label.text + TextAlign.END]"""
        c.form['label3'].text = """[label.text + TextWrap.LINE_WRAP]
            Lorem ipsum
            dolor sit amet"""
        c.form['info'].text = '[label.text + tag] Lorem ipsum dolor sit amet'
        c.form['paragraph'].html = '<em>[label.html]</em> <strong>Lorem ipsum dolor sit amet</strong>, <u>consectetur adipisci elit</u>, <a href="#">sed eiusmod tempor incidunt ut labore et dolore magna aliqua</a>. Ut enim ad minim veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.'

        c.addComponent(Table, 'colorTable').with {
            columns = [
                    'status',
                    'textColor',
                    'backgroundColor',
            ]
            rowActions = false
            body.eachRow { TableRow row, Map values ->
                row.textColor = values.textColor
                row.backgroundColor = values.backgroundColor
            }
            body = [
                    [status: 'ERROR', textColor: Color.DANGER_TEXT, backgroundColor: Color.DANGER_BACKGROUND],
                    [status: 'WARNING', textColor: Color.WARNING_TEXT, backgroundColor: Color.WARNING_BACKGROUND],
                    [status: 'SUCCESS', textColor: Color.SUCCESS_TEXT, backgroundColor: Color.SUCCESS_BACKGROUND],
                    [status: 'INFO', textColor: Color.INFO_TEXT, backgroundColor: Color.INFO_BACKGROUND],
                    [status: 'DISABLED', textColor: Color.DISABLED_TEXT, backgroundColor: mainBackgroundColor],
            ]
        }

        c.addComponent(Table, 'table').with {
//            rowActions = false
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
            actions.addAction(action: 'test1')
            actions.addAction(action: 'test2')
            actions.addAction(action: 'test3')
            actions.addAction(action: 'test4')
            actions.addAction(action: 'test5')
            actions.addAction(action: 'test6')
            body = personService.list(max: 10)
            paginate = personService.count()
        }

        display content: c, modal: modal, wide: wide, fullscreen: fullscreen, animate: animate, closeButton: closeButton
    }

    def onChangeCheckbox() {
        def t = createTransition()
        if (params.checkbox) {
            t.set('checkbox', 'text', message('form.checkbox.text', ['SELECTED']))
        } else {
            t.set('checkbox', 'text', message('form.checkbox.text', ['NOT SELECTED']))
        }
        display transition: t
    }

    def onConfirm() {
        println params
        display
    }

    def increment() {
        def t = createTransition()
        Integer n = params.numberfield ?: 0
        n++
        t.setValue('numberfield', [value: n])
        display transition: t
    }

    def decrement() {
        def t = createTransition()
        Integer n = params.numberfield ?: 0
        if (n > 0) n--
        t.setValue('numberfield', [value: n])
        display transition: t
    }
}
