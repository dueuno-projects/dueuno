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
package dueuno.security

import dueuno.application.ApplicationPropertyService
import dueuno.application.ApplicationService
import dueuno.elements.core.PrettyPrinterDecimalFormat
import dueuno.elements.ElementsController
import dueuno.elements.style.GuiStyle
import dueuno.elements.components.Separator
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentForm
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.*
import dueuno.elements.style.TextDefault
import dueuno.tenant.TenantPropertyService
import dueuno.tenant.TenantService
import grails.plugin.springsecurity.annotation.Secured

/**
 * Users Management
 *
 * @author Gianluca Sartori
 */
@Secured(['ROLE_SECURITY'])
class UserController implements ElementsController {

    ApplicationService applicationService
    SecurityService securityService
    TenantService tenantService
    ApplicationPropertyService applicationPropertyService
    TenantPropertyService tenantPropertyService

    def index() {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        List cols = ['systemIcon']
        if (isSuperAdmin) cols += ['tenant']
        cols += [
                'adminIcon',
                'username',
                'fullname',
                'defaultGroup',
                'apiKey',
                'externalId',
                'enabled',
        ]

        def c = createContent(ContentTable)

        c.header.removeBackButton()

        c.table.with {
            filters.with {
                fold = false
                if (isSuperAdmin) {
                    addField(
                            class: Select,
                            id: 'tenant',
                            optionsFromRecordset: tenantService.list(),
                            cols: 2,
                    )
                }
                addField(
                        class: TextField,
                        id: 'find',
                        label: TextDefault.FIND,
                        cols: isSuperAdmin ? 8 : 10,
                )
                addField(
                        class: Select,
                        id: 'enabled',
                        options: [true: 'enabled', false: 'disabled'],
                        noSelection: true,
                        search: false,
                        cols: 2,
                )
            }
            sortable = [
                    tenant: 'asc',
                    username: 'asc',
            ]
            keys = ['username']
            columns = cols
            labels = [
                    systemIcon: '',
                    adminIcon: '',
            ]

            body.eachRow { TableRow row, Map values ->
                values.admin = securityService.isAdmin(values.username)
                values.system = !values.deletable
                if (values.system) {
                    row.actions.removeTailAction()
                    row.cells.systemIcon.icon = 'fa-gear'
                    row.cells.systemIcon.tooltip = 'user.tooltip.system'
                }

                if (values.admin) {
                    row.cells.adminIcon.icon = 'fa-house-user'
                    row.cells.adminIcon.tooltip = 'user.tooltip.admin'
                } else if (values.username == securityService.USERNAME_SUPERADMIN) {
                    row.cells.adminIcon.icon = 'fa-screwdriver-wrench'
                    row.cells.adminIcon.tooltip = 'user.tooltip.superadmin'

                }

                if (isSuperAdmin) {
                    row.cells.tenant.tag = true
                }

                values.externalId = !!values.externalId
                row.cells.apiKey.tag = true
            }
        }

        def filters = c.table.filterParams
        c.table.body = securityService.listAllUser(filters, params)
        c.table.paginate = securityService.countAllUser(filters)

        display content: c
    }

