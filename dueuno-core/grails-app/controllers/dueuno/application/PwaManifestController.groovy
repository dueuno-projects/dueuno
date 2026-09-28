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

import dueuno.elements.ElementsController
import dueuno.tenant.TenantPropertyService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.transform.CompileStatic

@CompileStatic
@Secured(['permitAll'])
class PwaManifestController implements ElementsController {

    TenantPropertyService tenantPropertyService

    def index() {
        String appname = message('app.name')
        String appicon = tenantPropertyService.getString('APPICON')
        String contextPath = servletContext.contextPath ?: '/'

        response.contentType = 'application/manifest+json'
        Map manifest = [
            name      : appname,
            short_name: appname,
            id        : contextPath,
            start_url : contextPath,
            display   : 'standalone',
            icons     : [
                [src: appicon, sizes: '192x192', type: 'image/png'],
                [src: appicon, sizes: '512x512', type: 'image/png']
            ]
        ]

        render manifest as JSON
    }

}
