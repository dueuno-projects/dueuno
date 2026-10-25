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
 * Component that listens for global keyboard events and dispatches a server-side action
 * when a configured trigger key is detected.
 * It also dispatches keyboard hotkeys configured on {@link dueuno.elements.components.Link}
 * instances by triggering the link's normal click handler.
 * <p>
 * {@code KeyPress} is primarily designed to support barcode-scanner input. The client
 * distinguishes scanner input from human typing and triggers the action when the configured
 * {@code triggerKey} is pressed (typically {@code Enter}).
 * </p>
 * <p>
 * The client buffers the accumulated characters and sends them to the server as the
 * {@code _21KeyPressed} request parameter, accessible via {@link #getKeyPressed()}.
 * </p>
 * <p>
 * The target controller action is configured by the {@code invocationDefinition} and
 * defaults to the {@code keyPress} controller's {@code onKeyPress} action.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class KeyPress extends Component {

    /** The invocation definition describing the controller action to invoke on key press. */
    InvocationDefinition invocationDefinition

    /** Whether keyboard listening is active. Defaults to {@code true}. */
    Boolean enabled

    /**
     * The key that signals end-of-input and triggers the server-side action.
     * Defaults to {@code "Enter"}.
     */
    String triggerKey

    /** Whether scanner input is suppressed from a focused input field. Defaults to {@code false}. */
    Boolean hideInput

    /**
     * Creates a {@code KeyPress} component from the given argument map.
     * All {@link Component} arguments are supported, plus:
     * <ul>
     *     <li>{@code enabled} — defaults to {@code true}</li>
     *     <li>{@code triggerKey} — defaults to {@code "Enter"}</li>
     *     <li>{@code hideInput} — suppress scanner text from a focused input; defaults to {@code false}</li>
     *     <li>{@code controller} / {@code action} / {@code namespace} / {@code params} / {@code submit}
     *         — invocation definition for the triggered action; defaults to {@code keyPress/onKeyPress}</li>
     * </ul>
     *
     * @param args map of component and key-press properties
     */
    KeyPress(Map args) {
        super(args)

        viewPath = '/dueuno/elements/'

        enabled = args.enabled == null ? true : args.enabled
        triggerKey = args.triggerKey ?: 'Enter'
        hideInput = args.hideInput == null ? false : args.hideInput as Boolean

        invocationDefinition = new InvocationDefinition(args)
        invocationDefinition.controller = args.controller ?: 'keyPress'
        invocationDefinition.action = args.action ?: 'onKeyPress'

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
     * {@code invocationDefinition}. Called automatically on construction and whenever the
     * invocation definition is modified.
     */
    private void setOnKeyPressEvent() {
        Map event = invocationDefinition.properties
        Map eventOverride = [
                event: 'keypress',
        ] as Map
        on(event + eventOverride)
    }

    /**
     * Returns a JSON string of the client-side properties needed to configure the
     * key-press listener on the frontend ({@code enabled}, {@code triggerKey}, {@code hideInput}).
     *
     * @return a JSON representation of the key-press configuration
     */
    @Override
    String getPropertiesAsJSON() {
        Map thisProperties = [
            enabled   : enabled,
            triggerKey: triggerKey,
            hideInput : hideInput,
        ]
        return Elements.encodeAsJSON(thisProperties)
    }

    /**
     * Returns the controller namespace used in the key-press action link.
     *
     * @return the namespace, or an empty string if none is set
     */
    String getNamespace() {
        return invocationDefinition.namespace
    }

    /**
     * Sets the controller namespace for the key-press action link and re-registers the event.
     *
     * @param value the namespace to set
     */
    void setNamespace(String value) {
        invocationDefinition.namespace = value
        setOnKeyPressEvent()
    }

    /**
     * Returns the controller name used in the key-press action link.
     *
     * @return the controller name
     */
    String getController() {
        return invocationDefinition.controller
    }

    /**
     * Sets the controller name for the key-press action link and re-registers the event.
     *
     * @param value the controller name to set
     */
    void setController(String value) {
        invocationDefinition.controller = value
        setOnKeyPressEvent()
    }

    /**
     * Returns the action name used in the key-press action link.
     *
     * @return the action name
     */
    String getAction() {
        return invocationDefinition.action
    }

    /**
     * Sets the action name for the key-press action link and re-registers the event.
     *
     * @param value the action name to set
     */
    void setAction(String value) {
        invocationDefinition.action = value
        setOnKeyPressEvent()
    }

    /**
     * Returns the request parameters included in the key-press action link.
     *
     * @return the parameters map
     */
    Map getParams() {
        return invocationDefinition.params
    }

    /**
     * Sets the request parameters for the key-press action link and re-registers the event.
     *
     * @param value the parameters map to set
     */
    void setParams(Map value) {
        invocationDefinition.params = value
        setOnKeyPressEvent()
    }

    /**
     * Returns the list of component names whose data is submitted with the key-press action.
     *
     * @return the submit component name list
     */
    List<String> getSubmit() {
        return invocationDefinition.submit
    }

    /**
     * Sets the list of component names to submit with the key-press action and re-registers the event.
     *
     * @param value the submit component name list
     */
    void setSubmit(List<String> value) {
        invocationDefinition.submit = value
        setOnKeyPressEvent()
    }
}
