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
package dueuno.elements.style

import groovy.transform.CompileStatic

/**
 * Enumerates the CSS {@code text-transform} options available to Elements UI components.
 * <p>
 * Each constant maps to a CSS {@code text-transform} value. {@link #toString()} returns
 * the value string directly, so instances can be used in inline styles or utility-class
 * lookups without additional conversion.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
enum TextTransform {

    /** No text transformation applied. CSS value: {@code ""}. */
    NONE(''),

    /** Transforms all characters to upper case. CSS value: {@code "uppercase"}. */
    UPPERCASE('uppercase'),

    /** Transforms all characters to lower case. CSS value: {@code "lowercase"}. */
    LOWERCASE('lowercase'),

    /** Capitalises the first letter of each word. CSS value: {@code "capitalize"}. */
    CAPITALIZE('capitalize')

    /** The CSS {@code text-transform} value corresponding to this constant. */
    final String cssClass

    /**
     * Creates a {@code TextTransform} constant bound to the given CSS value string.
     *
     * @param cssClass the CSS {@code text-transform} value (may be empty for {@link #NONE})
     */
    TextTransform(String cssClass) {
        this.cssClass = cssClass
    }

    /**
     * Returns the CSS {@code text-transform} value for this constant (e.g. {@code "uppercase"}).
     *
     * @return the CSS value, or an empty string for {@link #NONE}
     */
    String toString() {
        return cssClass
    }
}