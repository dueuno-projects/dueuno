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
package dueuno.properties

import groovy.transform.CompileStatic

/**
 * Enumerates the data types supported by the Elements property system.
 * <p>
 * Each constant describes the expected type of a configurable application property,
 * allowing the framework to render the appropriate input control and apply
 * type-specific validation and formatting.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
enum PropertyType {

    /** A boolean (true/false) value; typically rendered as a checkbox or toggle. */
    BOOL,

    /** A numeric value (integer or decimal); rendered as a number input. */
    NUMBER,

    /** A combined date and time value; rendered as a date-time picker. */
    DATETIME,

    /** A date-only value; rendered as a date picker. */
    DATE,

    /** A time-only value; rendered as a time picker. */
    TIME,

    /** A plain text string; rendered as a text input. */
    STRING,

    /** A sensitive string whose characters are masked in the UI; rendered as a password input. */
    PASSWORD,

    /** A file path pointing to a specific file; rendered as a file-name input or browser. */
    FILENAME,

    /** A file system path pointing to a directory; rendered as a directory input or browser. */
    DIRECTORY,

    /** A URL string; rendered as a URL input with basic format validation. */
    URL
}
