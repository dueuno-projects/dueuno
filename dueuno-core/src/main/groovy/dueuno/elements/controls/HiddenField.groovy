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

import dueuno.commons.utils.ObjectUtils
import dueuno.elements.core.Control
import dueuno.types.Type
import dueuno.types.Types
import groovy.transform.CompileStatic

/**
 * An invisible form control used to carry typed values across AJAX form submissions
 * without rendering any visible UI element.
 * <p>
 * {@code HiddenField} is used internally by {@link dueuno.elements.components.Form#addKeyField}
 * to transmit primary-key and surrogate-key values. The value type is inferred from the
 * supplied value via {@link Types#serializeValue(Object)} when not specified explicitly, and
 * defaults to {@link Type#STRING} when no value is present.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class HiddenField extends Control {

    /**
     * Creates a {@code HiddenField} instance configured from the supplied argument map.
     * The field is hidden from both the UI and the form layout.
     *
     * @param args initialisation arguments; recognised keys include:
     * {@code value} — the value to carry (type is auto-detected if not specified),
     * {@code valueType} ({@link String} or {@link Type}) — explicit type override,
     *             plus all keys accepted by {@link Control#Control(Map)}
     */
    HiddenField(Map args) {
        super(args)

        Map value = Types.serializeValue(args.value)
        valueType = args.valueType ?: value?.type ?: Type.STRING

        skipFocus = true
        display = false
        containerSpecs.display = false
    }

    /**
     * Sets the selected value(s) for this control.
     * <ul>
     *   <li>A single object with an {@code id} property is unwrapped to its ID.</li>
     *   <li>All other values are passed directly to the superclass.</li>
     * </ul>
     *
     * @param value the value to select; accepts {@code null}, a scalar, or a {@link Collection}
     */
    @Override
    void setValue(Object value) {
        if (!valueType || valueType in [Type.NA.toString(), Type.STRING.toString()]) {
            valueType = Types.serializeValue(value)?.type ?: Type.STRING
        }

        if (ObjectUtils.hasId(value)) {
            super.setValue(value['id'])
        } else {
            super.setValue(value)
        }
    }

}
