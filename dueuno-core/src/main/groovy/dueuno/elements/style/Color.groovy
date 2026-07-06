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
import groovy.util.logging.Slf4j

/**
 * Provides named colour constants and colour-conversion utilities used by the Elements
 * framework for semantic UI styling (danger, warning, success, info, disabled states).
 * <p>
 * All colour constants are hex RGB strings (e.g. {@code "#aa0000"}) compatible with CSS.
 * The {@link #hexToIntColor(String)} utility converts a hex colour to a list of three
 * integer RGB components, useful for programmatic colour manipulation.
 * </p>
 *
 * @author Gianluca Sartori
 */
@Slf4j
@CompileStatic
class Color {

    /** Foreground (text) colour for danger/error states. */
    static final String DANGER_TEXT = '#aa0000'

    /** Background colour for danger/error states. */
    static final String DANGER_BACKGROUND = '#ffdddd'

    /** Foreground (text) colour for warning states. */
    static final String WARNING_TEXT = '#755e01'

    /** Background colour for warning states. */
    static final String WARNING_BACKGROUND = '#ffeeaa'

    /** Foreground (text) colour for success states. */
    static final String SUCCESS_TEXT = '#014513'

    /** Background colour for success states. */
    static final String SUCCESS_BACKGROUND = '#bce3c6'

    /** Foreground (text) colour for informational states. */
    static final String INFO_TEXT = '#01224a'

    /** Background colour for informational states. */
    static final String INFO_BACKGROUND = '#dbebff'

    /** Foreground (text) colour for disabled elements. */
    static final String DISABLED_TEXT = '#777777'

    /** Pure white colour ({@code "white"}). */
    static final String WHITE = 'white'

    /**
     * Converts a 7-character hex RGB colour string (e.g. {@code "#aa0000"}) to a list of
     * three integer components {@code [red, green, blue]}, each in the range 0–255.
     * <p>
     * Returns an empty list and logs a warning if the input is {@code null}, empty, or does
     * not match the expected {@code #RRGGBB} format.
     * </p>
     *
     * @param hexRgbColor the hex colour string to convert (must start with {@code '#'} and be 7 characters long)
     * @return a {@link List} of three {@link Integer} values {@code [R, G, B]}, or an empty list on invalid input
     */
    static List<Integer> hexToIntColor(String hexRgbColor) {
        if (!hexRgbColor) return []
        if (!hexRgbColor.startsWith('#') || hexRgbColor.size() != 7) {
            log.warn "Color '${hexRgbColor}' is not a valid RGB color."
            return []
        }

        String hexColor = hexRgbColor.replaceAll('#', '')
        List<Integer> intColor = []

        for (int i = 0; i < 3; i++) {
            String hexC = hexColor[i * 2..i * 2 + 1]
            Integer intC = Integer.parseInt(hexC, 16)
            intColor.add(intC)
        }

        return intColor
    }

}