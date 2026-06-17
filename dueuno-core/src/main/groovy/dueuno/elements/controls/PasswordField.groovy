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
 * A password text-input control that masks the entered characters.
 * <p>
 * Extends {@link TextField} with the HTML input type set to {@link TextFieldInputType#PASSWORD},
 * a lock icon ({@code fa-lock}) displayed inside the input, and browser autocomplete disabled.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class PasswordField extends TextField {

    /**
     * Creates a {@code PasswordField} instance configured from the supplied argument map.
     * Sets the input type to {@link TextFieldInputType#PASSWORD}, applies a default lock icon,
     * and disables browser autocomplete.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code icon} ({@link String}, default {@code "fa-lock"}),
     *             plus all keys accepted by {@link TextField#TextField(Map)}
     */
    PasswordField(Map args) {
        super(args)

        viewTemplate = 'TextField'
        inputType = TextFieldInputType.PASSWORD

        icon = (args.icon == null) ? 'fa-lock' : args.icon
        autocomplete = false
    }
}