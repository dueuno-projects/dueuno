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

import dueuno.elements.PageService
import dueuno.elements.pages.ShellService
import dueuno.security.CryptoService
import dueuno.security.SecurityService
import dueuno.tenant.TenantPropertyService
import dueuno.types.Money
import dueuno.types.Quantity
import dueuno.types.Types
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 */

@CompileStatic
class BootStrap {

//    GroovyPagesTemplateEngine groovyPagesTemplateEngine
    ApplicationService applicationService
    SecurityService securityService
    CryptoService cryptoService
    TenantPropertyService tenantPropertyService
    PageService pageService
    ShellService shellService

    def init = {

        applicationService.onPluginInstall {
            applicationPropertyService.install()
            tenantService.install()
            securityService.install()
        }

        applicationService.onPluginTenantInstall { String tenantId ->
            securityService.tenantInstall()
            cryptoService.tenantInstall()
            tenantPropertyService.tenantInstall()
            pageService.tenantInstall()
            shellService.tenantInstall()
        }

        applicationService.beforeInit {
            securityService.init()

            // It works only from the 'app-test' within this project
            // not working as application dependency when compiled as plugin
//            groovyPagesTemplateEngine.groovyPageSourceDecorators = [new PageWhitespacesStripper() as GroovyPageSourceDecorator]

            Types.register(Money)
            Types.register(Quantity)
        }

        applicationService.afterInit {
            applicationPropertyService.validateAll()
            securityService.registerFeatures()
        }

        applicationService.beforeTenantInit { String tenantId ->
            cryptoService.tenantInit()
        }

        applicationService.afterTenantInit { String tenantId ->
            tenantPropertyService.validateAll()
        }

    }

    def destroy = {
        //no-op
    }

}
