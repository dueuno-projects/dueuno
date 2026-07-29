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

import dueuno.elements.core.WebRequestAware
import grails.artefact.Controller
import grails.plugin.springsecurity.annotation.Secured

/**
 * @author Gianluca Sartori
 */
@Secured(['isAuthenticated()'])
class SessionController implements Controller, WebRequestAware {

    def set() {
        String scope = params.scope
        String controller = params.targetController
        String action = params.targetAction
        String key = params.key
        String value = params.value

        switch (scope) {
            case 'ACTION':
                Map actionSession = getNamedSession(controller + '_' + action)
                actionSession[key] = value
                break

            case 'CONTROLLER':
                Map controllerSession = getNamedSession(controller)
                controllerSession[key] = value
                break

            case 'GLOBAL':
                session[key] = value
                break
        }

        render key
    }

    def get() {
        String scope = params.scope
        String controller = params.targetController
        String action = params.targetAction
        String key = params.key

        switch (scope) {
            case 'ACTION':
                Map actionSession = getNamedSession(controller + '_' + action)
                render actionSession[key]
                break

            case 'CONTROLLER':
                Map controllerSession = getNamedSession(controller)
                render controllerSession[key]
                break

            case 'GLOBAL':
                render session[key]
                break
        }
    }
}
