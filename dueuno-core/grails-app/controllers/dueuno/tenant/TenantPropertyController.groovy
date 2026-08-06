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
package dueuno.tenant

import dueuno.application.PropertyType
import dueuno.commons.utils.StringUtils
import dueuno.elements.ElementsController
import dueuno.elements.components.Label
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.*
import dueuno.elements.style.TextAlign
import dueuno.elements.style.TextDefault
import dueuno.elements.style.TextStyle
import dueuno.elements.style.TextTransform
import dueuno.security.SecurityService
import grails.plugin.springsecurity.annotation.Secured

/**
 * @author Gianluca Sartori
 */
@Secured(['ROLE_ADMIN'])
class TenantPropertyController implements ElementsController {

    SecurityService securityService
    TenantPropertyService tenantPropertyService

    def index() {
        def c = createContent(ContentTable)
        def isDeveloper = securityService.isDeveloper()

        if (isDeveloper) {
            c.header.nextButton.with {
                removeAllActions()
                addAction(
                    action: 'create',
                    params: [type: 'STRING'],
                    text: message("tenantProperty.STRING"),
                    icon: 'fa-plus'
                )
                for (String type in PropertyType.values()*.name()) {
                    if (type == 'STRING') continue
                    addAction(
                        action: 'create',
                        params: [type: type],
                        text: message("tenantProperty.${type}"),
                    )
                }
            }
        } else {
            c.header.removeNextButton()
        }

        c.table.with {
            filters.with {
                fold = false
                addField(
                    class: TextField,
                    id: 'find',
                    label: TextDefault.FIND,
                    cols: 8,
                )
                addField(
                    class: Select,
                    id: 'type',
                    optionsFromEnum: PropertyType,
                    noSelection: true,
                    cols: 2,
                )
                addField(
                    class: Select,
                    id: 'validation',
                    optionsFromList: ['error'],
                    search: false,
                    noSelection: true,
                    cols: 2,
                )
            }
            sortable = [
                name: 'asc',
            ]
            columns = [
                'issues',
                'name',
                'type',
                'value',
                'description',
            ]
            labels = [
                issues: '',
            ]
            prettyPrinterProperties = [
                value       : [decimals: 5],
                defaultValue: [decimals: 5],
            ]

            if (!isDeveloper) {
                actions.removeTailAction()
            }

            body.eachRow { TableRow row, Map values ->
                row.cells.value.textStyle = TextStyle.MONOSPACE
                row.cells.value.textAlign = TextAlign.START
                row.cells.type.tag = true

                if (values.validation) {
                    row.textColor = '#aa0000'
                    row.cells.issues.icon = 'fa-circle-exclamation'
                    row.cells.issues.tooltip = values.validation
                }

                String typeName = StringUtils.screamingSnakeToCamel(values.type as String)
                values.value = values[typeName]

                String descriptionCode = "tenant.property.${values.name}"
                String description = messageOrBlank(descriptionCode)
                row.cells.description.html = description ?: descriptionCode
                if (!description) {
                    row.cells.description.textColor = mainBackgroundColor
                }

                if (values.type == PropertyType.BOOL) {
                    values.value = values.value ? 'TRUE' : 'FALSE'
                }

                if (values.type == PropertyType.PASSWORD) {
                    values.value = '*******'
                }
            }
        }

        def filters = c.table.filterParams
        c.table.body = tenantPropertyService.list(filters, params)
        c.table.paginate = tenantPropertyService.count(filters)

        display content: c
    }

