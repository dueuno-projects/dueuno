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

import dueuno.commons.utils.DateUtils
import dueuno.elements.ElementsController
import dueuno.elements.components.*
import dueuno.elements.controls.*
import dueuno.core.ApplicationService
import dueuno.security.SecurityService
import dueuno.security.TUser
import dueuno.elements.style.*
import dueuno.types.QuantityService
import dueuno.types.Money
import dueuno.types.Quantity
import dueuno.types.QuantityUnit
import dueuno.types.Type
import jakarta.servlet.ServletContext

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class MyType {
    Integer i
}

class SandboxController implements ElementsController {

    ServletContext servletContext
    ApplicationService applicationService
    QuantityService quantityService
    SecurityService securityService
    PersonService personService

    def handleException(Exception e) {
        display exception: e
    }

    def index() {

        println params

        applicationService.registerTransformer('DB2UNIT') { value ->
            if (value == 'PZ') value = 'PCS'
            return value
        }

        applicationService.registerTransformer('DB2QUANTITY') { List value ->
            return new Quantity(value[0] as BigDecimal, value[1] as QuantityUnit)
        }

        applicationService.registerTransformer('DB2DATE') { value ->
            if (!value || value == 'null') return null
            return DateUtils.parseLocalDate(value, 'yyyyMMdd')
        }

        def c = createContent()
        c.header.addNextButton(
                action: 'onSubmit',
                submit: ['form', 'formFail', 'tableTest', 'table2'],
                text: 'Submit',
        )

        def formFail = c.addComponent(Form, 'formFail')
        formFail.with {
            // Typed key fields
            addKeyField('listTest', Type.LIST, [1, 2, 3])
            addKeyField('mapTest', Type.MAP, [a:1, b:2, c:[a:1, b:2]])
            addKeyField('datetimeTest', Type.DATETIME, LocalDateTime.now())
            addKeyField('dateTest', Type.DATE, LocalDate.now())
            addKeyField('timeTest', Type.TIME, LocalTime.now())

            // Automatic type retrieval
            addKeyField('listTestAuto', [1, 2, 3])
            addKeyField('mapTestAuto', [a:1, b:2, c:[a:1, b:2]])
            addKeyField('datetimeTestAuto', LocalDateTime.now())
            addKeyField('dateTestAuto', LocalDate.now())
            addKeyField('timeTestAuto', LocalTime.now())

//            addKeyField('typeError', new MyType(i: 10))
//            addKeyField('typeError', 'MY_TYPE', 10)

            addField(
                    class: Button,
                    id: 'messageAndRedirect',
                    action: 'onMessage',
                    modal: true,
                    displayLabel: false,
                    cols: 12,
                    colsSmall: 6,
            )
            addField(
                    class: Button,
                    id: 'confirmMessageAndRedirect',
                    action: 'onConfirmMessage',
                    params: [name: 'Gianluca Sartori'],
                    displayLabel: false,
                    cols: 12,
            )
            addField(
                    class: Button,
                    id: 'loadingScreen',
                    action: 'onHideLoadingScreen',
                    displayLabel: false,
                    cols: 12,
                    colsSmall: 6,
            )
            addField(
                    class: Button,
                    id: 'customParams',
                    action: 'index',
                    onClick: 'onCustomParams',
//                    params: [money: new Money(10), quantity: new Quantity(20)],
                    displayLabel: false,
                    cols: 12,
                    colsSmall: 6,
            )
            addField(
                    class: Button,
                    id: 'setErrors',
                    action: 'onSetErrors',
                    submit: 'formFail',
                    loading: false,
                    displayLabel: false,
                    cols: 12,
                    colsSmall: 6,
                    highlight: true,
            )
            addField(
                    class: DateTimeField,
                    id: 'dt1',
                    value: LocalDate.now().minusDays(4),
                    min: LocalDate.now().minusDays(3),
                    onLoad: 'onDateTimeLoad',
                    onChange: 'onDateTimeChange',
                    textStyle: TextStyle.LINE_THROUGH,
                    highlight: true,
                    cols: 4,
            )
            addField(
                    class: DateField,
                    id: 'd1',
                    value: LocalDate.now().minusDays(4),
                    min: LocalDate.now().minusDays(3),
                    onChange: 'onDateChange',
                    textStyle: [TextStyle.LINE_THROUGH, TextStyle.ITALIC],
                    highlight: true,
                    cols: 4,
            )
            addField(
                    class: TimeField,
                    id: 't1',
                    defaultValue: LocalTime.now(),
                    min: LocalTime.now().minusHours(3),
                    timeStep: 15,
                    onChange: 'onTimeChange',
                    textStyle: TextStyle.NORMAL,
                    help: 'This is a help message',
                    cols: 2,
            )
            addField(
                    class: TimeField,
                    id: 't2',
                    value: LocalTime.now(),
                    readonly: true,
                    help: 'This is a help message',
                    cols: 2,
            )
            addField(
                    class: MonthField,
                    id: 'm1',
                    value: LocalDate.now(),
                    textStyle: [TextStyle.LINE_THROUGH, TextStyle.NORMAL],
                    highlight: true,
                    help: 'This is a help message',
                    cols: 6,
            )
            addField(
                    class: PasswordField,
                    id: 'passWithHelp',
                    help: 'Type your password',
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'selectEnum',
                    optionsFromEnum: TextFieldInputMode,
                    highlight: true,
            )
            addField(
                    class: Select,
                    id: 'select2',
                    label: 'onLoad',
                    onLoad: 'onSelect2Load',
                    value: 99,
//                    onChange: 'onSelect2Change',
                    allowClear: true,
                    cols: 12,
            )
            addField(
                    class: Select,
                    id: 'select3',
                    label: 'onSearch',
                    onLoad: 'onSelect3Load',
                    onSearch: 'onSelect3Search',
                    onChange: 'onSelect3Change',
                    submit: ['formFail'],
                    help: 'This is a long help message, long, long, long, long, long, long, long',
                    value: 3,
                    allowClear: true,
                    cols: 12,
//                    highlight: true,
            )
            addField(
                    class: MoneyField,
                    id: 'testMoney',
                    value: new Money(),
                    cols: 6,
                    highlight: true,
            )
            addField(
                    class: QuantityField,
                    id: 'testQuantity',
                    availableUnits: quantityService.listAllUnits(),
                    defaultUnit: QuantityUnit.KG,
                    value: new Quantity(),
                    cols: 6,
                    highlight: true,
            )
            addField(
                    class: TextField,
                    id: 'placeholderText',
                    placeholder: 'sandbox.placeholder.text',
                    validChars: '/:1234567890',
//                    prefix: 'PRE',
                    icon: 'fa-box',
                    onChange: 'onIconChange',
                    highlight: true,
                    maxSize: 7,
                    cols: 6,
            )
            addField(
                    class: TextField,
                    id: 'readonlyText',
                    readonly: true,
                    cols: 6,
            )
            addField(
                    class: Textarea,
                    id: 'placeholderArea',
                    placeholder: 'sandbox.placeholder.textarea',
                    invalidChars: '+-*/',
                    acceptNewLine: false,
                    highlight: true,
                    cols: 12,
            )
            addField(
                    class: Button,
                    id: 'placeholderBtn',
                    action: 'index',
                    icon: 'fa-user',
                    onClick: 'onSetPlaceholder',
                    loading: false,
                    cols: 2,
            )
            addField(
                    class: Button,
                    id: 'targetNew',
                    action: 'index',
                    icon: 'fa-user',
                    targetNew: true,
                    cols: 10,
            )

            for (i in 1..6) {
                addField(
                        class: Label,
                        id: "${i}Label",
                        displayLabel: false,
                        textArgs: [i],
                        userSelect: true,
                        cols: 9,
                )
                addField(
                        class: DateField,
                        id: "${i}Date",
                        displayLabel: false,
                        cols: 3,
                )
            }

            addField(
                    class: TextField,
                    id: 'textUp',
                    value: 'lower UPPER Capitalized',
                    textTransform: TextTransform.UPPERCASE,
                    onChange: 'onTextChange',
                    cols: 4,
            )
            addField(
                    class: TextField,
                    id: 'textLow',
                    value: 'lower UPPER Capitalized',
                    textTransform: TextTransform.LOWERCASE,
                    cols: 4,
            )
            addField(
                    class: TextField,
                    id: 'textCap',
                    value: 'lower UPPER Capitalized',
                    textTransform: TextTransform.CAPITALIZE,
                    cols: 4,
            )
            addField(
                    class: NumberField,
                    id: 'textPattern',
                    pattern: '^(?!.*@.*@)(?!.*(\\.)\\1).[a-z0-9_\\.@]*$',
                    cols: 6,
            )
            addField(
                    class: Checkbox,
                    id: 'checkThisOut',
                    onChange: 'onChangeCheckThisOut',
                    help: 'This is a help message',
                    cols: 6,
            )

            def linkField = addField(
                    class: Link,
                    id: 'imageWithLink',
                    controller: 'form',
                    modal: true,
                    cols: 12,
            )
            linkField.component.addComponent(
                    class: Image,
                    id: 'theImage',
                    image: linkPublicResource("brand/login-logo.png", false),
            )
            linkField.component.addComponent(
                    class: Label,
                    id: 'theLabel',
                    text: 'This is a text',
            )

            addField(
                    class: Label,
                    id: 'errorField',
                    displayLabel: false,
                    cols: 12,
            )
            addField(
                    class: Label,
                    id: 'labelTest',
                    textStyle: TextStyle.MONOSPACE,
                    cols: 12,
                    textWrap: TextWrap.LINE_BREAK,
                    text: """
2022-01-04 15:46:57.006  INFO --- [           main] dueuno.solutions.test.Application        : The following profiles are active: development

Configuring Spring Security Core ...
... finished configuring Spring Security Core

ThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWord

2022-01-04 15:47:34.218  INFO --- [           main] dueuno.solutions.test.Application        : Started Application in 38.708 seconds (JVM running for 41.81)
Grails application running at http://localhost:9992/test in environment: development
"""
            )

//            addField(
//                    class: MultipleCheckbox,
//                    id: 'employees',
//                    optionsFromRecordset: personService.list(),
////                    value: TCompany.get(2).employees,
//            )
//            addField(
//                    class: Button,
//                    id: 'failCols',
//                    group: true,
//                    action: 'onShowHide',
//                    params: [show: true],
//                    cols: 12,
////                    cols: 13, // Throws Exception
//            )
//            addField(
//                    class: TextField,
//                    id: 'textUp',
//                    value: 'lower UPPER Capitalized',
//                    textTransform: TextTransform.UPPERCASE,
//                    cols: 4,
//            )
//            addField(
//                    class: TextField,
//                    id: 'textLow',
//                    value: 'lower UPPER Capitalized',
//                    textTransform: TextTransform.LOWERCASE,
//                    cols: 4,
//            )
//            addField(
//                    class: TextField,
//                    id: 'textCap',
//                    value: 'lower UPPER Capitalized',
//                    textTransform: TextTransform.CAPITALIZE,
//                    cols: 4,
//            )
//            addField(
//                    class: TextField,
//                    id: 'textTest',
//                    textTransform: TextTransform.CAPITALIZE,
//                    cols: 12,
//            )
        }

        formFail.values = [
                name: 'PIPPO',
                t1: LocalTime.now().plusHours(3)
        ]

        def table = c.addComponent(Table, 'tableTest')
        table.with {
            title.display = true
            filters.with {
                addField(
                        class: TextField,
                        id: 'find',
                        label: TextDefault.FIND,
                        cols: 12,
                )
            }
            columns = [
                    'company',
                    'name',
                    'address',
                    'postcode',
                    'input',
            ]
            submit = [
                    'company',
                    'name',
                    'customColumn',
            ]
            widths = [
                    'input': 150,
            ]
            actions.addAction(action: 'test1')
            actions.addAction(action: 'test2')
            actions.addAction(action: 'test3')
            body.eachRow { TableRow row, Map values ->
                values.customColumn = 'PIPPO'

                row.cells.postcode.tag = true

                row.cells.input.component = [
                        class   : NumberField,
                        id      : "number${values.id}",
                        min     : -2,
                        max     : 10,
                        textAlign: TextAlign.END,
                ]
                row.cells.input.component.addAction(
                        action: 'onDecrement',
                        submit: "tableTest-${row.index}",
                        icon: 'fa-minus',
                        text: '',
                )
                row.cells.input.component.addAction(
                        action: 'onIncrement',
                        submit: "tableTest-${row.index}",
                        icon: 'fa-plus',
                        text: '',
                )

            }
            max = 10
            body = personService.list(max: 10)
            paginate = personService.count()
        }

        c.addComponent(
                class: Button,
                id: 'reject',
                action: 'onReject',
                icon: 'fa-xmark',
                stretch: true,
        )
        c.addComponent(
                class: Button,
                id: 'setCellValue',
                action: 'onSetCellValue',
                params: [value: '**PIPPO**'],
                stretch: true,
                loading: false,
        )

        def table2 = c.addComponent(Table, 'table2')
        table2.with {
//            hasComponents = true
            title.display = true
            title.icon = 'fa-file'
//            filters.with {
//                addField(
//                        class: TextField,
//                        id: 'find',
//                        label: TextDefault.FIND,
//                        cols: 12,
//                )
//            }
            columns = [
                    'company',
                    'name',
                    'address',
                    'postcode',
                    'input',
            ]
            submit = [
                    'company',
            ]
            body.eachRow { TableRow row, Map values ->
            }
            body = personService.list(max: 10)
        }
//
//        def form = c.addComponent(Form)
//        form.with {
//            addField(
//                    class: Button,
//                    id: 'btnHide',
//                    group: true,
//                    action: 'onShowHide',
//                    params: [show: true],
//                    cols: 12,
//            ).component.addAction(
//                    action: 'onShowHide',
//                    params: [show: false],
//            )
//            addField(
//                    class: DateTimeField,
//                    id: 'dt1',
//                    cols: 4,
//            )
//            addField(
//                    class: DateField,
//                    id: 'd1',
//                    cols: 4,
//            )
//            addField(
//                    class: TimeField,
//                    id: 't1',
//                    cols: 4,
//            )
//            addField(
//                    class: Separator,
//                    id: 's1',
//                    text: 'This is a long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long long text!',
//                    cols: 12,
//            )
//            addField(
//                    class: Select,
//                    id: 'select1',
//                    label: 'onSearch',
//                    minLength: 3,
//                    help: 'Digitare "user" o "admin"',
//                    onSearch: 'onSearch',
//                    optionsFromRecordset: securityService.listUser(),
//                    defaultValue: 3,
//                    noSelectionText: 'Seleziona qualcosa',
//                    cols: 12,
//            )
//            addField(
//                    class: Select,
//                    id: 'select2',
//                    label: 'onLoad',
//                    onLoad: 'onLoadOptions',
//                    cols: 12,
//            )
//
////            addField(
////                    class: Select,
////                    id: 'transformerSelect',
////                    optionsFromList: quantityService.listUnit(),
////                    defaultValue: 'KM',
////                    noSelection: true,
////                    transformer: 'DB2UNIT',
////                    textPrefix: 'quantity.unit',
////            )
//            addField(
//                    class: NumberField,
//                    id: 'number',
//                    decimals: 5,
//                    negative: false,
//                    cols: 12,
//            )
//            addField(
//                    class: QuantityField,
//                    id: 'qtaTransform',
//                    availableUnits: quantityService.listAllUnits(),
//                    transformer: 'DB2QUANTITY',
//                    defaultUnit: Unit.KG,
//                    cols: 9,
//
//            )
//            addField(
//                    class: Button,
//                    id: 'qtaReadonly',
//                    action: 'onQtaSetReadonly',
//                    text: 'Disable',
//                    icon: 'fa-times',
//            )
////            addField(
////                    class: DateField,
////                    id: 'datainival',
////                    transformer: 'DB2DATE',
////                    defaultValue: '20210704',
////                    cols: 6,
////            )
//            addField(
//                    class: Button,
//                    id: 'exceptionTest',
//                    action: 'onThrowException',
//                    cols: 12,
//            )
//            addField(
//                    class: Label,
//                    id: 'label',
//                    monospace: true,
//                    cols: 12,
//                    textWrap: TextWrap.LINE_BREAK,
//                    text: """
//2022-01-04 15:46:57.006  INFO --- [           main] dueuno.solutions.test.Application        : The following profiles are active: development
//
//Configuring Spring Security Core ...
//... finished configuring Spring Security Core
//
//ThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWordThisIsAnUnbrakableWord
//
//2022-01-04 15:47:34.218  INFO --- [           main] dueuno.solutions.test.Application        : Started Application in 38.708 seconds (JVM running for 41.81)
//Grails application running at http://localhost:9992/test in environment: development
//"""
//            )
//        }

//        form['select1'].value = 1
//        form['transformerSelect'].value = 'PZ'
//        form['transformQta'].value = [3, 'M']
//        form['transformQta'].value = new Quantity(3, Unit.M)

        c.addComponent(
                before: 'formFail',
                class: Label,
                id: 'addedBeforeFormFail',
                text: 'I am the one 8-)',
                textAlign: TextAlign.CENTER,
                backgroundColor: 'red',
                textColor: Color.WHITE,
                tag: true,
        )

        display content: c, modal: true
    }

