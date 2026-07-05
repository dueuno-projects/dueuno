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

import dueuno.audit.AuditService
import dueuno.elements.components.Form
import dueuno.elements.components.Label
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentForm
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.*
import dueuno.core.ApplicationService
import dueuno.elements.ElementsController
import dueuno.elements.style.Color
import dueuno.elements.style.TextDefault
import dueuno.elements.style.TextWrap
import dueuno.types.QuantityService
import dueuno.types.Type
import grails.plugin.springsecurity.annotation.Secured

import java.time.LocalDate

class CrudController implements ElementsController {

    AuditService auditService
    ApplicationService applicationService
    QuantityService quantityService
    PersonService personService
    CompanyService companyService

    def index() {

//        sleep(2000)

        applicationService.registerTransformer('TRANSFORM_ME') { TCompany value ->
            return "<i class='fa-fw fa-solid fa-building me-1'></i>${value.name.toUpperCase()}"
        }

        applicationService.registerPrettyPrinter('PRETTY_MAP', '${it.a} ${it.b} ${it.c} ${it.d}')

//        throw new Exception("ATTENZIONE!!!")

        def c = createContent(ContentTable)

        c.title = 'Runtime "content" title'

        if (isMobileDevice()) {
            c.addComponent(
                    class: Label,
                    id: 'isMobileDevice',
                    text: "Running on a mobile device.",
                    backgroundColor: Color.WARNING_BACKGROUND,
                    textColor: Color.WARNING_TEXT,
            )
        }

        c.with {
            header.sticky = false
            header.text = 'Runtime "content.header" title'
            header.addBackButton().with {
                addAction(action: 'pippo')
            }
            header.nextButton.addAction(action: 'onCreateRecords')
            header.nextButton.addAction(controller: 'crudCompany', modal: true)
            header.nextButton.addAction(controller: 'crudPOGO', modal: true)
            header.nextButton.defaultAction.tooltip = 'default.create'

            def form = c.addComponentBefore('table', Form)
            form.with {
                addField(
                        class: Select,
                        id: 'testLoadingScreen',
                        optionsFromList: ['Select me...', 'Select me too!'],
                        onChange: 'onTestLoadingScreen',
                        loading: true,
                        cols: 3,
                )
            }

            table.with {
                rowStriped = true
                stickyHeaderOffset = 0
//                stickyHeaderZIndex = 9999

                noResultsIcon = ''

                filters.with {
                    fold = false
                    autoFold = true
                    addField(
                            class: Select,
                            id: 'company',
                            optionsFromRecordset: companyService.list(),
                            transformer: 'TRANSFORM_ME',
//                            renderTextPrefix: true,
                            multiple: true,
                            placeholder: 'Seleziona qualcosa',
                            cols: 6,
                    )
                    addField(
                            class: TextField,
                            id: 'name',
                            cols: 3,
                    )
                }

                actionbar.with {
                    addAction(
                            action: 'doubleDisplay',
                    )
                    addAction(
                            action: 'loadingScreenOnRedirect',
                    )
                    addAction(
                            controller: 'someController',
                            params: [p1: 1, p2: 2],
                    )
                }

                groupActions.addAction(
                        action: 'onGroupAction1',
                        submit: ['table'],
                        params: [
                                s: 'Stringa',
                                n: 12345,
                                l: [1, 2, 3], // TODO: Le liste non vengono passate come params
                                m: [firstname: 'Giangio', lastname: 'Sartori'],
                                ml: [ids: [1, 2, 3]],
                                lm: [[id:1], [id:2], [id: 3]] // TODO: Le liste non vengono passate come params
                        ],
                        //confirmMessage: 'Vado?' // TODO: aggiungendo confirmMessage '_tableSelecttion' non viene passata
                )
                groupActions.addAction(action: 'onGroupAction2')
                groupActions.addAction(controller: 'onGroupAction3', confirmMessage: 'Messaggio di conferma, sei sicuro?')

                actions.addAction(action: 'action1.with.a.very.very.long.name', icon: 'fa-arrow-alt-circle-up')
                actions.addAction(action: 'action2', icon: 'fa-arrow-alt-circle-right')
                actions.addAction(controller: 'controller3', icon: 'fa-arrow-alt-circle-down')

                actions.tailAction.order = 9999
                actions.tailAction.text = TextDefault.DELETE
                actions.unsetTailAction()
                actions.defaultAction.tooltip = 'Modifica record'

                sortable = [
                        address: 'asc',
                        name: 'desc',
                ]
                columns = [
                        'tagIcon',
                        'company',
                        'employeeCount',
                        'name',
                        'picture',
                        'address',
                        'postcode',
                        'salary',
                        'salaryPerMonth',
                        'distanceKm',
                        'dateStart',
                        'dateEnd',
                        'active',
                        'prettyMap',
                        'prettyHtml',
                        'prettyObjectList',
                ]
                includeValues = [
                        'company.employees',
                ]
                prettyPrinterProperties = [
                        salary: [renderZero: '-'],
                        salaryPerMonth: [highlightNegative: false, renderZero: '-'],
                        name: [renderTextPrefix: true],
                ]
                prettyPrinters = [
                        prettyMap: 'PRETTY_MAP',
                ]
                widths = [
                        company: 300,
                ]

                max = 10
//                sort = [name: 'desc']

                body.eachRow { TableRow row, Map values ->
                    values.prettyMap = [a: 'This', b: "is", c: "a", d: "Map"]
                    values.prettyObjectList = [personService.create(name: 'Company 1'), personService.create(name: 'Company 2')]
                    row.cells.prettyHtml.html = 'table.cell.label.html'
//                    row.verticalAlign = VerticalAlign.TOP
                    row.cells.postcode.tag = true

                    row.cells.tagIcon.icon = 'fa-user'
                    row.cells.tagIcon.tag = true
                    row.cells.tagIcon.tooltip = 'User'

                    row.cells['name'].textWrap = TextWrap.SOFT_WRAP
                    row.cells['company'].textWrap = TextWrap.LINE_WRAP
                    row.cells['company'].icon = 'fa-lightbulb'
                    row.cells['company'].tooltip = 'Questa è una azienda'
                    row.cells['company'].url = 'https://google.com'

                    TCompany company = companyService.get(values.company.id)
                    values.employeeCount = company.employees.size()

                    if (values.salary) println prettyPrint(values.salary)
                    if (values.picture) row.cells.picture.icon = 'fa-file'
                    if (values.salary) values.salaryPerMonth = values.salary / 12
                    if (row.actions.hasActions()) row.actions.addSeparator()
                    row.actions.addAction(
                            controller: 'myController',
                            action: 'rowAction3',
                            params: [x: 1, y: 2],
                            icon: 'fa-times-circle',
                    )
                    if (values.name == 'aaa') row.actions.setDefaultAction(controller: 'myController', action: 'rowAction3')
                    //row.tailAction.danger = true

                    row.cells.address.label.textWrap = TextWrap.NO_WRAP
                }
            }
        }

        c.table.body = personService.list(c.table.filterParams, c.table.fetchParams)
        c.table.paginate = personService.count(c.table.filterParams)

        display content: c
    }

