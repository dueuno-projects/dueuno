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
 * Enumeration of HTML {@code inputmode} attribute values for {@link TextField} controls.
 * <p>
 * The {@code inputmode} hint tells the browser which virtual keyboard to display on
 * touch devices. The enum name is lowercased when serialised to HTML.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
enum TextFieldInputMode {
    /** No virtual keyboard hint; the browser uses its default. */
    NONE,

    /** Standard text keyboard. */
    TEXT,

    /** Numeric keyboard (integers, no decimal separator). */
    NUMERIC,

    /** Decimal keyboard (numeric with a locale-appropriate decimal separator). */
    DECIMAL,

    /** Search-optimised keyboard. */
    SEARCH,

    /** E-mail address keyboard (includes {@code @} and {@code .}). */
    EMAIL,

    /** URL keyboard (includes {@code /}, {@code .}, and {@code :}). */
    URL,

    /** Telephone number keyboard (includes {@code +}, {@code *}, and {@code #}). */
    TEL
}
