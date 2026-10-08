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

import dueuno.application.AuthenticationProviderService
import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.Checkbox
import dueuno.elements.controls.PasswordField
import dueuno.elements.controls.Select
import dueuno.elements.controls.TextField
import dueuno.tenant.TenantService
import grails.plugin.springsecurity.annotation.Secured

/**
 * Application Management for authentication provider configuration.
 */
@Secured(['ROLE_ADMIN', 'ROLE_SUPERADMIN'])
class AuthenticationProviderController implements ElementsController {

    AuthenticationProviderService authenticationProviderService
    SecurityService securityService
    TenantService tenantService

    def index() {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        def c = createContent(ContentTable)
        c.header.removeNextButton()
        List tableColumns = []
        if (isSuperAdmin) {
            tableColumns.add('tenant.tenantId')
        }

        tableColumns.addAll([
            'sequence',
            'providerType',
            'enabled',
        ])

        c.table.with {
            filters.with {
                fold = false

                if (isSuperAdmin) {
                    addField(
                        class: Select,
                        id: 'tenant',
                        optionsFromRecordset: tenantService.list(),
                        noSelection: true,
                        search: false,
                        cols: 3,
                    )
                }
            }

            columns = tableColumns
            labels = [
                providerType: 'authenticationProvider.providerType',
            ]
            prettyPrinterProperties = [
                providerType: [
                    textPrefix: 'authenticationProvider.providerType',
                ],
            ]
            sortable = [
                'tenant.tenantId': 'asc',
                sequence: 'asc',
            ]

            actions.removeTailAction()
            body.eachRow { TableRow row, Map values ->
                if ((values.providerType as AuthenticationProviderType).isOidcProvider()) {
                    row.actions.addTailAction(
                        controller: 'authenticationProviderIdentity',
                        icon: 'fa-users',
                        text: '',
                        tooltip: 'authenticationProvider.identities',
                    )
                    row.actions.addParams([
                        providerId: values.id,
                    ])
                }
            }

            body = isSuperAdmin
                ? authenticationProviderService.listAll(filterParams)
                : authenticationProviderService.list()
        }

        display content: c
    }

    private buildForm(TAuthenticationProvider obj) {
        def c = createContent(ContentEdit)

        def readonly = obj.providerType == AuthenticationProviderType.EMBEDDED
        if (readonly) {
            c.header.removeNextButton()
        }

        c.form.with {
            validate = TAuthenticationProvider
            addField(
                class: Select,
                id: 'tenant',
                optionsFromRecordset: securityService.isSuperAdmin() ? tenantService.list() : [obj.tenant],
                readonly: true,
                cols: 12,
            )
            addField(
                class: Select,
                id: 'providerType',
                optionsFromEnum: AuthenticationProviderType,
                textPrefix: 'authenticationProvider.providerType',
                readonly: true,
                cols: 9,
            )
            addField(
                class: TextField,
                id: 'sequence',
                readonly: true,
                cols: 3,
            )
            addField(
                class: Checkbox,
                id: 'enabled',
                readonly: readonly,
                cols: 12,
            )

            if (obj.providerType == AuthenticationProviderType.LDAP) {
                addField(
                    class: TextField,
                    id: 'server',
                    cols: 12,
                )
                addField(
                    class: TextField,
                    id: 'managerDn',
                    cols: 6,
                )
                addField(
                    class: PasswordField,
                    id: 'managerPassword',
                    cols: 6,
                )
                addField(
                    class: TextField,
                    id: 'searchBase',
                    cols: 12,
                )
                addField(
                    class: TextField,
                    id: 'searchFilter',
                    cols: 12,
                )
                addField(
                    class: Checkbox,
                    id: 'searchSubtree',
                    cols: 6,
                )
                addField(
                    class: Checkbox,
                    id: 'retrieveGroupRoles',
                    cols: 6,
                )
                addField(
                    class: TextField,
                    id: 'groupSearchBase',
                    cols: 4,
                )
                addField(
                    class: TextField,
                    id: 'groupSearchFilter',
                    cols: 4,
                )
                addField(
                    class: TextField,
                    id: 'groupRoleAttribute',
                    cols: 4,
                )
                addField(
                    class: Checkbox,
                    id: 'retrieveDatabaseRoles',
                    cols: 6,
                )
                addField(
                    class: TextField,
                    id: 'defaultRole',
                    cols: 6,
                )
            }

            if (obj.providerType.isOidcProvider()) {
                addField(
                    class: TextField,
                    id: 'clientId',
                    cols: 6,
                )
                addField(
                    class: PasswordField,
                    id: 'clientSecret',
                    cols: 6,
                )
                addField(
                    class: Select,
                    id: 'clientAuthenticationMethod',
                    cols: 12,
                    options: [
                        client_secret_basic: 'clientAuthenticationMethod.basic',
                        client_secret_post : 'clientAuthenticationMethod.post',
                    ],
                )
                addField(
                    class: TextField,
                    id: 'discoveryUri',
                    cols: 12,
                    help: 'authenticationProvider.discoveryUri.help',
                )
                addField(
                    class: TextField,
                    id: 'issuerUri',
                    cols: 12,
                )
                addField(
                    class: TextField,
                    id: 'authorizationUri',
                    cols: 12,
                )
                addField(
                    class: TextField,
                    id: 'tokenUri',
                    cols: 12,
                )
                addField(
                    class: TextField,
                    id: 'jwksUri',
                    cols: 12,
                )
                addField(
                    class: TextField,
                    id: 'userInfoUri',
                    cols: 12,
                )
                addField(
                    class: TextField,
                    id: 'redirectUri',
                    cols: 12,
                    help: 'authenticationProvider.redirectUri.help',
                )
                addField(
                    class: TextField,
                    id: 'scopes',
                    cols: 12,
                )
            }
        }

        c.form.values = obj
        return c
    }

    def edit() {
        TAuthenticationProvider obj = securityService.isSuperAdmin()
            ? authenticationProviderService.get(params.id as Serializable)
            : authenticationProviderService.getForCurrentTenant(params.id as Serializable)
        if (!obj) {
            display action: 'index'
            return
        }

        display content: buildForm(obj), modal: true
    }

    def onEdit() {
        TAuthenticationProvider obj = authenticationProviderService.update(params, securityService.isSuperAdmin())
        if (!obj || obj.hasErrors()) {
            display errors: obj
        } else {
            display action: 'index'
        }
    }

}
