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
        tableColumns += [
            'lastname',
            'firstname',
            'username',
            'userAgent',
            'loginIpAddress',
            'loginTime',
            'lastAccessedTime',
            'expirationTime',
            'rememberMeExpirationTime',
        ]

        def c = createContent(ContentTable)
        c.header.removeNextButton()
        c.header.addBackButton(
            action: 'index',
            icon: 'fa-rotate',
            text: '',
            tooltip: 'default.text.refresh',
        )
        c.table.with {
            filters.with {
                fold = false
                if (isSuperAdmin) {
                    addField(
                        class: Select,
                        id: 'tenant',
                        optionsFromRecordset: tenantService.list(),
                        cols: 3,
                    )
                }
            }
            columns = tableColumns
            keys = ['id']
            labels = [
                userAgent: '',
            ]
            prettyPrinterProperties = [
                loginTime: [
                    renderSeconds: true,
                ],
                lastAccessedTime: [
                    renderSeconds: true,
                ],
            ]
            actions.removeAllActions()
            actions.addDefaultAction(
                action: 'onDisconnect',
                text: '',
                icon: 'fa-right-from-bracket',
                tooltip: 'userSession.disconnect',
                confirmMessage: 'userSession.disconnect.confirm',
            )
            String tenantId = tenantService.currentTenantId
            if (isSuperAdmin && filterParams.tenant) {
                tenantId = tenantService.get(filterParams.tenant as Serializable)?.tenantId
            } else if (isSuperAdmin) {
                tenantId = null
            }
            body.eachRow { TableRow row, Map values ->
                row.cells.userAgent.icon = getBrowserIcon(values.userAgent)
                row.cells.userAgent.tooltip = values.userAgent
                values.userAgent = null
            }
            body = userSessionService.list(tenantId, isSuperAdmin)
        }

        display content: c, modal: true, large: true, closeButton: true
    }

    def onDisconnect() {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        String tenantId = isSuperAdmin ? null : tenantService.currentTenantId
        userSessionService.disconnect(params.id as String, tenantId, isSuperAdmin)
        display action: 'index'
    }

    private String getBrowserIcon(String userAgent) {
        if (!userAgent) return 'fa-solid fa-globe'

        if (userAgent.contains('Edg/') || userAgent.contains('EdgA/') || userAgent.contains('EdgiOS/') || userAgent.contains('Edge/')) {
            return 'fa-brands fa-edge'
        }
        if (userAgent.contains('OPR/') || userAgent.contains('OPiOS/') || userAgent.contains('Opera/')) {
            return 'fa-brands fa-opera'
        }
        if (userAgent.contains('FxiOS/') || userAgent.contains('Firefox/')) {
            return 'fa-brands fa-firefox-browser'
        }
        if (userAgent.contains('MSIE ') || userAgent.contains('Trident/')) {
            return 'fa-brands fa-internet-explorer'
        }
        if (userAgent.contains('SamsungBrowser/')) return 'fa-solid fa-globe'
        if (userAgent.contains('CriOS/') || userAgent.contains('Chrome/')) {
            return 'fa-brands fa-chrome'
        }
        if (userAgent.contains('Safari/')) return 'fa-brands fa-safari'

        return 'fa-solid fa-globe'
    }
}
