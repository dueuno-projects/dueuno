/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.elements.controls

import groovy.transform.CompileStatic

/**
 * A {@link Select} implementation rendered with Virtual Select.
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class SelectX extends Select {

    SelectX(Map args) {
        super(args)
    }

    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        List<Map<String, String>> virtualOptions = []
        for (entry in super.getOptions()) {
            Map<String, String> option = [id: entry.key as String, text: entry.value as String]
            virtualOptions.add(option)
        }
        return super.getPropertiesAsJSON([options: virtualOptions] + properties)
    }
}
