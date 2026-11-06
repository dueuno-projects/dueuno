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
import dueuno.elements.controls.TextField
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
            'userAgentIcon',
            'osIcon',
            'deviceIcon',
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
                addField(
                    class: TextField,
                    id: 'search',
                    cols: 3,
                )
            }
            columns = tableColumns
            keys = ['id']
            labels = [
                userAgentIcon: '',
                osIcon: '',
                deviceIcon: '',
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
                row.cells.tenant.tag = true

                Map browserInfo = getBrowserInfo(values.userAgent as String)
                row.cells.userAgentIcon.icon = browserInfo.icon
                row.cells.userAgentIcon.tooltip = browserInfo.tooltip

                Map operatingSystemInfo = getOperatingSystemInfo(values.userAgent as String)
                row.cells.osIcon.icon = operatingSystemInfo.icon
                row.cells.osIcon.tooltip = operatingSystemInfo.tooltip

                if (values.isMobile) {
                    row.cells.deviceIcon.icon = 'fa-mobile-screen-button'
                    row.cells.deviceIcon.tooltip = 'userSession.device.mobile'
                } else {
                    row.cells.deviceIcon.icon = 'fa-desktop'
                    row.cells.deviceIcon.tooltip = 'userSession.device.desktop'
                }
            }
            body = userSessionService.list(tenantId, isSuperAdmin, filterParams.search as String)
        }

        display content: c, modal: true, large: true, closeButton: true
    }

    def onDisconnect() {
        Boolean isSuperAdmin = securityService.isSuperAdmin()
        String tenantId = isSuperAdmin ? null : tenantService.currentTenantId
        userSessionService.disconnect(params.id as String, tenantId, isSuperAdmin)
        display action: 'index'
    }

    private Map getBrowserInfo(String userAgent) {
        String browserName = 'Unknown browser'
        String browserVersion
        String icon = 'fa-solid fa-globe'

        if (userAgent) {
            def matcher = userAgent =~ /(?:EdgA?|EdgiOS|Edge)\/([\d.]+)/
            if (matcher.find()) {
                browserName = 'Microsoft Edge'
                browserVersion = matcher.group(1)
                icon = 'fa-brands fa-edge'
            } else if ((matcher = userAgent =~ /(?:OPR|OPiOS|Opera)\/([\d.]+)/).find()) {
                browserName = 'Opera'
                browserVersion = matcher.group(1)
                icon = 'fa-brands fa-opera'
            } else if ((matcher = userAgent =~ /(?:FxiOS|Firefox)\/([\d.]+)/).find()) {
                browserName = 'Firefox'
                browserVersion = matcher.group(1)
                icon = 'fa-brands fa-firefox-browser'
            } else if ((matcher = userAgent =~ /MSIE ([\d.]+)/).find()) {
                browserName = 'Internet Explorer'
                browserVersion = matcher.group(1)
                icon = 'fa-brands fa-internet-explorer'
            } else if (userAgent.contains('Trident/')) {
                matcher = userAgent =~ /rv:([\d.]+)/
                if (matcher.find()) {
                    browserName = 'Internet Explorer'
                    browserVersion = matcher.group(1)
                    icon = 'fa-brands fa-internet-explorer'
                }
            } else if ((matcher = userAgent =~ /SamsungBrowser\/([\d.]+)/).find()) {
                browserName = 'Samsung Internet'
                browserVersion = matcher.group(1)
            } else if ((matcher = userAgent =~ /(?:CriOS|Chrome)\/([\d.]+)/).find()) {
                browserName = 'Chrome'
                browserVersion = matcher.group(1)
                icon = 'fa-brands fa-chrome'
            } else if (userAgent.contains('Safari/')) {
                matcher = userAgent =~ /Version\/([\d.]+)/
                if (matcher.find()) {
                    browserName = 'Safari'
                    browserVersion = matcher.group(1)
                    icon = 'fa-brands fa-safari'
                }
            }
        }

        String tooltip = browserVersion ? "${browserName} ${browserVersion}" : browserName
        return [icon: icon, tooltip: tooltip]
    }

    private Map getOperatingSystemInfo(String userAgent) {
        String operatingSystemName = 'Unknown OS'
        String operatingSystemVersion
        String icon = 'fa-solid fa-gear'

        if (userAgent) {
            def matcher = userAgent =~ /(?:iPhone OS|CPU OS) ([\d_]+)/
            if (matcher.find()) {
                operatingSystemName = 'iOS'
                operatingSystemVersion = matcher.group(1).replace('_', '.')
                icon = 'fa-brands fa-apple fa-lg'
            } else if ((matcher = userAgent =~ /Android[\s\/]([\d.]+)/).find()) {
                operatingSystemName = 'Android'
                operatingSystemVersion = matcher.group(1)
                icon = 'fa-brands fa-android'
            } else if ((matcher = userAgent =~ /Windows NT ([\d.]+)/).find()) {
                String windowsVersion = matcher.group(1)
                operatingSystemName = [
                    '10.0': 'Windows 10/11',
                    '6.3' : 'Windows 8.1',
                    '6.2' : 'Windows 8',
                    '6.1' : 'Windows 7',
                    '6.0' : 'Windows Vista',
                    '5.1' : 'Windows XP',
                ].get(windowsVersion, "Windows ${windowsVersion}")
                icon = 'fa-brands fa-windows'
            } else if ((matcher = userAgent =~ /Mac OS X ([\d_]+)/).find()) {
                operatingSystemName = 'macOS'
                operatingSystemVersion = matcher.group(1).replace('_', '.')
                icon = 'fa-brands fa-apple fa-lg'
            } else if (userAgent.contains('Linux')) {
                operatingSystemName = 'Linux'
                icon = 'fa-brands fa-linux fa-lg'
            }
        }

        String tooltip = operatingSystemVersion ? "${operatingSystemName} ${operatingSystemVersion}" : operatingSystemName
        return [icon: icon, tooltip: tooltip]
    }
}
