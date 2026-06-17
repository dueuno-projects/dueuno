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

import dueuno.elements.Control
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
 * defaults to {@link Type#TEXT} when no value is present.
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
     *             {@code value} — the value to carry (type is auto-detected if not specified),
     *             {@code valueType} ({@link String} or {@link Type}) — explicit type override,
     *             plus all keys accepted by {@link Control#Control(Map)}
     */
    HiddenField(Map args) {
        super(args)

        Map value = Types.serializeValue(args.value)
        valueType = args.valueType ?: value?.type ?: Type.TEXT

        skipFocus = true
        display = false
        containerSpecs.display = false
    }

}
