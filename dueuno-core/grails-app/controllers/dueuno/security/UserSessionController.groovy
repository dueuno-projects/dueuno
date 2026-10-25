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

import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.components.Timer
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.Select
import dueuno.tenant.TenantService
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j

/**
 * Management of authenticated user sessions.
 */
@Secured(['ROLE_SECURITY'])
@Slf4j
class UserSessionController implements ElementsController {

    UserSessionService userSessionService
    SecurityService securityService
    TenantService tenantService

    def index() {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        List tableColumns = []
        if (isSuperAdmin) tableColumns += ['tenant']
        tableColumns += ['username', 'firstname', 'lastname', 'lastAccessedTime']

        def c = createContent(ContentTable)
        c.header.removeNextButton()
        c.addComponent(Timer, 'refresh', [
            interval          : 10000,
            action            : 'index',
            executeImmediately: false,
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
                        cols: 4,
                    )
                }
                actionbar.with {
                    addAction(
                        action: 'index',
                        icon: 'fa-rotate',
                        text: '',
                        tooltip: 'default.text.refresh',
                    )
                }
            }
            columns = tableColumns
            keys = ['id']
            labels = [
                tenant   : 'user.tenant',
                username : 'user.username',
                firstname: 'user.firstname',
                lastname : 'user.lastname',
            ]
            actions.removeAllActions()
            actions.addDefaultAction(
                action: 'onDisconnect',
                text: 'userSession.disconnect',
                icon: 'fa-right-from-bracket',
                confirmMessage: 'userSession.disconnect.confirm',
            )
            String tenantId = tenantService.currentTenantId
            if (isSuperAdmin && filterParams.tenant) {
                tenantId = tenantService.get(filterParams.tenant as Serializable)?.tenantId
            } else if (isSuperAdmin) {
                tenantId = null
            }
            body = userSessionService.list(tenantId, isSuperAdmin)
            body.eachRow { TableRow row, Map values ->
                row.removeSelection()
            }
        }

        display content: c, modal: true, closeButton: true
    }

    def onDisconnect() {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        String tenantId = isSuperAdmin ? null : tenantService.currentTenantId
        userSessionService.disconnect(params.id as String, tenantId, isSuperAdmin)
        display action: 'index'
    }
}