    def onIconChange() {
        def t = createTransition()
        t.set('placeholderText', 'icon', 'fa-box-open')
        display transition: t
    }

    def onMessage() {
        display message: 'You will be redirected to "CRUD View"', controller: 'crud', modal: true, large: true
    }

    def onConfirmMessage() {
        display confirmMessage: 'sandbox.message.with.params', messageArgs: [params.name], controller: 'table', modal: true, large: true
    }

    def onTextChange() {
        println "Event has been triggered"
        display
    }

    def onHideLoadingScreen() {
        sleep(5000)
        def t = createTransition()
        t.loading(false)
        display transition: t
    }

    def onChangeCheckThisOut() {
        println params
        display transition: createTransition()
    }

    def onReject() {
        def user = new TUser(username: 'G', password: 'G')
        user.errors.reject('obj.reject.error.test')
        display errors: user
    }

    def onSetErrors() {
        display errors: [testMoney: 'Something wrong here', placeholderText: 'Here as well!']
    }

    def onSetCellValue() {
        def t = createTransition()
        t.call('table2', 'setValueByIndex', [row: 1, column: 'name', value: params.value])
        t.call('table2', 'setValueByKey', [keyName: 'id', keyValue: 2, column: 'address', value: params.value])
        display transition: t
    }

    def onDateTimeLoad() {
        def t = createTransition()
        t.set('d1', 'min', LocalDate.now().minusDays(5))
        t.set('t1', 'min', LocalTime.now().minusHours(5))
        display transition: t
    }

