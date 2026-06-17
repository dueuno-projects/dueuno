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
package dueuno.elements

import dueuno.core.LinkDefinition
import dueuno.elements.controls.HiddenField
import groovy.transform.CompileStatic

/**
 * Component that listens for global keyboard events and dispatches a server-side action
 * when a configured trigger key is detected.
 * <p>
 * {@code KeyPress} is primarily designed to support barcode-scanner input. It distinguishes
 * between human typing and scanner input by measuring the speed at which characters arrive:
 * if the characters arrive faster than {@link #readingSpeed} milliseconds apart, the input
 * is treated as coming from a scanner and the action is triggered when the {@link #triggerKey}
 * is pressed (typically {@code Enter}).
 * </p>
 * <p>
 * The accumulated characters are stored in an internal {@link HiddenField} ({@link #buffer})
 * and sent to the server as the {@code _21KeyPressed} request parameter, accessible via
 * {@link #getKeyPressed()}.
 * </p>
 * <p>
 * The target controller action is defined by the {@link #linkDefinition} and defaults to
 * the {@code keyPress} controller's {@code onKeyPress} action.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class KeyPress extends Component {

    /** The link definition describing the controller action to invoke on key press. */
    LinkDefinition linkDefinition

    /** Hidden form field used to buffer the characters received from the keyboard or scanner. */
    HiddenField buffer

    /** Whether keyboard listening is active. Defaults to {@code true}. */
    Boolean enabled

    /**
     * The key that signals end-of-input and triggers the server-side action.
     * Defaults to {@code "Enter"}.
     */
    String triggerKey

    /**
     * Maximum inter-character delay in milliseconds used to distinguish scanner input from
     * human typing. If characters arrive faster than this value, the input is treated as
     * coming from a barcode scanner. Defaults to {@code 20} ms.
     * Only evaluated when the focus is on an {@code <input>} element.
     */
    Integer readingSpeed

    /**
     * Time in milliseconds after which the input buffer is automatically cleared to
     * prevent accidental partial input from being submitted. Defaults to {@code 500} ms.
     */
    Integer bufferTimeout

    /**
     * When {@code true}, characters typed while an {@code <input>} element has focus are
     * suppressed (not displayed in the field). Defaults to {@code false}.
     */
    Boolean hideInput

    /**
     * Creates a {@code KeyPress} component from the given argument map.
     * All {@link Component} arguments are supported, plus:
     * <ul>
     *     <li>{@code enabled} — defaults to {@code true}</li>
     *     <li>{@code triggerKey} — defaults to {@code "Enter"}</li>
     *     <li>{@code readingSpeed} — max inter-character delay for scanner detection (ms); defaults to {@code 20}</li>
     *     <li>{@code bufferTimeout} — buffer auto-clear delay (ms); defaults to {@code 500}</li>
     *     <li>{@code hideInput} — defaults to {@code false}</li>
     *     <li>{@code controller} / {@code action} / {@code namespace} / {@code params} / {@code submit}
     *         — link definition for the triggered action; defaults to {@code keyPress/onKeyPress}</li>
     * </ul>
     *
     * @param args map of component and key-press properties
     */
    KeyPress(Map args) {
        super(args)

        viewPath = '/dueuno/elements/'

        enabled = args.enabled == null ? true : args.enabled
        triggerKey = args.triggerKey ?: 'Enter'

        // a barcode reader is typically much faster at typing than a human...
        // let's use this principle to understand if the typing comes from a reader (ms)
        // (only evaluated if focus is on an "input" element)
        readingSpeed = args.readingSpeed == null ? 20 : args.readingSpeed as Integer

        // to prevent accidental typing, the buffer empties after a certain time (ms)
        bufferTimeout = args.bufferTimeout == null ? 500 : args.bufferTimeout as Integer

        // avoid writing text if focus is on an "input" element (default false)
        hideInput = args.hideInput == null ? false : args.hideInput

        linkDefinition = new LinkDefinition(args)
        linkDefinition.controller = args.controller ?: 'keyPress'
        linkDefinition.action = args.action ?: 'onKeyPress'

        buffer = createControl(
                class: HiddenField,
                id: 'buffer',
        )

        setOnKeyPressEvent()
    }

    /**
     * Returns the value that was accumulated in the input buffer during the current request,
     * i.e. the characters that were typed or scanned before the trigger key was pressed.
     *
     * @return the buffered key-press string from the {@code _21KeyPressed} request parameter
     */
    String getKeyPressed() {
        return getGrailsWebRequest().params._21KeyPressed
    }

    /**
     * (Re-)registers the {@code keypress} event on this component using the current
     * {@link #linkDefinition}. Called automatically on construction and whenever the
     * link definition is modified.
     */
    private void setOnKeyPressEvent() {
        on(linkDefinition.properties + [
                event: 'keypress',
        ])
    }

    /**
     * Returns a JSON string of the client-side properties needed to configure the
     * key-press listener on the frontend ({@link #enabled}, {@link #triggerKey},
     * {@link #readingSpeed}, {@link #bufferTimeout}, {@link #hideInput}).
     *
     * @return a JSON representation of the key-press configuration
     */
    @Override
    String getPropertiesAsJSON() {
        Map thisProperties = [
                enabled: enabled,
                triggerKey: triggerKey,
                readingSpeed: readingSpeed,
                bufferTimeout: bufferTimeout,
                hideInput: hideInput,
        ]
        return Elements.encodeAsJSON(thisProperties)
    }

    /**
     * Returns the controller namespace used in the key-press action link.
     *
     * @return the namespace, or an empty string if none is set
     */
    String getNamespace() {
        return linkDefinition.namespace
    }

    /**
     * Sets the controller namespace for the key-press action link and re-registers the event.
     *
     * @param value the namespace to set
     */
    void setNamespace(String value) {
        linkDefinition.namespace = value
        setOnKeyPressEvent()
    }

    /**
     * Returns the controller name used in the key-press action link.
     *
     * @return the controller name
     */
    String getController() {
        return linkDefinition.controller
    }

    /**
     * Sets the controller name for the key-press action link and re-registers the event.
     *
     * @param value the controller name to set
     */
    void setController(String value) {
        linkDefinition.controller = value
        setOnKeyPressEvent()
    }

    /**
     * Returns the action name used in the key-press action link.
     *
     * @return the action name
     */
    String getAction() {
        return linkDefinition.action
    }

    /**
     * Sets the action name for the key-press action link and re-registers the event.
     *
     * @param value the action name to set
     */
    void setAction(String value) {
        linkDefinition.action = value
        setOnKeyPressEvent()
    }

    /**
     * Returns the request parameters included in the key-press action link.
     *
     * @return the parameters map
     */
    Map getParams() {
        return linkDefinition.params
    }

    /**
     * Sets the request parameters for the key-press action link and re-registers the event.
     *
     * @param value the parameters map to set
     */
    void setParams(Map value) {
        linkDefinition.params = value
        setOnKeyPressEvent()
    }

    /**
     * Returns the list of component names whose data is submitted with the key-press action.
     *
     * @return the submit component name list
     */
    List<String> getSubmit() {
        return linkDefinition.submit
    }

    /**
     * Sets the list of component names to submit with the key-press action and re-registers the event.
     *
     * @param value the submit component name list
     */
    void setSubmit(List<String> value) {
        linkDefinition.submit = value
        setOnKeyPressEvent()
    }
}
