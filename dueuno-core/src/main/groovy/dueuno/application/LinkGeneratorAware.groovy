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

import dueuno.elements.Elements
import grails.core.GrailsApplication
import grails.util.Holders
import grails.web.mapping.LinkGenerator
import groovy.transform.CompileStatic

/**
 * Helper trait to generate links from any class
 *
 * @author Gianluca Sartori
 */

@CompileStatic
trait LinkGeneratorAware {

    /**
     * Returns the absolute application URL (Eg. http://my.server.com/myapp)
     * The server URL will be automatically provided only when an HttpRequest is available, otherwise this method will return null.
     * In such cases you can configure `grails.serverUrl` in application.yml and this method will return that value.
     *
     * @return the absolute application URL (Eg. http://my.server.com/myapp)
     */
    String getBaseUrl() {
        LinkGenerator linkGenerator = Elements.getBean("grailsLinkGenerator") as LinkGenerator
        return linkGenerator.serverBaseURL
    }

    /**
     * Returns the servlet context as configured in application.yml
     * @return the servlet context as configured in application.yml
     */
    String getContextPath() {
        GrailsApplication grailsApplication = Holders.grailsApplication
        String contextPath = grailsApplication.config.getProperty('server.servlet.context-path') ?: ''
        return !contextPath || contextPath == '/' ? '' : contextPath
    }

    /**
     * Returns the relative URL to a static resource from a filesystem path defined in a System Property
     * @return the relative URL to a static resource from a filesystem path defined in a System Property
     *
     * @param root The name of the system property specifying an absolute path in the filesystem
     * @param pathname The name of the file to serve
     */
    String linkResource(String root, String pathname, Boolean reload = true) {
        return generateResourceLink('system', '', root, pathname, reload)
    }

    /**
     * Returns the relative URL to a static resource from a filesystem path defined in '${tenantId}_TENANT_PUBLIC_DIR'
     * @return the relative URL to a static resource from a filesystem path defined in '${tenantId}_TENANT_PUBLIC_DIR'
     *
     * @param pathname The name of the file to serve
     */
    String linkPublicResource(String pathname, Boolean reload = true) {
        return generateResourceLink('currentTenant', '', '', pathname, reload)
    }

    /**
     * Returns the relative URL to a static resource from a filesystem path defined in '${tenantId}_TENANT_PUBLIC_DIR'
     * @return the relative URL to a static resource from a filesystem path defined in '${tenantId}_TENANT_PUBLIC_DIR'
     *
     * @param tenantId The id of the tenant
     * @param pathname The name of the file to serve
     */
    String linkPublicResource(String tenantId, String pathname, Boolean reload = true) {
        return generateResourceLink('tenant', tenantId, '', pathname, reload)
    }

    private String generateResourceLink(String action, String tenantId, String root, String pathname, Boolean reload) {
        String request = "/downloadResource/${action}?tenantId=${tenantId}&root=${root}&pathname=${pathname}${reload ? '&' + System.currentTimeMillis() : '' }"
        return contextPath + request
    }
}
