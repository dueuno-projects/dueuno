/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package dueuno.security

import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.Select
import dueuno.elements.controls.TextField
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j

@Slf4j
@Secured(['ROLE_ADMIN', 'ROLE_SUPERADMIN'])
class AuthenticationIdentityController implements ElementsController {

    AuthenticationIdentityService authenticationIdentityService
    SecurityService securityService

    def index() {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        TAuthenticationProvider provider = authenticationIdentityService.getProvider(params.providerId as Serializable, isSuperAdmin)
        if (!provider) {
            display controller: 'authenticationProvider', action: 'index'
            return
        }

        def c = createContent(ContentTable)
        c.header.nextButton.addParams([providerId: provider.id])
        c.table.with {
            keys = [
                'id',
            ]
            columns = [
                'user',
                'providerType',
                'issuer',
                'subject',
            ]
            labels = [
                user: 'authenticationIdentity.username',
            ]
            sortable = [
                issuer: 'asc',
                subject: 'asc',
            ]
            body.eachRow { TableRow row, Map values ->
                row.actions.addParams([
                    providerId: provider.id,
                ])
            }
            body = authenticationIdentityService.list(provider.id, isSuperAdmin)
        }

        display content: c, modal: true
    }

    private buildForm(TAuthenticationProvider provider, TUserAuthenticationIdentity obj = null) {
        def c = obj ? createContent(ContentEdit) : createContent(ContentCreate)

        c.header.addBackButton(action: 'index', params: [providerId: provider.id])
        c.header.nextButton.addParams([providerId: provider.id])
        c.form.with {
            validate = TUserAuthenticationIdentity
            addField(
                class: Select,
                id: 'user',
                optionsFromRecordset: authenticationIdentityService.listUsers(provider.id, securityService.isSuperAdmin()),
                search: true,
                cols: 12,
            )
            addField(
                class: Select,
                id: 'providerType',
                optionsFromEnum: AuthenticationProviderType,
                textPrefix: 'authenticationProvider.providerType',
                readonly: true,
                cols: 12,
            )
            addField(
                class: TextField,
                id: 'issuer',
                cols: 12,
            )
            addField(
                class: TextField,
                id: 'subject',
                cols: 12,
            )
        }

        c.form.values = obj ?: [providerType: provider.providerType]
        return c
    }

    def create() {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        TAuthenticationProvider provider = authenticationIdentityService.getProvider(params.providerId as Serializable, isSuperAdmin)
        if (!provider) {
            display controller: 'authenticationProvider', action: 'index'
            return
        }

        display content: buildForm(provider), modal: true
    }

    def onCreate() {
        TUserAuthenticationIdentity obj = authenticationIdentityService.create(
            params,
            params.providerId as Serializable,
            securityService.isSuperAdmin(),
        )

        if (!obj) {
            display controller: 'authenticationProvider', action: 'index'
        } else if (obj.hasErrors()) {
            display errors: obj
        } else {
            display action: 'index', params: [providerId: params.providerId]
        }
    }

    def edit() {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        TAuthenticationProvider provider = authenticationIdentityService.getProvider(params.providerId as Serializable, isSuperAdmin)

        TUserAuthenticationIdentity obj = authenticationIdentityService.get(
            params.id as Serializable,
            params.providerId as Serializable,
            isSuperAdmin,
        )
        if (!provider || !obj) {
            display action: 'index', params: [providerId: params.providerId]
            return
        }

        display content: buildForm(provider, obj), modal: true
    }

    def onEdit() {
        TUserAuthenticationIdentity obj = authenticationIdentityService.update(
            params,
            params.providerId as Serializable,
            securityService.isSuperAdmin(),
        )

        if (!obj) {
            display action: 'index', params: [providerId: params.providerId]
        } else if (obj.hasErrors()) {
            display errors: obj
        } else {
            display action: 'index', params: [providerId: params.providerId]
        }
    }

    def onDelete() {
        authenticationIdentityService.delete(
            params.id as Serializable,
            params.providerId as Serializable,
            securityService.isSuperAdmin(),
        )

        display action: 'index', params: [providerId: params.providerId]
    }

}
