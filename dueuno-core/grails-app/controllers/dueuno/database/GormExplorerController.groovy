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
package dueuno.database

import dueuno.commons.utils.SqlUtils
import dueuno.core.ConnectionSourceService
import dueuno.elements.Elements
import dueuno.elements.ElementsController
import dueuno.elements.components.Button
import dueuno.elements.components.Form
import dueuno.elements.components.Table
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.*
import dueuno.elements.style.TextDefault
import dueuno.security.SecurityService
import dueuno.tenants.TenantService
import dueuno.types.CustomType
import dueuno.types.Types
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import jakarta.annotation.PostConstruct

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Secured(['ROLE_DEVELOPER'])
class GormExplorerController implements ElementsController {

    SecurityService securityService
    TenantService tenantService
    ConnectionSourceService connectionSourceService

    @PostConstruct
    void init() {
        // Executes only once when the application starts
    }

    private List<Class> getDomainProperties(Class domainClass) {
        List<Class> results = []
        domainClass.constrainedProperties.each {
            if (it.value.property.propertyType !in Set) {
                results << it
            }
        }
        return results
    }

    private List<String> getDomainColumns(Class domainClass) {
        List<String> results = ['id']
        domainClass.constrainedProperties.each {
            if (it.value.property.propertyType !in Set) {
                results << it.key.toString()
            }
        }
        return results
    }

    private Map<String, String> getDomainFieldNames(Class domainClass) {
        Map<String, String> results = [id: 'id']
        domainClass.constrainedProperties.each {
            if (it.value.property.propertyType !in Set) {
                results << [(it.key): it.key.toString()]
            }
        }
        return results
    }

    def index() {
        def c = createContent()

        c.header.nextButton.text = 'gormExplorer.sqlConsole'
        c.header.nextButton.icon = 'fa-pen-to-square'
        c.header.nextButton.action = 'sqlConsole'

        def resetPagination = false
        String tenantId = params.tenantId ?: controllerSession['tenantId'] ?: securityService.currentUser.tenant.tenantId
        if (params.tenantId && params.tenantId != controllerSession['tenantId']) resetPagination = true
        controllerSession['tenantId'] = tenantId

        String domainClassName = params.domainClassName ?: controllerSession['domainClassName']
        if (params.domainClassName && params.domainClassName != controllerSession['domainClassName']) resetPagination = true
        controllerSession['domainClassName'] = domainClassName
        Class domainClass
        if (domainClassName) {
            domainClass = grailsApplication.getDomainClass(domainClassName).clazz
            controllerSession['domainClass'] = domainClass
        }

        def form = c.addComponent(Form)
        form.with {
            sticky = true
            if (securityService.isSuperAdmin()) {
                addField(
                        class: Select,
                        id: 'tenantId',
                        optionsFromRecordset: tenantService.list(),
                        keys: ['tenantId'],
                        allowClear: false,
                        defaultValue: tenantService.defaultTenantId,
                        onChange: 'index',
                        submit: 'form',
                        cols: 3,
                )
            } else {
                addField(
                        class: TextField,
                        id: 'tenantId',
                        defaultValue: tenantId,
                        readonly: true,
                        cols: 3,
                )
            }

            addField(
                    class: Select,
                    id: 'domainClassName',
                    optionsFromList: grailsApplication.domainClasses*.fullName,
                    defaultValue: domainClassName,
                    renderTextPrefix: false,
                    search: true,
                    onChange: 'index',
                    submit: 'form',
                    cols: 7,
            )
            addField(
                    class: Button,
                    id: 'btnCreate',
                    action: 'create',
                    text: TextDefault.CREATE,
                    icon: 'fa-plus',
                    readonly: !domainClassName,
                    cols: 2,
            )
        }

        def table = c.addComponent(Table)
        if (resetPagination) table.pagination.reset()
        if (domainClassName) {
            table.with {
                filters.with {
                    fold = true
                    addField(
                            class: NumberField,
                            id: 'id',
                            label: 'Id',
                            cols: 2,
                    )
                    addField(
                            class: TextField,
                            id: 'find',
                            label: TextDefault.FIND,
                            cols: 10,
                    )
                }

                columns = getDomainColumns(domainClass)
                labels = getDomainFieldNames(domainClass)
                sortable = [id: 'desc']

                body.eachRow { TableRow row, Map values ->
                }
            }

            tenantService.withTenant(tenantId) {
                Number searchId = table.filterParams.id as Number
                String searchText = table.filterParams.find
                Number searchNumber
                try {
                    searchNumber = table.filterParams.find as Long
                } catch (Exception ignore) {
                    searchNumber = null
                }

                def query = new DetachedCriteria(domainClass).build {

                    // Dynamic associations fetch
                    for (property in getDomainProperties(domainClass)) {
                        Class propertyClass = property.value.property.propertyType
                        String propertyName = property.key

                        if (Elements.isDomainClass(propertyClass)) {
                            join propertyName
                        }
                    }

                    if (searchId) {
                        eq 'id', searchId
                    }

                    if (searchText) {
                        or {
                            for (property in getDomainProperties(domainClass)) {
                                Class propertyClass = property.value.property.propertyType
                                String propertyName = property.key

                                if (searchText && propertyClass in String) {
                                    ilike propertyName, "%${searchText}%"

                                } else if (searchText && propertyClass in CustomType && propertyClass['TYPE_VALUE_PROPERTY_TYPE'] == String) {
                                    ilike propertyName + '.' + propertyClass['TYPE_VALUE_PROPERTY_NAME'], "%${searchText}%"

                                } else if (searchNumber && propertyClass in CustomType && propertyClass['TYPE_VALUE_PROPERTY_TYPE'] == Number) {
                                    eq propertyName + '.' + propertyClass['TYPE_VALUE_PROPERTY_NAME'], searchNumber

                                } else if (searchNumber && propertyClass in Number) {
                                    eq propertyName, searchNumber
                                }
                            }
                        }
                    }
                }

                table.body = query.list(table.fetchParams)
                table.paginate = query.count()
            }
        }

        display content: c
    }