    private buildForm(TUser obj = null) {
        def c = obj
                ? createContent(ContentEdit)
                : createContent(ContentCreate)

        def mustRefresh = c in ContentCreate && controllerSession.createAndNew
        if (mustRefresh) {
            c.header.addBackButton(
                    action: 'index',
                    icon: 'fa-times',
                    text: '',
            )
        }

        def isCreatingNewUser = !obj
        def isSuperAdmin = securityService.isSuperAdmin()
        def isEditingUserButNotSuperAdmin = obj && obj.username != securityService.USERNAME_SUPERADMIN
        def isTenantAdmin = obj && securityService.isAdmin(obj) && !obj.deletable

        c.form.with {
            validate = TUser

            if (isSuperAdmin && (isCreatingNewUser || isEditingUserButNotSuperAdmin)) {
                addField(
                        class: Select,
                        id: 'tenant',
                        optionsFromRecordset: tenantService.list(),
                        defaultValue: tenantService.defaultTenant.id,
                        search: false,
                        noSelection: false,
                        onLoad: 'onTenantChange',
                        onChange: 'onTenantChange',
                        submit: ['form'],
                        readonly: isTenantAdmin,
                )
            }

            addField(
                    class: TextField,
                    id: 'username',
                    icon: 'fa-user',
                    cols: 6,
            )
            PasswordField password = addField(
                    class: PasswordField,
                    id: 'password',
                    nullable: obj,
                    cols: 6,
            ).component
            password.addAction(action: 'onGeneratePassword', submit: ['form'], tooltip: 'user.generatePassword', text: '', icon: 'fa-key')

        }

        buildSensitiveDataForm(c)

        if (isCreatingNewUser || isEditingUserButNotSuperAdmin) {
            c.form.with {
                addField(
                        class: Separator,
                        id: 'authorizations',
                        icon: 'fa-shield-halved',
                )
                addField(
                        class: Select,
                        id: 'groups',
                        optionsFromRecordset: securityService.listAllGroup(hideUsers: true),
                        search: false,
                        multiple: true,
                )
                addField(
                        class: Select,
                        id: 'defaultGroup',
                        optionsFromRecordset: securityService.listAllGroup(),
                        search: false,
                )
                addField(
                        class: NumberField,
                        id: 'sessionDuration',
                        prefix: 'min',
                        decimals: 0,
                        defaultValue: tenantPropertyService.getNumber('SESSION_DEFAULT_DURATION'),
                        cols: 6,
                )
                addField(
                        class: NumberField,
                        id: 'rememberMeDuration',
                        prefix: 'min',
                        decimals: 0,
                        defaultValue: tenantPropertyService.getNumber('REMEMBER_ME_DEFAULT_DURATION'),
                        cols: 6,
                )
                addField(
                        class: Checkbox,
                        id: 'admin',
                        cols: 6,
                )
                addField(
                        class: Checkbox,
                        id: 'enabled',
                        defaultValue: true,
                        nullable: true,
                        cols: 6,
                )
                addField(
                        class: Separator,
                        id: 'integration',
                        icon: 'fa-plug',
                )
                TextField apiKey = addField(
                        class: TextField,
                        id: 'apiKey',
                        icon: 'fa-lock',
                        readonly: !securityService.isDeveloper() && !securityService.isAdmin(),
                ).component
                apiKey.addAction(action: 'onGenerateApiKey', submit: ['form'], text: 'user.generateApiKey', icon: 'fa-key')
                addField(
                        class: TextField,
                        id: 'externalId',
                        icon: 'fa-barcode',
                )
            }
        }

        buildPreferencesForm(c)

        c.form['language'].defaultValue = applicationPropertyService.getString('DEFAULT_LANGUAGE')

        return c
    }

    private buildSensitiveDataForm(ContentForm c) {
        c.form.with {
            addField(
                    class: TextField,
                    id: 'firstname',
                    cols: 6,
            )
            addField(
                    class: TextField,
                    id: 'lastname',
                    cols: 6,
            )
            addField(
                    class: EmailField,
                    id: 'email',
                    cols: 6,
            )
            addField(
                    class: TelephoneField,
                    id: 'telephone',
                    cols: 6,
            )
            addField(
                    class: Textarea,
                    id: 'note',
                    maxSize: 2000,
                    rows: 2,
            )
        }
    }

    private buildPreferencesForm(ContentForm c) {
        c.form.with {
            addField(
                    class: Separator,
                    id: 'preferences',
                    icon: 'fa-earth-americas',
            )
            addField(
                    class: Select,
                    id: 'language',
                    optionsFromList: applicationService.languages,
                    textPrefix: 'default.language',
                    search: false,
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'firstDaySunday',
                    options: [false: 'false', true: 'true'],
                    textPrefix: 'default.firstDaySunday',
                    search: false,
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'invertedMonth',
                    options: [false: 'false', true: 'true'],
                    textPrefix: 'default.invertedMonth',
                    search: false,
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'twelveHours',
                    options: [false: 'false', true: 'true'],
                    textPrefix: 'default.twelveHours',
                    search: false,
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'decimalFormat',
                    optionsFromEnum: PrettyPrinterDecimalFormat,
                    textPrefix: 'default.decimalFormat',
                    search: false,
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'prefixedUnit',
                    options: [false: 'false', true: 'true'],
                    textPrefix: 'default.prefixedUnit',
                    search: false,
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'symbolicCurrency',
                    options: [false: 'false', true: 'true'],
                    textPrefix: 'default.symbolicCurrency',
                    search: false,
                    cols: 6,
            )
            addField(
                    class: Select,
                    id: 'symbolicQuantity',
                    options: [false: 'false', true: 'true'],
                    textPrefix: 'default.symbolicQuantity',
                    search: false,
                    cols: 6,
            )

            addField(
                    class: Separator,
                    id: 'appearance',
                    icon: 'fa-circle-half-stroke',
            )
            if (securityService.isDeveloper()) {
                addField(
                        class: NumberField,
                        id: 'fontSize',
                        defaultValue: 14,
                        cols: 4,
                        colsSmall: 6,
                )
            } else {
                addField(
                        class: Select,
                        id: 'fontSize',
                        optionsFromList: [12, 13, 14, 15, 16, 17, 18, 19, 20, 21],
                        defaultValue: 14,
                        renderTextPrefix: false,
                        cols: 4,
                        colsSmall: 6,
                )
            }
            addField(
                    class: Select,
                    id: 'guiStyle',
                    optionsFromEnum: GuiStyle,
                    textPrefix: 'default',
                    cols: 4,
                    colsSmall: 6,
            )
            addField(
                    class: Checkbox,
                    id: 'animations',
                    defaultValue: true,
                    cols: 4,
            )
        }
    }

