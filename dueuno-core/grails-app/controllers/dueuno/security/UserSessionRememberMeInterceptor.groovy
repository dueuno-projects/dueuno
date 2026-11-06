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
import groovy.transform.CompileStatic

/**
 * Delegates user-session request handling to UserSessionService.
 */
@CompileStatic
class UserSessionRememberMeInterceptor implements ElementsController {

    int order = HIGHEST_PRECEDENCE + 20

    UserSessionService userSessionService

    UserSessionRememberMeInterceptor() {
        matchAll()
    }

    boolean before() {
        return userSessionService.handleRequest()
    }

    boolean after() {
        return true
    }

    void afterView() {
        // no-op
    }
}