    private buildForm(TTenantProperty obj) {
        def c = createContent(ContentEdit)
        c.form.with {
            validate = TTenantProperty

            String descriptionCode = "tenant.property.${obj.name}"
            String description = messageOrBlank(descriptionCode)
            if (description) {
                addField(
                    class: Label,
                    id: 'description',
                    html: description,
                    displayLabel: false,
                    tag: true,
                    backgroundColor: mainBackgroundColor,
                )
            }

            addField(
                class: TextField,
                id: 'name',
                textStyle: TextStyle.MONOSPACE,
                textTransform: TextTransform.UPPERCASE,
                readonly: obj.name,
                cols: 9,
            )
            addField(
                class: Select,
                id: 'type',
                optionsFromEnum: PropertyType,
                textStyle: TextStyle.MONOSPACE,
                readonly: true,
                nullable: true,
                cols: 3,
            )

            switch (obj.type) {
                case PropertyType.STRING:
                    if (obj.stringDefault) {
                        addField(
                            class: Textarea,
                            id: 'defaultValue',
                            textStyle: TextStyle.MONOSPACE,
                            readonly: true,
                            rows: 2,
                        )
                    }
                    addField(
                        class: Textarea,
                        id: 'value',
                        textStyle: TextStyle.MONOSPACE,
                        rows: 2,
                    )
                    break

                case PropertyType.PASSWORD:
                    addField(
                        class: PasswordField,
                        id: 'value',
                    )
                    break

                case PropertyType.FILENAME:
                    if (obj.filenameDefault) {
                        addField(
                            class: TextField,
                            id: 'defaultValue',
                            textStyle: TextStyle.MONOSPACE,
                            readonly: true,
                        )
                    }
                    addField(
                        class: TextField,
                        id: 'value',
                        textStyle: TextStyle.MONOSPACE,
                        onLoad: 'onValidate',
                        onChange: 'onValidate',
                    )
                    break

                case PropertyType.DIRECTORY:
                    if (obj.directoryDefault) {
                        addField(
                            class: TextField,
                            id: 'defaultValue',
                            textStyle: TextStyle.MONOSPACE,
                            readonly: true,
                        )
                    }
                    addField(
                        class: TextField,
                        id: 'value',
                        textStyle: TextStyle.MONOSPACE,
                        onLoad: 'onValidate',
                        onChange: 'onValidate',
                    )
                    break

                case PropertyType.URL:
                    if (obj.urlDefault) {
                        addField(
                            class: TextField,
                            id: 'defaultValue',
                            textStyle: TextStyle.MONOSPACE,
                            readonly: true,
                        )
                    }
                    addField(
                        class: TextField,
                        id: 'value',
                        textStyle: TextStyle.MONOSPACE,
                        onLoad: 'onValidate',
                        onChange: 'onValidate',
                    )
                    break

                case PropertyType.NUMBER:
                    if (obj.numberDefault) {
                        addField(
                            class: NumberField,
                            id: 'defaultValue',
                            textStyle: TextStyle.MONOSPACE,
                            decimals: 5,
                            readonly: true,
                        )
                    }
                    addField(
                        class: NumberField,
                        id: 'value',
                        textStyle: TextStyle.MONOSPACE,
                        decimals: 5,
                    )
                    break

                case PropertyType.BOOL:
                    if (obj.boolDefault) {
                        addField(
                            class: Checkbox,
                            id: 'defaultValue',
                            readonly: true,
                            label: '',
                        )
                    }
                    addField(
                        class: Checkbox,
                        id: 'value',
                        label: '',
                    )
                    break

                case PropertyType.DATETIME:
                    if (obj.datetimeDefault) {
                        addField(
                            class: DateTimeField,
                            id: 'defaultValue',
                            readonly: true,
                        )
                    }
                    addField(
                        class: DateTimeField,
                        id: 'value',
                    )
                    break

                case PropertyType.DATE:
                    if (obj.dateDefault) {
                        addField(
                            class: DateField,
                            id: 'defaultValue',
                            readonly: true,
                        )
                    }
                    addField(
                        class: DateField,
                        id: 'value',
                    )
                    break

                case PropertyType.TIME:
                    if (obj.timeDefault) {
                        addField(
                            class: TimeField,
                            id: 'defaultValue',
                            readonly: true,
                        )
                    }
                    addField(
                        class: TimeField,
                        id: 'value',
                    )
                    break
            }
        }

        if (obj) {
            c.form.values = obj
            if (obj.type == PropertyType.PASSWORD) {
                c.form['value'].value = tenantPropertyService.getPassword(obj.name, true)
            } else {
                c.form['value'].value = tenantPropertyService.getValue(obj.type, obj.name, true)
            }
        }

        return c
    }

    def onValidate() {
        def t = createTransition()

        def validation
        switch (params.type as PropertyType) {
            case PropertyType.FILENAME:
                validation = tenantPropertyService.validateFilename(params.value)
                break

            case PropertyType.DIRECTORY:
                validation = tenantPropertyService.validateDirectory(params.value)
                break

            case PropertyType.URL:
                validation = tenantPropertyService.validateUrl(params.value)
                break
        }

        if (validation) {
            t.set('valueField', 'error', validation)
        }

        display transition: t
    }

    def create() {
        def obj = new TTenantProperty(type: params.type as PropertyType)
        def c = buildForm(obj)
        display content: c, modal: true
    }

    def edit() {
        def obj = tenantPropertyService.get(params.id)
        def c = buildForm(obj)
        display content: c, modal: true
    }

    def onEdit() {
        if (!params.name) {
            display errors: [name: 'nullable']
            return
        }

        if (params.type == 'PASSWORD') {
            tenantPropertyService.setPassword(params.name, params.value)

        } else {
            tenantPropertyService.setValue(params.type as PropertyType, params.name, params.value)
        }

        display action: 'index'
    }

    def onDelete() {
        try {
            tenantPropertyService.delete(params.id)
            display action: 'index'

        } catch (Exception e) {
            display exception: e
        }
    }
}