    private buildForm(String tenantId, Class domainClass, Object obj = null) {
        def c = obj
                ? createContent(ContentEdit)
                : createContent(ContentCreate)

        c.header.text = domainClass.simpleName

        c.form.with {
            validate = domainClass

            addField(
                    class: NumberField,
                    id: 'id',
                    label: 'id',
                    readonly: true,
            )

            for (property in getDomainProperties(domainClass)) {
                Class propertyClass = property.value.property.propertyType
                String propertyName = property.key

                if (Types.isRegistered(propertyClass)) {
                    addField(
                            class: propertyClass['TYPE_FIELD'],
                            id: propertyName,
                            label: propertyName,
                    )
                    continue
                }

                if (Elements.isDomainClass(propertyClass)) {
                    addField(
                            class: Select,
                            id: propertyName,
                            label: propertyName,
                            optionsFromRecordset: propertyClass.list(),
                    )
                    continue
                }

                Class fieldClass
                switch (propertyClass) {
                    case String:
                        fieldClass = TextField
                        break

                    case Number:
                        fieldClass = NumberField
                        break

                    case Boolean:
                        fieldClass = Checkbox
                        break

                    case LocalDateTime:
                        fieldClass = DateTimeField
                        break

                    case LocalDate:
                        fieldClass = DateField
                        break

                    case LocalTime:
                        fieldClass = TimeField
                        break

                    default:
                        fieldClass = TextField
                }

                addField(
                        class: fieldClass,
                        id: propertyName,
                        label: propertyName,
                        text: propertyName,
                )

            }
        }

        if (obj) {
            c.form.values = obj
        }

        return c
    }

    def create() {
        String tenantId = controllerSession['tenantId']
        Class domainClass = controllerSession['domainClass']

        if (!domainClass) {
            display message: 'gormExplorer.select.table.first'
            return
        }

        def c = buildForm(tenantId, domainClass)
        display content: c, modal: true
    }

    @Transactional
    def onCreate() {
        String tenantId = controllerSession['tenantId']
        Class domainClass = controllerSession['domainClass']

        tenantService.withTenant(tenantId) {
            def obj = domainClass.newInstance(params)
            obj.save(flush: true)

            if (obj.hasErrors()) {
                display errors: obj
                return
            }

            display action: 'index'
        }
    }

    def edit() {
        String tenantId = controllerSession['tenantId']
        Class domainClass = controllerSession['domainClass']

        tenantService.withTenant(tenantId) {
            def obj = domainClass.get(params.id)
            def c = buildForm(tenantId, domainClass, obj)
            display content: c, modal: true
        }
    }

    @Transactional
    def onEdit() {
        String tenantId = controllerSession['tenantId']
        Class domainClass = controllerSession['domainClass']

        tenantService.withTenant(tenantId) {
            def obj = domainClass.get(params.id)
            obj.properties = params
            obj.save(flush: true)

            if (obj.hasErrors()) {
                display errors: obj
                return
            }

            display action: 'index'
        }
    }

    @Transactional
    def onDelete() {
        String tenantId = controllerSession['tenantId']
        Class domainClass = controllerSession['domainClass']

        tenantService.withTenant(tenantId) {
            try {
                def obj = domainClass.get(params.id)
                obj.delete(flush: true)
                display action: 'index'

            } catch (e) {
                display exception: e
            }
        }
    }

    def sqlConsole() {
        def c = createContent(ContentForm)

        c.header.nextButton.action = 'onExecuteSql'
        c.header.nextButton.text = TextDefault.EXECUTE
        c.header.nextButton.icon = 'fa-play'

        c.form.with {
            addField(
                    class: Select,
                    id: 'connectionSource',
                    optionsFromRecordset: connectionSourceService.list(),
                    keys: ['name'],
                    allowClear: false,
                    defaultValue: tenantService.defaultTenantId,
            )
            addField(
                    class: Textarea,
                    id: 'sql',
                    rows: 4,
            )
        }

        display content: c, modal: true, large: true
    }

    @Transactional
    def onExecuteSql() {
        def dataSource = connectionSourceService.getDataSource(params.connectionSource)
        def sql = params.sql

        try {
            SqlUtils.execute(dataSource, sql)
            display message: 'gormExplorer.sql.console.execution.success'

        } catch (Exception e) {
            display exception: e
        }
    }

}