    def onGeneratePassword() {
        def password = securityService.generatePassword()
        def t = createTransition()
        t.set('password', 'showPassword', true)
        t.setValue('password', password)
        display transition: t
    }

    def onGenerateApiKey() {
        def apiKey = securityService.generateApiKey()
        def t = createTransition()
        t.setValue('apiKey', apiKey)
        display transition: t
    }

    def onTenantChange() {
        def rs = securityService.listAllGroup(tenant: params.tenant, hideUsers: true)
        def tenantGroups = Select.optionsFromRecordset(recordset: rs)

        def t = createTransition()
        t.set('defaultGroup', 'options', tenantGroups)
        t.set('groups', 'options', tenantGroups)

        def user = securityService.getUserByUsername(params.username)
        if (user) {
            t.setValue('defaultGroup',  user.defaultGroup?.id)
            t.setValue('groups',  user.authorities*.id)

        } else {
            def tenant = tenantService.get(params.tenant)
            if (tenant) {
                tenantService.withTenant(tenant.tenantId) {
                    def sessionDuration = tenantPropertyService.getNumber('SESSION_DEFAULT_DURATION')
                    def rememberMeDuration = tenantPropertyService.getNumber('REMEMBER_ME_DEFAULT_DURATION')
                    t.setValue('sessionDuration',  sessionDuration)
                    t.setValue('rememberMeDuration',  rememberMeDuration)
                }
            }
        }

        display transition: t
    }

    def create() {
        def c = buildForm()

        c.header.nextButton.action = 'onCreateAndClose'
        c.header.nextButton.text = 'user.onCreateAndClose'
        c.header.nextButton.icon = ''
        c.header.nextButton.addAction(action: 'onCreate', submit: ['form'])
        if (controllerSession.createAndNew) {
            c.header.nextButton.setDefaultAction(action: 'onCreate')
        } else {
            c.header.nextButton.setDefaultAction(action: 'onCreateAndClose')
        }

        def mustRefresh = c in ContentCreate && controllerSession.createAndNew
        display content: c, modal: true, closeButton: !mustRefresh
    }

    private normalizeInput(Map params) {
        params.tenant = tenantService.get(params.tenant) ?: tenantService.currentTenant

        List groups = []
        for (groupId in params.groups) {
            groups.add(securityService.getGroup(groupId).name)
        }
        params.groups = groups

        def defaultGroup = securityService.getGroup(params.defaultGroup)
        if (defaultGroup) params.defaultGroup = defaultGroup.name
    }

    def onCreateAndClose() {
        controllerSession.createAndNew = false
        normalizeInput(params)
        def obj = securityService.createUser(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        display action: 'index'
    }

    def onCreate() {
        controllerSession.createAndNew = true
        normalizeInput(params)
        def obj = securityService.createUser(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        display action: 'create'
    }

    def edit() {
        def user = securityService.getUserByUsername(params.username)
        def c = buildForm(user)

        def isNotSuperAdmin = !securityService.isSuperAdmin()
        def isEditingSuperAdminUser = user.username == securityService.USERNAME_SUPERADMIN
        if (isNotSuperAdmin || isEditingSuperAdminUser) {
            c.form['username'].readonly = true
            c.form['usernameField'].help = 'user.edit.username.help'
        }

        c.form.values = user
        c.form['password'].value = null

        if (!isEditingSuperAdminUser) {
            c.form['admin'].value = (user.authorities.find { it.name == SecurityService.GROUP_ADMINS } != null)
            c.form['admin'].readonly = !user.deletable
            c.form['enabled'].readonly = !user.deletable
            c.form['groups'].value = user.authorities.collect { it.id }
        }

        display content: c, modal: true
    }

    def onEdit() {
        normalizeInput(params)
        def obj = securityService.updateUserAndGroups(params)

        if (obj.hasErrors()) {
            display errors: obj

        } else {
            display action: 'index'
        }
    }

    def onDelete() {
        try {
            securityService.deleteUser(params.username)
            display action: 'index'

        } catch (e) {
            e.printStackTrace()
            display exception: e
        }
    }
}
