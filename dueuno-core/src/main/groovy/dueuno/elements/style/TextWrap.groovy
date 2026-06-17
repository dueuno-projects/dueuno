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
 * Enumerates the text-wrapping options available to Elements UI components.
 * <p>
 * Each constant maps to a CSS utility class name. {@link #toString()} returns the
 * class string directly, so instances can be interpolated into HTML {@code class}
 * attributes without additional conversion.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
enum TextWrap {

    /** No explicit wrapping behaviour; inherits from the parent element. CSS class: {@code ""}. */
    DEFAULT(''),

    /** Prevents the text from wrapping to the next line. CSS class: {@code "no-wrap"}. */
    NO_WRAP('no-wrap'),

    /** Allows soft (browser-controlled) line wrapping. CSS class: {@code "soft-wrap"}. */
    SOFT_WRAP('soft-wrap'),

    /** Forces line wrapping at word boundaries. CSS class: {@code "line-wrap"}. */
    LINE_WRAP('line-wrap'),

    /** Forces a line break at the element boundary. CSS class: {@code "line-break"}. */
    LINE_BREAK('line-break')

    /** The CSS utility class corresponding to this wrapping option. */
    final String cssClass

    /**
     * Creates a {@code TextWrap} constant bound to the given CSS class string.
     *
     * @param cssClass the CSS utility class for this wrapping option (may be empty for {@link #DEFAULT})
     */
    TextWrap(String cssClass) {
        this.cssClass = cssClass
    }

    /**
     * Returns the CSS utility class string for this wrapping option (e.g. {@code "no-wrap"}).
     *
     * @return the CSS class, or an empty string for {@link #DEFAULT}
     */
    String toString() {
        return cssClass
    }
}