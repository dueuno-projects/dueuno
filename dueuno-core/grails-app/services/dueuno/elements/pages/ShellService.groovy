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

import dueuno.application.ApplicationPropertyService
import dueuno.application.ApplicationService
import dueuno.application.LinkGeneratorAware
import dueuno.application.WebRequestAware
import dueuno.commons.utils.LogUtils
import dueuno.elements.PageService
import dueuno.exceptions.ElementsException
import dueuno.tenant.TenantPropertyService
import dueuno.tenant.TenantService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class ShellService implements WebRequestAware, LinkGeneratorAware {

    ApplicationService applicationService
    TenantService tenantService
    ApplicationPropertyService applicationPropertyService
    TenantPropertyService tenantPropertyService
    PageService pageService

    void tenantInstall() {
        String tenantId = tenantService.currentTenantId
        tenantPropertyService.setString('LOGO', linkPublicResource(tenantId, '/brand/logo.png', false))
    }

    Shell getShell() {
        Shell shell = pageService.mainPage as Shell
        return shell ?: createShell()
    }

    private Shell createShell() {
        try {
            ShellConfig config = readConfig()
            Map shellArgs = [config: config]
            Shell newShell = pageService.createPage(Shell, shellArgs)
            pageService.mainPage = newShell
            return newShell

        } catch (Exception e) {
            log.error LogUtils.logStackTrace(e)
            throw new ElementsException("Cannot create the shell: ${e.message}", e)
        }
    }

    private ShellConfig readConfig() {
        ShellConfig shellConfig = new ShellConfig()

        // These are not cloned, we will have direct access to the servletContext objects
        shellConfig.features.user = applicationService.userFeatures
        shellConfig.features.main = applicationService.mainFeatures

        // System properties
        shellConfig.display.menu = applicationPropertyService.getBoolean('DISPLAY_MENU', true)
        shellConfig.display.menuSearch = applicationPropertyService.getBoolean('DISPLAY_MENU_SEARCH', true)
        shellConfig.display.homeButton = applicationPropertyService.getBoolean('DISPLAY_HOME_BUTTON', true)
        shellConfig.display.userMenu = applicationPropertyService.getBoolean('DISPLAY_USER_MENU', true)

        // Tenant properties
        shellConfig.display.logo = tenantPropertyService.getString('LOGO', true)

        return shellConfig
    }
}
