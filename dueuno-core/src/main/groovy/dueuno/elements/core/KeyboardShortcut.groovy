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
 * Keyboard shortcuts supported by {@link dueuno.elements.components.Link}.
 * <p>
 * Every shortcut must be used with at least one standard modifier key:
 * {@code Ctrl}/{@code Control} or {@code Meta}/{@code Command}, on either
 * platform. Left and right variants work identically. The {@code Shift},
 * {@code Alt}/{@code Option}, and {@code Fn} keys are not included.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
enum KeyboardShortcut {

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
    /** Enter, used with a standard modifier key. */
    ENTER('Enter')

    /** The string representation passed to the client-side shortcut listener. */
    final String key

    /**
     * Creates a keyboard shortcut with its client-side key representation.
     *
     * @param key the key representation used by the browser listener
     */
    KeyboardShortcut(String key) {
        this.key = key
    }
}
