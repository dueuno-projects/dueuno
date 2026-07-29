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
package dueuno.elements

import dueuno.application.ApplicationPropertyService
import dueuno.tenant.TenantService
import grails.gorm.multitenancy.CurrentTenant
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j

/**
 * Resource downloading
 *
 * @author Gianluca Sartori
 */
@Slf4j
class DownloadResourceController implements ElementsController {

    ApplicationPropertyService applicationPropertyService
    TenantService tenantService

    private Boolean isValidPathname(String pathname) {
        return !pathname.contains('..')
    }

    @Secured(['permitAll'])
    def system() {
        String root = applicationPropertyService.getDirectory(params.root)
        String pathname = params.pathname
        String resource =  root + pathname

        if (!isValidPathname(pathname)) {
            render status: 404
            return
        }

        log.trace "Downloading resource '${resource}'"
        download root + pathname
    }

    @Secured(['permitAll'])
    def tenant() {
        String tenantId = params.tenantId
        String root = tenantService.getPublicDir(tenantId)
        String pathname = params.pathname
        String resource =  root + pathname

        if (!isValidPathname(pathname)) {
            render status: 404
            return
        }

        log.trace "Downloading resource '${resource}'"
        download resource
    }

    @CurrentTenant
    def currentTenant() {
        String root = tenantService.publicDir
        String pathname = params.pathname
        String resource =  root + pathname

        if (!isValidPathname(pathname)) {
            render status: 404
            return
        }

        log.trace "Downloading resource '${resource}'"
        download resource
    }
}
