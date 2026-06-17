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
 * A text-input control for entering URLs.
 * <p>
 * Extends {@link TextField} with an input validation pattern restricted to URL-safe characters
 * ({@code ^[a-zA-Z0-9@\-_.:\/~#=!?&\*()]*$}), the {@link TextFieldInputMode#URL} input mode,
 * and a globe icon ({@code fa-globe}) displayed inside the input.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class UrlField extends TextField {

    /**
     * Creates a {@code UrlField} instance configured from the supplied argument map.
     * Sets the input validation pattern, input mode, and icon; the pattern can be overridden
     * via {@code args.pattern}.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code pattern} ({@link String}, default {@code "^[a-zA-Z0-9@\\-_.:\\/~#=!?&\\*()]*$"}),
     *             plus all keys accepted by {@link TextField#TextField(Map)}
     */
    UrlField(Map args) {
        super(args)

        viewTemplate = 'TextField'
        pattern = args.pattern ?: '^[a-zA-Z0-9@\\-_.:\\/~#=!?&\\*()]*$'

        inputMode = TextFieldInputMode.URL
        icon = 'fa-globe'
    }
}
