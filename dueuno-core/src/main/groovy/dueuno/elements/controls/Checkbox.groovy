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

import dueuno.core.PrettyPrinterProperties
import dueuno.elements.Component
import dueuno.elements.Control
import dueuno.types.Type
import groovy.transform.CompileStatic

/**
 * A boolean checkbox control that can operate in two modes:
 * <ul>
 *   <li><b>Simple mode</b> — stores {@code true}/{@code false}; {@link #getChecked()} returns
 *       {@code true} when the value is {@code true}.</li>
 *   <li><b>Option mode</b> — stores an arbitrary key/value pair set via {@link #setOption(Map)};
 *       {@link #getChecked()} returns {@code true} when the current value equals
 *       {@link #optionKey}.</li>
 * </ul>
 * <p>
 * The value type is always {@link dueuno.types.Type#BOOL}. An optional inline label
 * ({@link #text}) is displayed next to the checkbox tick.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class Checkbox extends Control {

    /** Inline label text displayed beside the checkbox tick (i18n key or literal). */
    String text

    /** Interpolation arguments for the {@link #text} i18n message key. */
    List textArgs

    /** When {@code true}, renders a simplified checkbox without a toggle-switch style. */
    Boolean simple

    /**
     * The key value that represents the "checked" state in option mode.
     * Compared against the control's current value to determine {@link #getChecked()}.
     */
    Object optionKey

    /** The display label associated with {@link #optionKey} in option mode. */
    Object optionValue

    /**
     * Creates a {@code Checkbox} instance configured from the supplied argument map.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code text} ({@link String}) — inline label (defaults to {@code id.text} i18n key),
     *             {@code textArgs} ({@link List}),
     *             {@code simple} ({@link Boolean}, default {@code false}),
     *             {@code option} ({@link Map}) — single-entry map {@code {key: value}} for option mode,
     *             {@code optionKey}, {@code optionValue} — alternative to {@code option},
     *             plus all keys accepted by {@link Control#Control(Map)}
     */
    Checkbox(Map args) {
        super(args)

        valueType = Type.BOOL

        textArgs = args.textArgs as List ?: []
        text = args.text == null ? buildLabel(id + ".text", new PrettyPrinterProperties(textArgs: textArgs)) : args.text
        simple = (args.simple == null) ? false : args.simple
        if (args.option) {
            setOption(args.option as Map ?: [:])
        } else {
            optionKey = args.optionKey ?: ''
            optionValue = args.optionValue ?: ''
        }

        containerSpecs.label = args.label ?: ''
        containerSpecs.nullable = true
    }

    /**
     * Returns whether this checkbox should be rendered in a checked state.
     * <ul>
     *   <li>Returns {@code false} when the value is {@code null} or falsy.</li>
     *   <li>Returns {@code true} when the value is {@code true} (simple mode).</li>
     *   <li>Returns {@code true} when {@link #optionKey} is set and the value equals
     *       {@link #optionKey} (option mode).</li>
     * </ul>
     *
     * @return {@code true} if the checkbox should appear checked
     */
    Boolean getChecked() {
        if (!value) return false
        if (value == true) return true
        if (optionKey && (value == optionKey)) return true
        return false
    }

    /**
     * Configures this checkbox in option mode using a single-entry map.
     * The map's first entry provides the {@link #optionKey} and {@link #optionValue}.
     *
     * @param option a single-entry map {@code {key: value}} representing the option
     */
    void setOption(Map option) {
        option.find {key, value ->
            optionKey = key
            optionValue = value
            return true
        }
    }

    /**
     * Returns the current option as a single-entry map ({@link #optionKey} → {@link #optionValue}).
     *
     * @return the option map
     */
    Map getOption() {
        Map option = [:]
        option.put(optionKey, optionValue)
        return option
    }

    /**
     * Registers a {@code change} event listener that submits the form when the checkbox
     * is toggled, if no {@code change} handler has already been registered.
     *
     * @param args event configuration forwarded to {@link dueuno.elements.Component#on(Map)}
     * @return this component for chaining
     */
    @Override
    Component onSubmit(Map args) {
        String submitEvent = 'change'
        if (!hasEvent(submitEvent)) {
            args.event = submitEvent
            on(args)
        }

        return this
    }

    /**
     * Serialises this checkbox's properties to JSON, adding {@link #simple}, the toggle
     * mode identifier, and {@link #optionKey}.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this checkbox's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                simple: simple,
                toggle: 'toggle',
                option: optionKey,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }
}
