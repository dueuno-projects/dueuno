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
 * Enumeration of HTML {@code type} attribute values for {@link TextField} controls.
 * <p>
 * The {@code type} attribute determines the browser's built-in validation, UI widget,
 * and virtual keyboard on touch devices. The enum name is lowercased when serialised
 * to the HTML {@code <input>} element.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
enum TextFieldInputType {
    /** Plain text input; no special validation or widget. */
    TEXT,

    /** Numeric input with browser-native spinner controls. */
    NUMBER,

    /** Decimal numeric input. */
    DECIMAL,

    /** Search input; may render with a clear button in some browsers. */
    SEARCH,

    /** Password input; entered characters are masked. */
    PASSWORD,

    /** E-mail address input; browser validates the format. */
    EMAIL,

    /** URL input; browser validates the format. */
    URL,

    /** Telephone number input; no strict format validation, but optimises mobile keyboard. */
    TEL
}