    def onDateTimeChange() {
        def t = createTransition()
        t.set('placeholderText', 'placeholder', 'DateTime changed!')
        t.set('placeholderArea', 'placeholder', params.dt1 as String)
        t.set('d1', 'min', LocalDate.now().minusDays(10))
        t.set('t1', 'min', LocalTime.now().minusHours(10))
        display transition: t
    }

    def onDateChange() {
        def t = createTransition()
        t.set('placeholderText', 'placeholder', 'Date changed!')
        t.set('placeholderArea', 'placeholder', params.d1 as String)
        display transition: t
    }

    def onTimeChange() {
        def t = createTransition()
        t.set('placeholderText', 'placeholder', 'Time changed!')
        t.set('placeholderArea', 'placeholder', params.t1 as String)
        t.setValue('t2', params.t1?.plusHours(3))
        t.set('t2', 'min', params.t1?.plusHours(3))
        t.set('t2', 'focus', true)
//        t.set('t2', 'focus', false)
        display transition: t
    }

    def onSetPlaceholder() {
        def t = createTransition()
        t.set('placeholderText', 'placeholder', 'Pippo')
        t.set('placeholderArea', 'placeholder', 'Pluto')
        display transition: t
    }

    def onCustomParams() {
        display action: 'index', params: [string: 'String', number: 1, bool: true, temporal: LocalDate.now(), money: new Money(10), quantity: null, enum: TestEnum.THREE]
    }