    def doubleDisplay() {
        display
        display action: 'index'
    }

    def loadingScreenOnRedirect() {
//        sleep(5000)
        display action: 'index'
    }

    def onTestLoadingScreen() {
//        sleep(5000)
        display action: 'index', loading: true
    }

    def onCreateRecords() {
        TCompany company = companyService.get(1)

        (1..100).each {
            personService.create(
                    active: true,
                    company: company,
                    name: 'user' + it,
                    address: 'Via del\'automazione, ' + it,
                    postcode: 12345
            )
        }

        display action: 'index'
    }

    def onGroupAction1() {
        println params
        display action: 'index'
    }

    private buildForm(TPerson obj = null) {
        def c = obj
                ? createContent(ContentEdit)
                : createContent(ContentCreate)

        c.header.nextButton.confirmMessage = 'Are you sure?'

        if (params.embedded) {
            c.header.addBackButton(animate: params.animate)
            c.header.backButton.with {
                addAction(action: 'pippo')
            }
            if (params.animate) {
                c.header.nextButton.animate = params.animate
            }
        }

        c.form.with {
            validate = TPerson

            addKeyField('embedded')
            addKeyField('selection', Type.LIST, [[id: 1]])
            addField(
                    class: Select,
                    id: 'company',
                    optionsFromRecordset: companyService.list(),
                    prettyPrinter: 'customCompanyPrinter',
                    onChange: 'onCompanyChange',
            )
            addField(
                    class: TextField,
                    id: 'companyName',
                    readonly: true,
            )
            addField(
                    class: TextField,
                    id: 'name',
                    defaultValue: 'XXX',
            )
            addField(
                    class: Textarea,
                    id: 'address',
            )
            addField(
                    class: NumberField,
                    id: 'postcode',
            )
            addField(
                    class: MoneyField,
                    id: 'salary',
                    negative: true,
            )
            addField(
                    class: QuantityField,
                    id: 'distanceKm',
                    availableUnits: quantityService.listAllUnits(false, false, true, false, false, false, false),
            )
            addField(
                    class: DateField,
                    id: 'dateStart',
                    min: LocalDate.now().minusDays(3),
                    max: LocalDate.now().plusDays(3),
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

    def onCompanyChange() {
        def t = createTransition()
        def company = companyService.get(params.company)
        t.setValue('companyName', company?.name)
        t.set('name', 'readonly', company)
        display transition: t
    }

    def create() {
        def c = buildForm()
        display content: c, modal: true
    }

    def edit() {
        def obj = personService.get(params.id)
        def c = buildForm(obj)
        display content: c, modal: true
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

    @Secured(['permitAll'])
    def register() {
        def c = createContent(ContentForm)
        c.header.removeNextButton()

        display content: c, modal: true
    }

    @Secured(['permitAll'])
    def passwordRecovery() {
        def c = createContent(ContentForm)
        c.header.removeNextButton()

        display content: c, modal: true
    }
}
