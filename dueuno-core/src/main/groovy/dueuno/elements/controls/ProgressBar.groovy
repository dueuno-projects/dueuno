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
package dueuno.elements.controls

import dueuno.elements.core.Control
import dueuno.elements.core.PrettyPrinterProperties
import dueuno.types.Type
import groovy.transform.CompileStatic

/**
 * @author Francesco Piceghello
 */

@CompileStatic
class ProgressBar extends Control {

    BigDecimal min
    BigDecimal max
    Integer decimals
    Boolean indefinite
    Boolean percentage

    ProgressBar(Map args) {
        super(args)

        valueType = Type.NUMBER

        indefinite = args.indefinite == null ? args.max == null : args.indefinite as Boolean
        min = args.min == null ? 0.0 : args.min as BigDecimal
        max = args.max == null ? 100.0 : args.max as BigDecimal
        value = args.value == null ? 0.0 : args.value as BigDecimal
        percentage = args.percentage == null ? true : args.percentage as Boolean
        decimals = args.decimals as Integer ?: 0

        skipFocus = true
    }

    @Override
    String getPrettyValue() {
        BigDecimal displayValue = value as BigDecimal
        if (percentage) {
            displayValue = 0.0
            if (max > min) {
                BigDecimal currentValue = value as BigDecimal
                displayValue = ((currentValue - min) / (max - min) * 100).max(0.0).min(100.0)
            }
        }

        PrettyPrinterProperties renderProperties = new PrettyPrinterProperties()
        renderProperties.decimals = decimals
        return prettyPrint(displayValue, renderProperties)
    }

    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
            min       : min,
            max       : max,
            value     : value,
            decimals  : decimals,
            indefinite: indefinite,
            percentage: percentage,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }
}
