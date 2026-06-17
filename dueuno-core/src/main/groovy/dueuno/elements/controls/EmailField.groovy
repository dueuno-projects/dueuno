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
 * A text-input control pre-configured for e-mail address entry.
 * <p>
 * Extends {@link TextField} with:
 * <ul>
 *   <li>Input mode set to {@link TextFieldInputMode#EMAIL} (triggers the e-mail keyboard
 *       on mobile devices).</li>
 *   <li>A client-side validation pattern that allows a single {@code @} character,
 *       prevents consecutive dots, and restricts characters to letters, digits, and
 *       {@code _}, {@code -}, {@code .}, {@code @}.</li>
 *   <li>An {@code fa-at} icon displayed inside the input.</li>
 * </ul>
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class EmailField extends TextField {

    /**
     * Creates an {@code EmailField} instance configured from the supplied argument map.
     * Sets the input mode, validation pattern, and icon; all three can be overridden via
     * {@code args}.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code inputMode} ({@link TextFieldInputMode}, default {@link TextFieldInputMode#EMAIL}),
     *             {@code pattern} ({@link String}) — overrides the default e-mail regex,
     *             plus all keys accepted by {@link TextField#TextField(Map)}
     */
    EmailField(Map args) {
        super(args)

        viewTemplate = 'TextField'
        inputMode = args.inputMode as TextFieldInputMode ?: TextFieldInputMode.EMAIL
        pattern = args.pattern ?: '^(?!.*@.*@)(?!.*(\\.)\\1).[A-Za-z0-9_\\-\\.@]*$'
        icon = 'fa-at'
    }
}