    def onSelect2Load() {
        def t = createTransition()
        def results = securityService.listAllUser()
        def options = Select.optionsFromRecordset(recordset: results)
        t.set('select2', 'options', options)
        t.setValue('select2', 99)
        display transition: t
    }

    def onSelect2Change() {
        def t = createTransition()
        def user = securityService.getUser(params.select2)
        t.setValue('select3', user.id)
        display transition: t
    }

    def onSelect3Load() {
        println "LOAD: $params"
        def t = createTransition()
        def results = securityService.listAllUser(id: params.select3)
        def options = Select.optionsFromRecordset(recordset: results)
        t.set('select3', 'options', options)
        display transition: t
    }

    def onSelect3Search() {
        println "SEARCH: $params"
        def t = createTransition()
        def search = params.select3?.replaceAll('\\*', '%')
        def results = securityService.listAllUser([find: search], [sort: [firstname: 'asc']])
        def options = Select.optionsFromRecordset(recordset: results)
        t.set('select3', 'options', options)
        display transition: t
    }

    def onSelect3Change() {
        def t = createTransition()
        def user = securityService.getUser(params.select3)
        t.set('placeholderText', 'placeholder', user ? user.fullname + " (" + user.username + ")" : 'NO SELECTION')
        display transition: t
    }

