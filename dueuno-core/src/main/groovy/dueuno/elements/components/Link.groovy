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
package dueuno.elements.components

import dueuno.elements.core.Hotkey
import dueuno.elements.core.InvocationDefinition
import groovy.transform.CompileStatic

/**
 * A {@link Label}-based component that renders a navigable link.
 * <p>
 * {@code Link} wraps a {@link InvocationDefinition} and registers a client-side
 * event (default: {@code "click"}) that triggers the configured server-side action.
 * All navigation properties ({@code controller}, {@code action}, {@code url}, etc.)
 * are delegated to the underlying {@code invocationDefinition}; changing any of them
 * automatically re-registers the click event via {@link #setOnEvent(String)}.
 * A hotkey can be set with {@code hotkey} using a
 * {@link Hotkey} value, for example {@code Hotkey.F1}.
 * </p>
 * <p>
 * Additional render options (modal display, animations, scroll behaviour, etc.) are
 * stored in {@link dueuno.elements.core.PageRenderProperties} accessible through
 * the {@code invocationDefinition} property.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class Link extends Label {

    /** The invocation definition holding all navigation and render properties. */
    InvocationDefinition invocationDefinition

    /** The DOM event name that triggers the navigation action. Defaults to {@code "click"}. */
    String eventName

    /** Hotkey that triggers this link. */
    Hotkey hotkey

    /**
     * Creates a {@code Link} component from the given argument map.
     * All {@link Label} arguments are supported, plus all {@link InvocationDefinition}
     * properties and an optional {@code hotkey} from {@link Hotkey}.
     * If no {@code action} is provided it defaults to {@code "index"}.
     * An optional {@code onClick} key may specify an action name override for the click event.
     *
     * @param args map of link and label properties
     */
    Link(Map args = [:]) {
        super(args)

        eventName = 'click'
        invocationDefinition = new InvocationDefinition(args)

        tag = args.tag == null ? false : args.tag

        html = args.html
        containerSpecs.label = ''
        containerSpecs.help = ''

        setHotkey(args.hotkey as Hotkey)
        setEvent(args.onClick as String)
    }

    /**
     * Registers (or re-registers) the client-side event that triggers this link's action.
     * Uses the current {@code invocationDefinition} properties, resolved i18n messages for
     * {@code infoMessage} and {@code confirmMessage}, and the optional {@code onEvent}
     * action override.
     *
     * @param onEvent optional action name to use instead of {@link InvocationDefinition#action};
     *                when {@code null} the definition's controller action is used
     */
    void setEvent(String onEvent = null) {
        Map invocationDefinitionProperties = this.invocationDefinition.properties
        Map eventOverrides = [
                event: eventName,
                action: onEvent ?: this.invocationDefinition.action,
                infoMessage: message(this.invocationDefinition.infoMessage, this.invocationDefinition.infoMessageArgs),
                confirmMessage: message(this.invocationDefinition.confirmMessage, this.invocationDefinition.confirmMessageArgs),
        ] as Map
        on(invocationDefinitionProperties + eventOverrides)
    }

    /**
     * Returns a JSON string of this link's client-side properties, adding its
     * {@code loading} flag and client-side {@code hotkey} on top of the inherited
     * {@link Label} properties.
     *
     * @param properties additional properties to merge
     * @return a JSON representation of all component properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
            loading: loading,
            hotkey: hotkey?.key,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    /** Returns the hotkey that triggers this link. */
    Hotkey getHotkey() { return hotkey }

    /**
     * Sets the hotkey that triggers this link. Supported values are
     * {@link Hotkey#F1} through {@link Hotkey#F12} and
     * {@link Hotkey#ENTER}. Every hotkey requires at least one
     * {@code Ctrl}/{@code Control} or {@code Meta}/{@code Command}. Left and
     * right variants work identically; {@code Shift}, {@code Alt}/{@code Option},
     * and {@code Fn} are not included.
     * {@code null} clears the hotkey.
     *
     * @param value the hotkey to register
     */
    void setHotkey(Hotkey value) {
        hotkey = value
    }

    /**
     * Returns a debug-friendly string showing the link path and query parameters.
     *
     * @return a string in the form {@code "Link: /namespace/controller/action?key=value&..."}
     */
    String toString() {
        String ns = invocationDefinition.namespace ?: ''
        String path = "${ns}/${invocationDefinition.controller}/${invocationDefinition.action}"
        String queryParams = invocationDefinition.params.collect { "${it.key}=${it.value}" }.join('&')
        String queryString = queryParams ? "?${queryParams}" : ''
        return "Link: ${path}${queryString}"
    }

    /**
     * Returns a simplified URL string suitable for development/debugging purposes.
     * Returns the explicit {@code url} if set, a {@code /controller/action} path if both
     * are present, or {@code null} otherwise.
     *
     * @return a dev-friendly URL string, or {@code null}
     */
    String getDevUrl() {
        if (url) return url
        if (controller && action) return "/${controller}/${action}"
        return null
    }

    /** @see InvocationDefinition#target */
    String getTarget() { return invocationDefinition.target }
    /** @see InvocationDefinition#target */
    void setTarget(String value) { invocationDefinition.target = value }

    /** @see InvocationDefinition#namespace */
    String getNamespace() { return invocationDefinition.namespace }
    /** Sets the controller namespace and re-registers the click event. */
    void setNamespace(String value) {
        invocationDefinition.namespace = value
        setEvent()
    }

    /** @see InvocationDefinition#controller */
    String getController() { return invocationDefinition.controller }
    /** Sets the controller name and re-registers the click event. */
    void setController(String value) {
        invocationDefinition.controller = value
        setEvent()
    }

    /** @see InvocationDefinition#action */
    String getAction() { return invocationDefinition.action }
    /** Sets the action name and re-registers the click event. */
    void setAction(String value) {
        invocationDefinition.action = value
        setEvent()
    }

    /** @see InvocationDefinition#call */
    String getCall() { return invocationDefinition.call }
    /** Sets the client-side function name and re-registers the click event. */
    void setCall(String value) {
        invocationDefinition.call = value
        setEvent()
    }

    /** @see InvocationDefinition#params */
    Map getParams() { return invocationDefinition.params }
    /** Sets the request parameters and re-registers the click event. */
    void setParams(Map value) {
        invocationDefinition.params = value
        setEvent()
    }

    /** @see InvocationDefinition#fragment */
    String getFragment() { return invocationDefinition.fragment }
    /** Sets the URL fragment and re-registers the click event. */
    void setFragment(String value) {
        invocationDefinition.fragment = value
        setEvent()
    }

    /** @see InvocationDefinition#path */
    String getPath() { return invocationDefinition.path }
    /** Sets the path and re-registers the click event. */
    void setPath(String value) {
        invocationDefinition.path = value
        setEvent()
    }

    /** @see InvocationDefinition#url */
    String getUrl() { return invocationDefinition.url }
    /** Sets the explicit URL and re-registers the click event. */
    void setUrl(String value) {
        invocationDefinition.url = value
        setEvent()
    }

    /** @see InvocationDefinition#submit */
    List<String> getSubmit() { return invocationDefinition.submit }

    /**
     * Convenience overload that wraps a single component name in a list before delegating
     * to {@link #setSubmit(List)}.
     *
     * @param value the name of the component to submit
     */
    void setSubmit(String value) { setSubmit([value]) }

    /** Sets the submit component list and re-registers the click event. */
    void setSubmit(List<String> value) {
        invocationDefinition.submit = value
        setEvent()
    }

    /** @see InvocationDefinition#direct */
    Boolean getDirect() { return invocationDefinition.direct }
    /** @see InvocationDefinition#direct */
    void setDirect(Boolean value) { invocationDefinition.direct = value }

    /** Whether this link opens its target in a modal dialog. */
    Boolean getModal() { return invocationDefinition.renderProperties.modal }
    /** Sets whether this link opens its target in a modal dialog and re-registers the click event. */
    void setModal(Boolean value) {
        invocationDefinition.renderProperties.modal = value
        setEvent()
    }

    /** Whether this link renders in a small (compact) style. */
    Boolean getSmall() { return invocationDefinition.renderProperties.small }
    /** Sets the small (compact) rendering style and re-registers the click event. */
    void setSmall(Boolean value) {
        invocationDefinition.renderProperties.small = value
        setEvent()
    }

    /** Whether this link renders in a large style. */
    Boolean getLarge() { return invocationDefinition.renderProperties.large }
    /** Sets the large rendering style and re-registers the click event. */
    void setLarge(Boolean value) {
        invocationDefinition.renderProperties.large = value
        setEvent()
    }

    /** The CSS animation name applied when navigating via this link. */
    String getAnimate() { return invocationDefinition.renderProperties.animate }
    /** Sets the CSS animation for navigation and re-registers the click event. */
    void setAnimate(String value) {
        invocationDefinition.renderProperties.animate = value
        setEvent()
    }

    /** Whether a close button is shown when the target is displayed in a modal. */
    Boolean getCloseButton() { return invocationDefinition.renderProperties.closeButton }
    /** Sets whether a close button is shown in the modal and re-registers the click event. */
    void setCloseButton(Boolean value) {
        invocationDefinition.renderProperties.closeButton = value
        setEvent()
    }

    /** The scroll behaviour applied when navigating via this link (e.g. {@code "reset"}). */
    String getScroll() { return invocationDefinition.renderProperties.scroll }
    /** Sets the scroll behaviour and re-registers the click event. */
    void setScroll(String value) {
        invocationDefinition.renderProperties.scroll = value
        setEvent()
    }

    /** @see InvocationDefinition#getTargetNew() */
    Boolean getTargetNew() { return invocationDefinition.targetNew }
    /** Sets whether the link opens in a new tab and re-registers the click event. */
    void setTargetNew(Boolean value) {
        invocationDefinition.targetNew = value
        setEvent()
    }

    /** @see InvocationDefinition#loading */
    Boolean getLoading() { return invocationDefinition.loading }
    /** Sets the loading indicator flag and re-registers the click event. */
    void setLoading(Boolean value) {
        invocationDefinition.loading = value
        setEvent()
    }

    /** @see InvocationDefinition#infoMessage */
    String getInfoMessage() { return invocationDefinition.infoMessage }
    /** Sets the info message and re-registers the click event. */
    void setInfoMessage(String value) {
        invocationDefinition.infoMessage = value
        setEvent()
    }
    /** Sets the info message interpolation arguments and re-registers the click event. */
    void setInfoMessageArgs(List value) {
        invocationDefinition.infoMessageArgs = value
        setEvent()
    }

    /** @see InvocationDefinition#confirmMessage */
    String getConfirmMessage() { return invocationDefinition.confirmMessage }
    /** Sets the confirmation dialog message and re-registers the click event. */
    void setConfirmMessage(String value) {
        invocationDefinition.confirmMessage = value
        setEvent()
    }
    /** Sets the confirmation message interpolation arguments and re-registers the click event. */
    void setConfirmMessageArgs(List value) {
        invocationDefinition.confirmMessageArgs = value
        setEvent()
    }
    /**
     * Sets the {@link InvocationDefinition} to invoke when the user confirms the confirmation dialog,
     * and re-registers the click event.
     *
     * @param value the invocation to run on confirmation
     */
    void setConfirmMessageOnConfirm(InvocationDefinition value) {
        invocationDefinition.confirmMessageOnConfirm = value
        setEvent()
    }
}
