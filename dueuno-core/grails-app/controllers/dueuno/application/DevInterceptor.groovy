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
import groovy.transform.CompileStatic

/**
 * Dev hints on command line
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class DevInterceptor implements ElementsController {

    int order = HIGHEST_PRECEDENCE + 100

    DevInterceptor() {
        matchAll()
    }

    boolean before() {
        if (devDisplayHints && controllerName != 'shell') {
            String httpMethod = request.get ? 'GET' : request.post ? 'POST' : null
            println " [${httpMethod}|${request.format}] /${controllerName}/${actionName} ".center(80, '-')
            println PrettyPrinter.printParams(params)
        }

        return true
    }

    boolean after() {
        return true
    }

    void afterView() {
        // no-op
    }
}
