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
import dueuno.core.WebRequestAware
import dueuno.exceptions.ElementsException
import dueuno.types.Types
import groovy.contracts.Requires
import groovy.transform.CompileStatic

/**
 * Represents a client-side UI transition.
 * <p>
 * A {@code Transition} collects a sequence of commands and component updates
 * that will be executed by the browser after a server-side action completes.
 * It is the primary mechanism used by Elements to perform AJAX-style UI
 * updates without reloading the page.
 * </p>
 *
 * <p>
 * A transition may:
 * </p>
 * <ul>
 *     <li>Redirect the browser to another URL or action.</li>
 *     <li>Render page content.</li>
 *     <li>Add, remove, replace, or append components.</li>
 *     <li>Update component properties.</li>
 *     <li>Trigger component events.</li>
 *     <li>Invoke client-side component methods.</li>
 *     <li>Display informational, error, confirmation, or option messages.</li>
 *     <li>Show or hide loading indicators.</li>
 * </ul>
 *
 * <p>
 * Transitions are normally created and returned by controller actions,
 * component events, or other server-side handlers that interact with
 * the Elements frontend.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class Transition implements WebRequestAware {

    private String view
    private List<TransitionCommand> commands
    private List<Component> components

    Transition() {
        view = '/dueuno/elements/Transition'
        commands = []
        components = []
    }

    /**
     * Returns the Groovy Server Pages view path used to render this transition.
     *
     * @return the GSP view path for the transition
     */
    String getView() {
        return view
    }

    /**
     * Returns the model map used when rendering the transition view.
     * The map contains a single entry {@code c} pointing to this transition instance.
     *
     * @return a map with this transition bound to the key {@code "c"}
     */
    Map getModel() {
        return [c: this]
    }

    /**
     * Clears all commands and components accumulated in this transition,
     * resetting it to an empty state.
     */
    void clear() {
        commands.clear()
        components.clear()
    }

    /**
     * Adds an already-instantiated component to this transition so that it can
     * be referenced by subsequent commands.
     *
     * @param component the component instance to add
     * @return the same component instance, for chaining
     */
    public <T> T addComponent(T component) {
        components.add(component as Component)
        return component
    }

    /**
     * Creates a new component of the given class and adds it to this transition.
     * If no {@code id} is supplied, the lower-cased simple class name is used.
     *
     * @param clazz the component class to instantiate
     * @param id    optional component identifier; defaults to the lower-cased simple class name
     * @param args  optional map of constructor/property arguments passed to the component
     * @return the newly created component instance
     */
    public <T> T addComponent(Class<T> clazz, String id = null, Map args = [:]) {
        args['class'] = clazz
        args['id'] = id ?: clazz.simpleName.toLowerCase()
        return addComponent(args)
    }

    /**
     * Creates a new component from an argument map and adds it to this transition.
     * The map must contain at least {@code id} and {@code class} entries.
     *
     * @param args a map with at least {@code id} (String) and {@code class} (Class) keys,
     *             plus any additional constructor/property arguments
     * @return the newly created component instance
     */
    @Requires({ args.id && args.class })
    public <T> T addComponent(Map args) {
        Class<T> clazz = args.class as Class<T>
        String id = args.id

        args.remove('class')
        args.remove('id')
        T component = Component.createInstance(clazz, id, args)
        addComponent(component as Component)

        return component
    }

    /**
     * Adds a redirect command to this transition, causing the browser to navigate
     * to the URL or controller/action described by {@code args}.
     *
     * @param args a map of link definition parameters (e.g. {@code controller}, {@code action},
     *             {@code url}, {@code params})
     */
    void redirect(Map args) {
        ComponentEvent event = new ComponentEvent(args)
        initializeWithRequestData(event)
        addCommand(
                TransitionCommandMethod.REDIRECT,
                null,
                null,
                event.asMap(),
        )
    }

    /**
     * Adds a content-rendering command to this transition that replaces the main
     * page content area with the supplied {@link PageContent} component.
     *
     * @param content the {@link PageContent} component to render
     */
    void renderContent(PageContent content) {
        addComponent(content)
        addCommand(
                TransitionCommandMethod.CONTENT,
                null,
                null,
                null,
        )
    }

    /**
     * Adds a command to remove the specified component from the DOM.
     *
     * @param component the identifier of the component to remove
     */
    void remove(String component) {
        addCommand(
                TransitionCommandMethod.REMOVE,
                component,
                null,
                null,
        )
    }

    /**
     * Adds a command to replace an existing component in the DOM with a new one.
     *
     * @param component    the identifier of the component to replace
     * @param newComponent the identifier of the replacement component
     */
    void replace(String component, String newComponent) {
        addCommand(
                TransitionCommandMethod.REPLACE,
                component,
                null,
                newComponent,
        )
    }

    /**
     * Adds a command to append a new component as a child of the specified parent component.
     *
     * @param parentComponent the identifier of the parent component
     * @param newComponent    the identifier of the component to append
     */
    void append(String parentComponent, String newComponent) {
        addCommand(
                TransitionCommandMethod.APPEND,
                parentComponent,
                null,
                newComponent,
        )
    }

    /**
     * Adds a command to show or hide the global loading indicator.
     *
     * @param show {@code true} to show the loading indicator, {@code false} to hide it
     */
    void loading(Boolean show) {
        addCommand(
                TransitionCommandMethod.LOADING,
                null,
                null,
                show
        )
    }

    /**
     * Adds a command to trigger a named event on the specified component.
     *
     * @param component the identifier of the target component
     * @param event     the name of the event to trigger
     */
    void trigger(String component, String event) {
        addCommand(
                TransitionCommandMethod.TRIGGER,
                component,
                event,
                null
        )
    }

    /**
     * Adds a command to invoke a client-side method on the specified component.
     *
     * @param component the identifier of the target component
     * @param method    the name of the client-side method to call
     * @param args      optional map of arguments to pass to the method
     */
    void call(String component, String method, Map args = [:]) {
        addCommand(
                TransitionCommandMethod.CALL,
                component,
                method,
                args,
        )
    }

    /**
     * Adds a command to close the currently open modal dialog.
     */
    void closeModal() {
        call('modal', 'close')
    }

    /**
     * Adds a command to set the {@code value} property of the specified control,
     * optionally triggering its change event.
     *
     * @param component the identifier of the target component
     * @param value     the new value to assign
     * @param trigger   if {@code true} (default), the component's change event is triggered
     */
    void setValue(String component, Object value, Boolean trigger = true) {
        set(component, 'value', value, [], trigger)
    }

    /**
     * Adds a command to set an arbitrary property of the specified component.
     * If the property is {@code "value"} and the value is an {@link Enum}, it is
     * converted to its string representation. String values are automatically
     * resolved through the i18n message source when a web request is present.
     *
     * @param component  the identifier of the target component
     * @param property   the name of the property to set
     * @param value      the new value to assign
     * @param valueArgs  optional list of arguments used for i18n message interpolation
     * @param trigger    if {@code true} (default), the component's change event is triggered
     */
    void set(String component, String property, Object value, List valueArgs = [], Boolean trigger = true) {
        if (property == 'value') {
            if (value in Enum) value = value.toString()
        }

        if (value in String && hasRequest()) {
            value = message(value as String, valueArgs)
        }

        addCommand(
                TransitionCommandMethod.SET,
                component,
                property,
                value,
                trigger
        )
    }

    /**
     * Displays an informational message box.
     *
     * @param msg     the i18n message key or literal message text
     * @param onClick optional {@link ComponentEvent} to invoke when the message is clicked
     */
    void infoMessage(String msg, ComponentEvent onClick = null) {
        infoMessage('info', msg, null, onClick)
    }

    /**
     * Displays an informational message box with interpolation arguments.
     *
     * @param msg     the i18n message key or literal message text
     * @param msgArgs arguments used for i18n message interpolation
     * @param onCLick optional {@link ComponentEvent} to invoke when the message is clicked
     */
    void infoMessage(String msg, List msgArgs, ComponentEvent onCLick = null) {
        infoMessage('info', msg, msgArgs, onCLick)
    }

    /**
     * Displays an error message box.
     *
     * @param msg     the i18n message key or literal message text
     * @param onCLick optional {@link ComponentEvent} to invoke when the message is clicked
     */
    void errorMessage(String msg, ComponentEvent onCLick = null) {
        infoMessage('error', msg, null, onCLick)
    }

    /**
     * Displays an error message box with interpolation arguments.
     *
     * @param msg     the i18n message key or literal message text
     * @param msgArgs arguments used for i18n message interpolation
     * @param onCLick optional {@link ComponentEvent} to invoke when the message is clicked
     */
    void errorMessage(String msg, List msgArgs, ComponentEvent onCLick = null) {
        infoMessage('error', msg, msgArgs, onCLick)
    }

    /**
     * Internal helper that resolves the message text and dispatches a {@code call} command
     * targeting the {@code messagebox} component with the specified type.
     *
     * @param type    the message type (e.g. {@code "info"} or {@code "error"})
     * @param msg     the i18n message key or literal message text
     * @param msgArgs optional arguments used for i18n message interpolation
     * @param onClick optional {@link ComponentEvent} to invoke when the message is clicked
     */
    void infoMessage(String type, String msg, List msgArgs = [], ComponentEvent onClick = null) {
        String infoMessage = hasRequest()
                ? message(msg, msgArgs)
                : msg

        Map args = [:]
        args.infoMessage = infoMessage

        if (onClick && (onClick.controller || onClick.action || onClick.url)) {
            initializeWithRequestData(onClick)
            args.click = onClick.asMap()
        }

        call('messagebox', type, args)
    }

    /**
     * Displays a confirmation message box with a single confirm button.
     * Equivalent to calling {@link #optionsMessage(String, List, ComponentEvent, ComponentEvent)}
     * with no cancel action.
     *
     * @param msg            the i18n message key or literal message text
     * @param onClickConfirm the {@link ComponentEvent} invoked when the user confirms
     */
    void confirmMessage(String msg, ComponentEvent onClickConfirm) {
        optionsMessage(msg, [], null, onClickConfirm)
    }

    /**
     * Displays a confirmation message box with interpolation arguments and a single confirm button.
     *
     * @param msg            the i18n message key or literal message text
     * @param msgArgs        arguments used for i18n message interpolation
     * @param onClickConfirm the {@link ComponentEvent} invoked when the user confirms
     */
    void confirmMessage(String msg, List msgArgs, ComponentEvent onClickConfirm) {
        optionsMessage(msg, msgArgs, null, onClickConfirm)
    }

    /**
     * Displays an options message box with both cancel and confirm buttons.
     *
     * @param msg             the i18n message key or literal message text
     * @param onClickCancel   the {@link ComponentEvent} invoked when the user cancels
     * @param onClickConfirm  the {@link ComponentEvent} invoked when the user confirms
     */
    void optionsMessage(String msg, ComponentEvent onClickCancel, ComponentEvent onClickConfirm) {
        optionsMessage(msg, [], onClickCancel, onClickConfirm)
    }

    /**
     * Displays an options message box with interpolation arguments and both cancel and confirm buttons.
     *
     * @param msg             the i18n message key or literal message text
     * @param msgArgs         arguments used for i18n message interpolation
     * @param onClickCancel   the {@link ComponentEvent} invoked when the user cancels; may be {@code null}
     * @param onClickConfirm  the {@link ComponentEvent} invoked when the user confirms
     */
    void optionsMessage(String msg, List msgArgs, ComponentEvent onClickCancel, ComponentEvent onClickConfirm) {
        String confirmMessage = hasRequest()
                ? message(msg, msgArgs)
                : msg

        Map args = [:]
        args.confirmMessage = confirmMessage

        if (onClickCancel && (onClickCancel.controller || onClickCancel.action || onClickCancel.url)) {
            initializeWithRequestData(onClickCancel)
            args.clickCancel = onClickCancel.asMap()
        }

        if (onClickConfirm && (onClickConfirm.controller || onClickConfirm.action || onClickConfirm.url)) {
            initializeWithRequestData(onClickConfirm)
            args.clickConfirm = onClickConfirm.asMap()
        }

        call('messagebox', 'confirm', args)
    }

    private void addCommand(TransitionCommandMethod method, String component, String property, Object value, Boolean trigger = true) {
        TransitionCommand command = new TransitionCommand()
        command.method = method as String
        command.component = component
        command.property = property
        command.value = Types.serializeValue(value)
        command.trigger = trigger
        commands.add(command)
    }

    private void initializeWithRequestData(LinkDefinition componentEventData) {
        if (!componentEventData) {
            return
        }

        if (!hasRequest() && !componentEventData.controller) {
            throw new ElementsException("The transition is outside a web request, a controller name must be specified (Eg. 't.redirect(controller: 'myController')')")
        }

        if (!hasRequest()) {
            return
        }

        if (componentEventData.action && !componentEventData.controller) {
            componentEventData.controller = getControllerName()
        }

        if (!componentEventData.action && componentEventData.controller) {
            componentEventData.action = componentEventData.action ?: 'index'
        }
    }

    String getCommandsAsJSON() {
        return Elements.encodeAsJSON(commands)
    }

    List<Component> getComponents() {
        return components
    }
}
