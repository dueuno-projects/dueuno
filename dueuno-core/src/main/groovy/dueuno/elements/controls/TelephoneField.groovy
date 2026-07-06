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

import groovy.transform.CompileStatic

/**
 * A text-input control for entering telephone numbers.
 * <p>
 * Extends {@link TextField} with an input validation pattern restricted to digits and the
 * {@code +} sign ({@code ^[0-9\+]*$}), the {@link TextFieldInputMode#TEL} input mode for
 * mobile numeric keyboards, and a phone icon ({@code fa-phone}) displayed inside the input.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class TelephoneField extends TextField {

    /**
     * Creates a {@code TelephoneField} instance configured from the supplied argument map.
     * Sets the input validation pattern, input mode, and icon; the pattern and input mode
     * can be overridden via {@code args.pattern} and {@code args.inputMode} respectively.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code pattern} ({@link String}, default {@code "^[0-9\\+]*$"}),
     *             {@code inputMode} ({@link TextFieldInputMode}, default {@link TextFieldInputMode#TEL}),
     *             plus all keys accepted by {@link TextField#TextField(Map)}
     */
    TelephoneField(Map args) {
        super(args)

        viewTemplate = 'TextField'
        pattern = args.pattern ?: '^[0-9\\+]*$'

        inputMode = args.inputMode as TextFieldInputMode ?: TextFieldInputMode.TEL
        icon = 'fa-phone'
    }
}
