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
package dueuno.elements.pages

import dueuno.application.ApplicationService
import dueuno.elements.ElementsController
import dueuno.elements.components.Label
import dueuno.elements.contents.ContentForm
import dueuno.elements.style.TextAlign
import dueuno.security.SecurityService
import dueuno.tenant.TenantPropertyService
import grails.plugin.springsecurity.annotation.Secured

/**
 * The Shell
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@Secured(['isAuthenticated()'])
class ShellController implements ElementsController {

    ApplicationService applicationService
    TenantPropertyService tenantPropertyService
    ShellService shellService
    SecurityService securityService

    def index() {
        Shell shell = shellService.shell
        shell.content = shell.home
        display page: shell
    }

    def credits() {
        def c = createContent(ContentForm)
        c.header.removeNextButton()

        String logoImage = tenantPropertyService.getString('LOGIN_LOGO')

        c.form.with {
            addField(
                class: Label,
                id: 'logo',
                html: """<img width="50%" src="${logoImage}" />""",
                textAlign: TextAlign.CENTER,
                displayLabel: false,
            )
            addField(
                class: Label,
                id: 'title',
                html: """<div class="m-4">${message('app.credits.text')}</div>""",
                textAlign: TextAlign.CENTER,
                displayLabel: false,
            )

            Integer i = 0
            for (item in applicationService.credits) {
                addField(
                    class: Label,
                    id: "credits${i}",
                    html: """${item.key}<br/><strong>${item.value.join('</strong><br/><strong>')}</strong>""",
                    textAlign: TextAlign.CENTER,
                    label: '',
                )
                i++
            }
        }

        display content: c, modal: true
    }

    @Secured(['permitAll'])
    def toggleDevHints() {
        devDisplayHints = !devDisplayHints
        display controller: securityService.userLandingPage ?: 'shell', direct: true
    }

    @Secured(['permitAll'])
    def toggleClientLogs() {
        Boolean logs = tenantPropertyService.getBoolean('LOG_ERROR')
        tenantPropertyService.setBoolean('LOG_ERROR', !logs)
        tenantPropertyService.setBoolean('LOG_DEBUG', !logs)
        display controller: securityService.userLandingPage ?: 'shell', direct: true
    }

    @Secured(['permitAll'])
    def switchLanguage() {
        currentLanguage = (String) params.id
        securityService.saveCurrentUserLanguage()
        display controller: securityService.userLandingPage ?: 'shell', direct: true
    }
}
