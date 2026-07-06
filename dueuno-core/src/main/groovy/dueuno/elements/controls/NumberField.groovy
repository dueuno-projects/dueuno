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

import dueuno.elements.Elements
import dueuno.types.Type
import groovy.transform.CompileStatic

/**
 * A numeric text-input control backed by {@link dueuno.types.Type#NUMBER}.
 * <p>
 * Extends {@link TextField} with number-specific features: configurable decimal places,
 * optional negative-value support, and optional min/max boundaries. The client-side
 * validation pattern is generated dynamically by {@link #buildPattern()} and applied
 * during JSON serialisation.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class NumberField extends TextField {

    /** Number of decimal places allowed; {@code 0} means integers only. */
    Integer decimals

    /** Whether negative values are allowed. Defaults to {@code true}. */
    Boolean negative

    /** Minimum allowed value (inclusive); {@code null} means no lower bound. */
    Integer min

    /** Maximum allowed value (inclusive); {@code null} means no upper bound. */
    Integer max

    /**
     * Creates a {@code NumberField} instance configured from the supplied argument map.
     * Sets the value type to {@link dueuno.types.Type#NUMBER} and configures the input mode
     * based on whether decimal places are required.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code decimals} ({@link Integer}, default {@code 0}),
     *             {@code negative} ({@link Boolean}, default {@code true}),
     *             {@code min} ({@link Integer}),
     *             {@code max} ({@link Integer}),
     *             {@code pattern} ({@link String}) — overrides the default regex,
     *             plus all keys accepted by {@link TextField#TextField(Map)}
     */
    NumberField(Map args) {
        super(args)

        viewTemplate = 'TextField'
        valueType = Type.NUMBER
        pattern = args.pattern ?: '^[0-9\\-\\.\\,]*$'
        decimals = args.decimals as Integer ?: 0
        negative = (args.negative == null) ? true : args.negative
        min = args.min as Integer
        max = args.max as Integer

        inputType = TextFieldInputType.TEXT
        inputMode = decimals ? TextFieldInputMode.DECIMAL : TextFieldInputMode.NUMERIC
    }

    /**
     * Builds the client-side input validation regex pattern based on the current
     * {@link #negative} and {@link #decimals} settings.
     * <p>Examples:</p>
     * <ul>
     *   <li>integers only, no negatives: {@code ^[0-9]*$}</li>
     *   <li>integers, allow negatives: {@code ^-?[0-9]*$}</li>
     *   <li>2 decimal places, allow negatives: {@code ^-?[0-9]*((?<=[0-9])[.,])?[0-9]{0,2}$}</li>
     * </ul>
     *
     * @return the generated regex pattern string
     */
    String buildPattern() {
        String result = '^'

        if (negative) result += '-?'

        result += '[0-9]*'
        if (decimals > 0) {
            result += '((?<=[0-9])[\\.\\,])?[0-9]{0,' + decimals + '}';
        }

        result += '$'
        return result
    }

    /**
     * Serialises this field's properties to JSON, first regenerating the validation pattern
     * via {@link #buildPattern()}, then adding {@link #decimals}, {@link #negative},
     * {@link #min}, and {@link #max}.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this field's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        pattern = buildPattern()
        Map thisProperties = [
                decimals: decimals,
                negative: negative,
                min: min,
                max: max,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    /**
     * Serialises the current numeric value to a JSON string containing the
     * {@code type}, {@code value}, and {@code decimals} fields.
     *
     * @return a JSON string representing the current number value
     */
    @Override
    String getValueAsJSON() {
        Map valueMap = [
                type: valueType,
                value: value,
                decimals: decimals,
        ]

        return Elements.encodeAsJSON(valueMap)
    }
}
