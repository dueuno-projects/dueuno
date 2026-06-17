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
import dueuno.elements.Elements
import dueuno.elements.components.Button
import dueuno.elements.style.TextTransform
import dueuno.types.Type
import groovy.transform.CompileStatic

/**
 * A single-line text-input control backed by {@link dueuno.types.Type#TEXT}.
 * <p>
 * Serves as the base class for specialised text controls (e.g. {@link PasswordField},
 * {@link NumberField}, {@link TelephoneField}, {@link Textarea}). Supports configurable
 * HTML input type and input mode, an optional icon or prefix displayed inside the input,
 * maximum character length, placeholder text, autocomplete, auto-select-on-focus, text
 * transformation, and an optional async {@code onChange} handler. An adjacent action
 * {@link Button} can be populated via {@link #addAction(Map)}.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class TextField extends Control {

    /** The HTML {@code type} attribute for the underlying {@code <input>} element. */
    TextFieldInputType inputType

    /** The HTML {@code inputmode} attribute, used to request a specific virtual keyboard on mobile devices. */
    TextFieldInputMode inputMode

    /** Action button rendered adjacent to the input field. */
    Button actions

    /** Optional icon class (e.g. {@code "fa-lock"}) displayed inside the input. */
    String icon

    /** Optional prefix string (e.g. a currency symbol) displayed before the input value. */
    String prefix

    /** Maximum number of characters accepted; {@code 0} means no limit. */
    Integer maxSize

    /** Placeholder text shown when the field is empty. */
    String placeholder

    /** When {@code true}, browser autocomplete is enabled for this field. Defaults to {@code false}. */
    Boolean autocomplete

    /** When {@code true}, the field's current value is automatically selected on focus. Defaults to {@code true}. */
    Boolean autoSelect

    /** CSS text transformation applied to the displayed value (e.g. uppercase). Defaults to {@link TextTransform#NONE}. */
    TextTransform textTransform

    /** When {@code true}, the {@code onChange} event is fired asynchronously. Defaults to {@code false}. */
    Boolean onChangeAsync

    /**
     * Creates a {@code TextField} instance configured from the supplied argument map.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code inputType} ({@link TextFieldInputType}, default {@link TextFieldInputType#TEXT}),
     *             {@code inputMode} ({@link TextFieldInputMode}, default {@link TextFieldInputMode#TEXT}),
     *             {@code icon} ({@link String}),
     *             {@code prefix} ({@link String}),
     *             {@code maxSize} ({@link Integer}),
     *             {@code placeholder} ({@link String}),
     *             {@code autocomplete} ({@link Boolean}, default {@code false}),
     *             {@code autoSelect} ({@link Boolean}, default {@code true}),
     *             {@code textTransform} ({@link TextTransform}, default {@link TextTransform#NONE}),
     *             {@code renderTextPrefix} ({@link Boolean}, default {@code false}),
     *             {@code onChangeAsync} ({@link Boolean}, default {@code false}),
     *             plus all keys accepted by {@link Control#Control(Map)}
     */
    TextField(Map args) {
        super(args)

        valueType = Type.TEXT
        inputType = args.inputType as TextFieldInputType ?: TextFieldInputType.TEXT
        inputMode = args.inputMode as TextFieldInputMode ?: TextFieldInputMode.TEXT

        icon = args.icon ?: ''
        prefix = args.prefix ?: ''
        maxSize = args.maxSize as Integer ?: 0
        placeholder = args.placeholder == null ? '' : args.placeholder
        autocomplete = args.autocomplete == null ? false : args.autocomplete
        autoSelect = args.autoSelect == null ? true : args.autoSelect
        textTransform = args.textTransform as TextTransform ?: TextTransform.NONE
        prettyPrinterProperties.renderTextPrefix = args.renderTextPrefix == null ? false : args.renderTextPrefix

        onChangeAsync = args.onChangeAsync == null ? false : args.onChangeAsync
        //onChangeMinChars = args.onChangeMinChars ?: 0 // forse in futuro

        actions = createControl(
                class: Button,
                id: 'actions',
                group: true,
                dontCreateDefaultAction: true,
        )
    }

    /**
     * Returns the HTML {@code type} attribute value as a lower-case string.
     *
     * @return the input type string (e.g. {@code "text"}, {@code "password"})
     */
    String getInputType() {
        return inputType.toString().toLowerCase()
    }

    /**
     * Returns the HTML {@code inputmode} attribute value as a lower-case string.
     *
     * @return the input mode string (e.g. {@code "text"}, {@code "numeric"}, {@code "decimal"})
     */
    String getInputMode() {
        return inputMode.toString().toLowerCase()
    }

    /**
     * Adds an action to the adjacent action {@link Button}.
     * Defaults {@code loading} to {@code false} if not specified.
     *
     * @param args action configuration arguments forwarded to {@link Button#addAction(Map)}
     * @return this control
     */
    Control addAction(Map args) {
        args.loading = args.loading != null ? args.loading : false
        actions.addAction(args)
        return this
    }

    /**
     * Removes an action from the adjacent action {@link Button}.
     *
     * @param args action identification arguments forwarded to {@link Button#removeAction(Map)}
     */
    void removeAction(Map args) {
        actions.removeAction(args)
    }

    /**
     * Serialises this control's properties to JSON, adding {@link #autocomplete},
     * {@link #autoSelect}, {@link #textTransform}, and {@link #onChangeAsync}.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this control's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                autocomplete: autocomplete,
                autoSelect: autoSelect,
                textTransform: textTransform as String,
                onChangeAsync: onChangeAsync,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    /**
     * Serialises the current text value to a JSON string containing the {@code type}
     * and {@code value} fields. The value is rendered via the control's pretty-printer.
     *
     * @return a JSON string representing the current text value
     */
    @Override
    String getValueAsJSON() {
        Map valueMap = [
                type: valueType,
                value: prettyPrint(value, prettyPrinterProperties),
        ]

        return Elements.encodeAsJSON(valueMap)
    }
}
