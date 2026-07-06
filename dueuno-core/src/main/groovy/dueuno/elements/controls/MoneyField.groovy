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
import dueuno.exceptions.ElementsException
import dueuno.types.Money
import groovy.transform.CompileStatic

/**
 * A numeric input control for entering {@link Money} values (amount + ISO 4217 currency code).
 * <p>
 * Extends {@link NumberField} with the value type fixed to {@link Money#TYPE_NAME}. The
 * currency code is displayed as a prefix and is automatically updated when a {@link Money}
 * value is set. Defaults to 2 decimal places, no negative values, and {@code EUR} currency.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class MoneyField extends NumberField {

    /**
     * Creates a {@code MoneyField} instance configured from the supplied argument map.
     * Sets the view template, value type, decimal places, negative-value flag, and currency prefix.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code decimals} ({@link Integer}, default {@code 2}),
     *             {@code negative} ({@link Boolean}, default {@code false}),
     *             {@code currency} ({@link String}, default {@code "EUR"}),
     *             plus all keys accepted by {@link NumberField#NumberField(Map)}
     */
    MoneyField(Map args) {
        super(args)

        viewTemplate = 'MoneyField'
        valueType = Money.TYPE_NAME

        decimals = args.decimals == null ? 2 : args.decimals as Integer
        negative = (args.negative == null) ? false : args.negative
        prefix = args.currency as String ?: 'EUR'

        inputMode = decimals ? TextFieldInputMode.DECIMAL : TextFieldInputMode.NUMERIC
    }

    /**
     * Sets the value of this field, enforcing that it must be a {@link Money} instance.
     * When a {@link Money} value is set, the {@code prefix} is updated to its currency code.
     *
     * @param value the {@link Money} value to set, or {@code null} to clear the field
     * @throws dueuno.exceptions.ElementsException if {@code value} is not a {@link Money} instance
     */
    @Override
    void setValue(Object value) {
        super.setValue(value)

        if (this.value && (this.value !instanceof Money)) {
            throw new ElementsException("${this.getClass().simpleName} can only accept values of type '${Money.name}'")
        }

        if (this.value) {
            prefix = (this.value as Money).currency
        }
    }

    /**
     * Serialises the current {@link Money} value to a JSON string containing the
     * {@code type}, {@code amount}, {@code currency}, and {@code decimals} fields.
     *
     * @return a JSON string representing the current money value
     */
    @Override
    String getValueAsJSON() {
        Map valueMap = [
                type: valueType,
                value: [
                        amount: (value as Money)?.amount,
                        currency: (value as Money)?.currency,
                        decimals: decimals,
                ]
        ]
        return Elements.encodeAsJSON(valueMap)
    }

}