    def onQtaSetReadonly() {
        def disable = session.disable == null ? true : session.disable
        session.disable = !disable
        def t = createTransition()
        t.set('qtaTransform', 'readonly', disable)
        t.set('qtaReadonly', 'text', disable ? 'Enable' : 'Disable')
        t.set('qtaReadonly', 'icon', disable ? 'fa-solid fa-check' : 'fa-solid fa-times')
        display transition: t
    }

    def onShowHide() {
        def t = createTransition()
        if (params.show) {
            t.set('dt1', 'visible', true)
            t.set('d1', 'visible', true)
            t.set('t1', 'visible', true)
        } else {
            t.set('dt1', 'visible', false)
            t.set('d1', 'visible', false)
            t.set('t1', 'visible', false)
        }
        display transition: t
    }

    def onThrowException() {
        try {
            throw new Exception("C'è stato un errore!")
        } catch (e) {
            display exception: e, action: 'index'
        }
    }

    def onSearch() {
        def t = createTransition()
        t.set('select1', 'options', Select.optionsFromRecordset(
                recordset: personService.list(name: params.select1),
        ))
        display transition: t
    }

    def onSubmit() {
        println params

        try {
            throw new Exception(params as String)

        } catch (Exception e) {
            display exception: e
        }
    }

    def onIncrement() {
        println "(+) ${params}"
        display
    }

    def onDecrement() {
        println "(-) ${params}"
        display
    }
}