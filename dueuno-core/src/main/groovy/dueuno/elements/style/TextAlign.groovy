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
 * Enumerates the horizontal text-alignment options available to Elements UI components.
 * <p>
 * Each constant maps to a Bootstrap CSS utility class. {@link #toString()} returns the
 * CSS class string directly, so instances can be interpolated into HTML {@code class}
 * attributes without additional conversion.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
enum TextAlign {

    /** No explicit alignment; inherits the parent element's alignment. CSS class: {@code ""}. */
    DEFAULT(''),

    /** Aligns text to the start (left in LTR, right in RTL). CSS class: {@code "text-start"}. */
    START('text-start'),

    /** Aligns text to the end (right in LTR, left in RTL). CSS class: {@code "text-end"}. */
    END('text-end'),

    /** Centres the text horizontally. CSS class: {@code "text-center"}. */
    CENTER('text-center')

    /** The Bootstrap CSS utility class corresponding to this alignment value. */
    final String cssClass

    /**
     * Creates a {@code TextAlign} constant bound to the given CSS class string.
     *
     * @param cssClass the Bootstrap utility class for this alignment (may be empty for {@link #DEFAULT})
     */
    TextAlign(String cssClass) {
        this.cssClass = cssClass
    }

    /**
     * Returns the Bootstrap CSS class string for this alignment (e.g. {@code "text-center"}).
     *
     * @return the CSS class, or an empty string for {@link #DEFAULT}
     */
    String toString() {
        return cssClass
    }
}