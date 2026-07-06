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

import dueuno.commons.utils.ObjectUtils
import dueuno.types.Types
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Type Management
 *
 * @author Gianluca Sartori
 */
@Slf4j
@CompileStatic
class ParamsInterceptor implements ElementsController {

    int order = HIGHEST_PRECEDENCE

    ParamsInterceptor() {
        matchAll()
    }

    boolean before() {
//        String httpMethod = request.get ? 'GET' : request.post ? 'POST' : null
//        println " [${httpMethod}|${request.format}] /${controllerName}/${actionName} ".center(80, '-')
//        println "QUERY STRING: ${request.queryString}"
//        println "BEFORE: ${params}"
        process21Params(params)
//        println "AFTER:"
//        println PrettyPrinter.printParams(params)
        return true
    }

    boolean after() {
        return true
    }

    void afterView() {
        // no-op
    }

    private void process21Params(Map params) {
        if (!params._21Params) {
            return
        }

        JsonSlurper slurper = new JsonSlurper()
        Map _21Params = slurper.parseText(params._21Params as String) as Map

        Map componentValues = processComponents(_21Params)
        Map processedParams = processParams(_21Params)

        params.putAll(componentValues)
        params.putAll(processedParams)
        params.remove('_21Params')
    }

    private Map processParams(Map submitted) {
        return Types.deserialize(submitted.params as Map ?: [:])
    }

    private Map processComponents(Map submitted) {
        Map components = submitted.components as Map
        if (!components) {
            return [_21SubmittedCount: 0]
        }

        Map results = [:]
        for (component in components) {
            String componentName = component.key
            Map componentValues = component.value as Map
            results[componentName] = ObjectUtils.expandDotKeysMap(Types.deserialize(componentValues))
        }

        if (components.size() == 1) {
            String formName = components.keySet()[0]
            Map result = results[formName] as Map
            result._21SubmittedCount = 1
            return result

        } else {
            results._21SubmittedCount = results.size()
            return results
        }
    }

}
