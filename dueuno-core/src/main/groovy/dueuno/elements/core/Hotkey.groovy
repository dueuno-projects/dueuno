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
package dueuno.elements.core

import groovy.transform.CompileStatic

/**
 * Hotkeys supported by {@link dueuno.elements.components.Link}.
 * <p>
 * Every hotkey must be used with {@code Command} on macOS or {@code Meta}
 * on Windows/Linux. Left and right variants work identically. The
 * {@code Shift} modifier maps F1–F12 to F13–F24. The
 * {@code Ctrl}/{@code Control}, {@code Alt}/{@code Option}, and {@code Fn}
 * keys alone do not activate hotkeys.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
enum Hotkey {

    /** Function key F1. */
    F1('F1'),
    /** Function key F2. */
    F2('F2'),
    /** Function key F3. */
    F3('F3'),
    /** Function key F4. */
    F4('F4'),
    /** Function key F5. */
    F5('F5'),
    /** Function key F6. */
    F6('F6'),
    /** Function key F7. */
    F7('F7'),
    /** Function key F8. */
    F8('F8'),
    /** Function key F9. */
    F9('F9'),
    /** Function key F10. */
    F10('F10'),
    /** Function key F11. */
    F11('F11'),
    /** Function key F12. */
    F12('F12'),
    /** Function key F13, also activated by Meta + Shift + F1. */
    F13('F13'),
    /** Function key F14, also activated by Meta + Shift + F2. */
    F14('F14'),
    /** Function key F15, also activated by Meta + Shift + F3. */
    F15('F15'),
    /** Function key F16, also activated by Meta + Shift + F4. */
    F16('F16'),
    /** Function key F17, also activated by Meta + Shift + F5. */
    F17('F17'),
    /** Function key F18, also activated by Meta + Shift + F6. */
    F18('F18'),
    /** Function key F19, also activated by Meta + Shift + F7. */
    F19('F19'),
    /** Function key F20, also activated by Meta + Shift + F8. */
    F20('F20'),
    /** Function key F21, also activated by Meta + Shift + F9. */
    F21('F21'),
    /** Function key F22, also activated by Meta + Shift + F10. */
    F22('F22'),
    /** Function key F23, also activated by Meta + Shift + F11. */
    F23('F23'),
    /** Function key F24, also activated by Meta + Shift + F12. */
    F24('F24'),
    /** Enter, used with a standard modifier key. */
    ENTER('Enter')

    /** The string representation passed to the client-side hotkey listener. */
    final String key

    /**
     * Creates a keyboard hotkey with its client-side key representation.
     *
     * @param key the key representation used by the browser listener
     */
    Hotkey(String key) {
        this.key = key
    }
}
