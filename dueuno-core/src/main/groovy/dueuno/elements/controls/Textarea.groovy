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

import dueuno.commons.utils.StringUtils
import dueuno.types.Type
import groovy.transform.CompileStatic

/**
 * A multi-line text-input control backed by {@link dueuno.types.Type#TEXT}.
 * <p>
 * Extends {@link TextField} with multi-line rendering, optional newline acceptance, and
 * optional Base64 encoding of the submitted value. When {@link #encode} is {@code true},
 * the submitted content is Base64-encoded before transmission and can be decoded server-side
 * via {@link #decodeText(String)}.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class Textarea extends TextField {

    /** When {@code true}, newline characters are accepted in the input. Defaults to {@code true}. */
    Boolean acceptNewLine

    /** When {@code true}, the submitted value is Base64-encoded before transmission. Defaults to {@code false}. */
    Boolean encode

    /**
     * Creates a {@code Textarea} instance configured from the supplied argument map.
     * Sets the value type to {@link dueuno.types.Type#TEXT}, disables auto-select by default,
     * and marks the container as multi-line.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code autoSelect} ({@link Boolean}, default {@code false}),
     *             {@code acceptNewLine} ({@link Boolean}, default {@code true}),
     *             {@code encode} ({@link Boolean}, default {@code false}),
     *             plus all keys accepted by {@link TextField#TextField(Map)}
     */
    Textarea(Map args) {
        super(args)

        valueType = Type.TEXT
        autoSelect = args.autoSelect == null ? false : args.autoSelect
        acceptNewLine = args.acceptNewLine == null ? true : args.acceptNewLine
        encode = args.encode == null ? false : args.encode

        containerSpecs.multiline = true
    }

    /**
     * Serialises this control's properties to JSON, adding {@code autoSelect},
     * {@link #acceptNewLine}, and {@link #encode}.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this control's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                autoSelect: autoSelect,
                acceptNewLine: acceptNewLine,
                encode: encode,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    //
    // Utils
    //

    /**
     * Decodes a Base64-encoded string previously submitted by a {@code Textarea} with
     * {@link #encode} set to {@code true}.
     *
     * @param encodedString the Base64-encoded string to decode
     * @return the decoded plain-text string
     */
    static String decodeText(String encodedString) {
        return StringUtils.base64Decode(encodedString)
    }
}
