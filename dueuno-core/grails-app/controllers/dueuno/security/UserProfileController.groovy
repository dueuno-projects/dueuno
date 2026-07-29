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

import dueuno.application.ApplicationService
import dueuno.elements.core.PrettyPrinterDecimalFormat
import dueuno.elements.ElementsController
import dueuno.elements.style.GuiStyle
import dueuno.elements.components.Separator
import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.*
import dueuno.elements.style.TextDefault
import dueuno.tenant.TenantPropertyService
import grails.plugin.springsecurity.annotation.Secured

/**
 * User profile
 *
 * @author Gianluca Sartori
 */
@Secured(['isAuthenticated()'])
class UserProfileController implements ElementsController {

    ApplicationService applicationService
    TenantPropertyService tenantPropertyService
    SecurityService securityService

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
                    colsSmall: 6,
            )
            addField(
                    class: Select,
                    id: 'twelveHours',
                    options: [false: 'false', true: 'true'],
                    textPrefix: 'default.twelveHours',
                    search: false,
                    cols: 6,
                    colsSmall: 6,
            )
            addField(
                    class: Select,
                    id: 'decimalFormat',
                    optionsFromEnum: PrettyPrinterDecimalFormat,
                    textPrefix: 'default.decimalFormat',
                    search: false,
                    cols: 6,
                    colsSmall: 6,
            )
            addField(
                    class: Select,
                    id: 'prefixedUnit',
                    options: [false: 'false', true: 'true'],
                    textPrefix: 'default.prefixedUnit',
                    search: false,
                    cols: 6,
                    colsSmall: 6,
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
            addField(
                    class: Select,
                    id: 'fontSize',
                    optionsFromList: [12, 13, 14, 15, 16, 17, 18, 19, 20, 21],
                    defaultValue: 14,
                    renderTextPrefix: false,
                    cols: 4,
                    colsSmall: 6,
            )
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
                    cols: 4,
            )
        }
    }

    def index() {
        def c = createContent(ContentForm)
        c.header.with {
            nextButton.action = 'onSave'
            nextButton.text = TextDefault.SAVE
            removeBackButton()
        }

        c.form.with {
            if (devDisplayHints) {
                addField(
                        class: Textarea,
                        id: 'authorities',
                        text: '',
                        readonly: true,
                        cols: 12,
                )
            }
            addField(
                    class: TextField,
                    id: 'username',
                    readonly: true,
                    icon: 'fa-user',
                    help: 'userProfile.edit.username.help',
                    cols: 12,
            )
        }

        buildSensitiveDataForm(c)

        buildPreferencesForm(c)

        def canChangePassword = tenantPropertyService.getBoolean('USER_CAN_CHANGE_PASSWORD')
        if (canChangePassword) {
            c.form.with {
                addField(
                        class: Separator,
                        id: 'change.password',
                        icon: 'fa-key',
                )
                addField(
                        class: PasswordField,
                        id: 'newPassword',
                        cols: 6,
                )
                addField(
                        class: PasswordField,
                        id: 'confirmNewPassword',
                        cols: 6,
                )
            }

        }

        def obj = securityService.getCurrentUser(true)
        if (obj) {
            c.form.values = obj
        }

        if (devDisplayHints) {
            c.form['authorities'].value = securityService.getCurrentUserAuthorities().join(',\n') + '.'
        }

        display content: c, modal: true
    }

    def onSave(UserProfileValidator val) {
        if (val.hasErrors()) {
            display errors: val
            return
        }

        if (params.newPassword) {
            params.password = params.newPassword
        }

        securityService.updateUser(params)
        securityService.getCurrentUser(true) // We reload the 'currentUser'

        currentLanguage = params.language
        decimalFormat = params.decimalFormat
        prefixedUnit = (params.prefixedUnit == 'false') ? false : true
        symbolicCurrency = (params.symbolicCurrency == 'false') ? false : true
        symbolicQuantity = (params.symbolicQuantity == 'false') ? false : true
        invertedMonth = (params.invertedMonth == 'false') ? false : true
        twelveHours = (params.twelveHours == 'false') ? false : true
        firstDaySunday = (params.firstDaySunday == 'false') ? false : true
        fontSize = params.fontSize as Integer
        guiStyle = params.guiStyle as String
        animations = params.animations as Boolean

        display controller: securityService.userLandingPage ?: 'shell', direct: true
    }
}
