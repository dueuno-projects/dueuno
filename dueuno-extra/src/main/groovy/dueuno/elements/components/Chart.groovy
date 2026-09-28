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

import dueuno.elements.core.Component
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
abstract class Chart extends Component {

    List values
    List labels
    List colors

    Chart(Map args) {
        super(args)

        viewPath = '/dueuno/elements/components/'

        values = args.values as List ?: []
        labels = args.labels as List ?: []
        colors = args.colors as List ?: []

        containerSpecs.multiline = true
    }

    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                values: values,
                labels: labels,
                colors: colors,
        ]

        return super.getPropertiesAsJSON(thisProperties + properties)
    }

}
