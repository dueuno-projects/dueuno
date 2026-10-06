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
package dueuno.application

import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.Checkbox
import dueuno.elements.controls.PasswordField
import dueuno.elements.controls.TextField
import grails.plugin.springsecurity.annotation.Secured

/**
 * Application Management for authentication provider configuration.
 */
@Secured(['ROLE_SUPERADMIN'])
class AuthenticationProviderController implements ElementsController {

    AuthenticationProviderService authenticationProviderService

    def index() {
        def c = createContent(ContentTable)
        c.header.removeNextButton()
        c.table.with {
            columns = [
                'sequence',
                'name',
                'enabled',
            ]
            body.eachRow { TableRow row, Map values ->
                row.actions.removeTailAction()
            }
            body = authenticationProviderService.list()
        }
        display content: c
    }

    private buildForm(TAuthenticationProvider obj) {
        def c = createContent(ContentEdit)
        c.form.with {
            validate = TAuthenticationProvider
            addField(
                class: TextField,
                id: 'name',
                readonly: true,
                cols: 8,
            )
            addField(
                class: TextField,
                id: 'sequence',
                readonly: true,
                cols: 4,
            )
            addField(
                class: Checkbox,
                id: 'enabled',
                readonly: obj.providerKey == 'embedded',
                cols: 12,
            )

            if (obj.providerKey == 'ldap') {
                addField(class: TextField, id: 'server', cols: 12)
                addField(class: TextField, id: 'managerDn', cols: 6)
                addField(class: PasswordField, id: 'managerPassword', cols: 6)
                addField(class: TextField, id: 'searchBase', cols: 12)
                addField(class: TextField, id: 'searchFilter', cols: 8)
                addField(class: Checkbox, id: 'searchSubtree', cols: 4)
                addField(class: Checkbox, id: 'retrieveGroupRoles', cols: 4)
                addField(class: TextField, id: 'groupSearchBase', cols: 8)
                addField(class: TextField, id: 'groupSearchFilter', cols: 6)
                addField(class: TextField, id: 'groupRoleAttribute', cols: 6)
                addField(class: Checkbox, id: 'retrieveDatabaseRoles', cols: 6)
                addField(class: TextField, id: 'defaultRole', cols: 6)
            }

            if (!(obj.providerKey in ['embedded', 'physical', 'ldap', 'rememberMe'])) {
                addField(class: TextField, id: 'clientId', cols: 6)
                addField(class: PasswordField, id: 'clientSecret', cols: 6)
                addField(class: TextField, id: 'issuerUri', cols: 12)
                addField(class: TextField, id: 'authorizationUri', cols: 12)
                addField(class: TextField, id: 'tokenUri', cols: 12)
                addField(class: TextField, id: 'userInfoUri', cols: 12)
                addField(class: TextField, id: 'redirectUri', cols: 12)
                addField(class: TextField, id: 'scopes', cols: 12)
            }
        }

        Map values = obj.properties as Map
        values.managerPassword = ''
        values.clientSecret = ''
        c.form.values = values
        return c
    }

    def edit() {
        TAuthenticationProvider obj = authenticationProviderService.get(params.id as Serializable)
        if (!obj) {
            display action: 'index'
            return
        }
        display content: buildForm(obj), modal: true
    }

    def onEdit() {
        TAuthenticationProvider obj = authenticationProviderService.update(params)
        if (!obj || obj.hasErrors()) {
            display errors: obj
        } else {
            display action: 'index'
        }
    }

}
