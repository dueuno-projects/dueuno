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
package dueuno.elements.components

import dueuno.elements.core.WebRequestAware
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

/**
 * Table multiple selection handling
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class TableInterceptor implements WebRequestAware {

    int order = HIGHEST_PRECEDENCE + 10

    TableInterceptor() {
        matchAll()
    }

    boolean before() {
        //println "BEFORE: $params"
        processMultipleSelection(params)
        //println "AFTER:\n" + PrintUtils.displayPrettyParams(params)
        return true
    }

    boolean after() {
        return true
    }

    void afterView() {
        // no-op
    }

    private void processMultipleSelection(Map params) {
        if (!params._tableSelection)
            return

        JsonSlurper json = new JsonSlurper()
        List<Map> selectedRows = json.parseText(params._tableSelection as String) as List<Map>

        // Convert integers to Long (this way 'in' works again in GORM queries)
        for (ids in selectedRows) {
            for (id in ids) {
                if (id.value in Integer) id.value = id.value as Long
            }
        }

        params._tableSelection = selectedRows
    }
}
