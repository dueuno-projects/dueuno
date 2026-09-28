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
package dueuno.servlet

import grails.artefact.Controller
import grails.util.Holders
import groovy.util.logging.Slf4j
import jakarta.servlet.Servlet

//
// See: http://stackoverflow.com/questions/7956661/servlet-running-under-grails
//
/**
 * @author Gianluca Sartori
 */
@Slf4j
class ServletController implements Controller {

    static defaultAction = 'service'

    def service(String servletName) {
        if (!servletName) {
            log.info "Cannot find a servlet called ${servletName}."
            render status: '404'
            return
        }

        Servlet servlet
        try {
            servlet = (Servlet) Holders.applicationContext.getBean(servletName)

        } catch (Exception ignore) {
            log.info "Cannot find a servlet called ${servletName}."
            render status: '404'
            return
        }

        servlet.service(request, response)
    }

}
