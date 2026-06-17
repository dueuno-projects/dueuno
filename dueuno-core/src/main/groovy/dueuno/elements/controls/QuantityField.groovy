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

import dueuno.core.PrettyPrinterProperties
import dueuno.elements.Elements
import dueuno.exceptions.ElementsException
import dueuno.types.Quantity
import dueuno.types.QuantityUnit
import groovy.transform.CompileStatic

/**
 * A numeric input control for entering {@link Quantity} values (amount + unit of measure).
 * <p>
 * Extends {@link NumberField} with the value type fixed to {@link Quantity#TYPE_NAME}. The
 * unit of measure is displayed as a selector populated from the list of available units, and
 * is automatically updated when a {@link Quantity} value is set. Defaults to 2 decimal places
 * and no negative values.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class QuantityField extends NumberField {

    /** Map of {@link QuantityUnit} name → localised display label for the unit selector. */
    Map<String, String> unitOptions

    /** The currently selected unit of measure; defaults to {@link QuantityUnit#ND} when no unit is available. */
    QuantityUnit defaultUnit

    /**
     * Creates a {@code QuantityField} instance configured from the supplied argument map.
     * Sets the view template, value type, decimal places, negative-value flag, and available units.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code decimals} ({@link Integer}, default {@code 2}),
     *             {@code negative} ({@link Boolean}, default {@code false}),
     *             {@code availableUnits} ({@link List} of {@link QuantityUnit}),
     *             {@code defaultUnit} ({@link QuantityUnit}),
     *             plus all keys accepted by {@link NumberField#NumberField(Map)}
     */
    QuantityField(Map args) {
        super(args)

        viewTemplate = 'QuantityField'
        valueType = Quantity.TYPE_NAME

        decimals = args.decimals == null ? 2 : args.decimals as Integer
        negative = (args.negative == null) ? false : args.negative
        unitOptions = unitListToOptions(args.availableUnits as List)
        setDefaultUnit(args.defaultUnit as QuantityUnit)

        inputMode = decimals ? TextFieldInputMode.DECIMAL : TextFieldInputMode.NUMERIC
    }

    /**
     * Sets the default unit for this field.
     * <ul>
     *   <li>If {@link #unitOptions} is non-empty and {@code value} is {@code null}, the first
     *       available unit is used.</li>
     *   <li>If {@code value} is non-null, it is used directly.</li>
     *   <li>Otherwise, {@link QuantityUnit#ND} is used as a fallback.</li>
     * </ul>
     *
     * @param value the desired default {@link QuantityUnit}, or {@code null} to auto-select
     */
    void setDefaultUnit(QuantityUnit value) {
        if (unitOptions && !value) {
            defaultUnit = unitOptions.keySet()[0]

        } else if (value) {
            defaultUnit = value

        } else {
            defaultUnit = QuantityUnit.ND
        }
    }

    /**
     * Converts a list of {@link QuantityUnit} values into an ordered map of
     * unit-name → localised display label, suitable for populating the unit selector.
     *
     * @param list the list of {@link QuantityUnit} values to convert; may be {@code null} or empty
     * @return a map of unit name strings to localised labels, or an empty map if {@code list} is empty
     */
    Map<String, String> unitListToOptions(List list) {
        if (!list) {
            return [:]
        }

        Map<String, String> results = [:]
        for (value in list) {
            // We only need to translate the Unit, not to transform it or do other stuff with it
            PrettyPrinterProperties renderProperties = new PrettyPrinterProperties()
            renderProperties.locale = prettyPrinterProperties.locale
            results[(value as String)] = prettyPrint(value as QuantityUnit, renderProperties)
        }
        return results
    }

    /**
     * Returns the localised display label for the current {@link #defaultUnit}.
     *
     * @return the pretty-printed string representation of {@link #defaultUnit} in the field's locale
     */
    String getPrettyDefaultUnit() {
        // We only need to translate the Unit, not to transform it or do other stuff with it
        PrettyPrinterProperties renderProperties = new PrettyPrinterProperties()
        renderProperties.locale = prettyPrinterProperties.locale
        return prettyPrint(defaultUnit, renderProperties)
    }

    /**
     * Sets the value of this field, enforcing that it must be a {@link Quantity} instance.
     * When a {@link Quantity} value is set, the {@link #defaultUnit} is updated to its unit.
     *
     * @param value the {@link Quantity} value to set, or {@code null} to clear the field
     * @throws dueuno.exceptions.ElementsException if {@code value} is not a {@link Quantity} instance
     */
    @Override
    void setValue(Object value) {
        super.setValue(value)

        if (this.value != null && (this.value !instanceof Quantity)) {
            throw new ElementsException("${this.getClass().simpleName} can only accept values of type '${Quantity.name}'")
        }

        if (this.value) {
            setDefaultUnit((this.value as Quantity).unit)
        }
    }

    /**
     * Serialises the current {@link Quantity} value to a JSON string containing the
     * {@code type}, {@code amount}, {@code unit}, and {@code decimals} fields.
     *
     * @return a JSON string representing the current quantity value
     */
    @Override
    String getValueAsJSON() {
        Map valueMap = [
                type: valueType,
                value: [
                        amount: (value as Quantity)?.amount,
                        unit: (value as Quantity)?.unit as String,
                        decimals: decimals,
                ]
        ]

        return Elements.encodeAsJSON(valueMap)
    }
}
