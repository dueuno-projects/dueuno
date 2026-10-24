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

import dueuno.elements.core.ComponentEvent
import dueuno.elements.core.Hotkey
import dueuno.elements.core.ComponentAction
import groovy.transform.CompileStatic

/**
 * A {@link Label}-based component that renders a navigable link.
 * <p>
 * {@code Link} wraps a {@link ComponentAction} and registers a client-side
 * event (default: {@code "click"}) that triggers the configured server-side action.
 * All navigation properties ({@code controller}, {@code action}, {@code url}, etc.)
 * are delegated to the underlying {@code componentAction}; changing any of them
 * automatically re-registers the click event via {@link #setOnEvent(String)}.
 * A hotkey can be set with {@code hotkey} using a
 * {@link Hotkey} value, for example {@code Hotkey.F1}.
 * </p>
 * <p>
 * Additional render options (modal display, animations, scroll behaviour, etc.) are
 * stored in {@link dueuno.elements.core.PageRenderProperties} accessible through
 * {@code componentAction}.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class Link extends Label {

    /** The underlying action definition holding all navigation and render properties. */
    ComponentAction componentAction

    /** The DOM event name that triggers the navigation action. Defaults to {@code "click"}. */
    String eventName

    /** Hotkey that triggers this link. */
    Hotkey hotkey

    /**
     * Creates a {@code Link} component from the given argument map.
     * All {@link Label} arguments are supported, plus all {@link ComponentAction}
     * properties and an optional {@code hotkey} from {@link Hotkey}.
     * If no {@code action} is provided it defaults to {@code "index"}.
     * An optional {@code onClick} key may specify an action name override for the click event.
     *
     * @param args map of link and label properties
     */
    Link(Map args = [:]) {
        super(args)

        componentAction = new ComponentAction(args)

        tag = args.tag == null ? false : args.tag

        html = args.html
        containerSpecs.label = ''
        containerSpecs.help = ''

        eventName = 'click'

        setHotkey(args.hotkey as Hotkey)
        setOnEvent(args.onClick as String)
    }

    /**
     * Registers (or re-registers) the client-side event that triggers this link's action.
     * Uses the current {@code componentAction} properties, resolved i18n messages for
     * {@code infoMessage} and {@code confirmMessage}, and the optional {@code onEvent}
     * action override.
     *
     * @param onEvent optional action name to use instead of {@link ComponentAction#action};
     *                when {@code null} the action definition's own action is used
     */
    void setOnEvent(String onEvent = null) {
        Map event = componentAction.properties
        Map eventOverride = [
                event: eventName,
                action: onEvent ?: componentAction.action,
                infoMessage: message(componentAction.infoMessage, componentAction.infoMessageArgs),
                confirmMessage: message(componentAction.confirmMessage, componentAction.confirmMessageArgs),
        ] as Map
        on(event + eventOverride)
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
        String ns = componentAction.namespace ?: ''
        String path = "${ns}/${componentAction.controller}/${componentAction.action}"
        String queryParams = componentAction.params.collect { "${it.key}=${it.value}" }.join('&')
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

    /** @see ComponentAction#target */
    String getTarget() { return componentAction.target }
    /** @see ComponentAction#target */
    void setTarget(String value) { componentAction.target = value }

    /** @see ComponentAction#namespace */
    String getNamespace() { return componentAction.namespace }
    /** Sets the controller namespace and re-registers the click event. */
    void setNamespace(String value) {
        componentAction.namespace = value
        setOnEvent()
    }

    /** @see ComponentAction#controller */
    String getController() { return componentAction.controller }
    /** Sets the controller name and re-registers the click event. */
    void setController(String value) {
        componentAction.controller = value
        setOnEvent()
    }

    /** @see ComponentAction#action */
    String getAction() { return componentAction.action }
    /** Sets the action name and re-registers the click event. */
    void setAction(String value) {
        componentAction.action = value
        setOnEvent()
    }

    /** @see ComponentAction#call */
    String getCall() { return componentAction.call }
    /** Sets the client-side function name and re-registers the click event. */
    void setCall(String value) {
        componentAction.call = value
        setOnEvent()
    }

    /** @see ComponentAction#params */
    Map getParams() { return componentAction.params }
    /** Sets the request parameters and re-registers the click event. */
    void setParams(Map value) {
        componentAction.params = value
        setOnEvent()
    }

    /** @see ComponentAction#fragment */
    String getFragment() { return componentAction.fragment }
    /** Sets the URL fragment and re-registers the click event. */
    void setFragment(String value) {
        componentAction.fragment = value
        setOnEvent()
    }

    /** @see ComponentAction#path */
    String getPath() { return componentAction.path }
    /** Sets the path and re-registers the click event. */
    void setPath(String value) {
        componentAction.path = value
        setOnEvent()
    }

    /** @see ComponentAction#url */
    String getUrl() { return componentAction.url }
    /** Sets the explicit URL and re-registers the click event. */
    void setUrl(String value) {
        componentAction.url = value
        setOnEvent()
    }

    /** @see ComponentAction#submit */
    List<String> getSubmit() { return componentAction.submit }

    /**
     * Convenience overload that wraps a single component name in a list before delegating
     * to {@link #setSubmit(List)}.
     *
     * @param value the name of the component to submit
     */
    void setSubmit(String value) { setSubmit([value]) }

    /** Sets the submit component list and re-registers the click event. */
    void setSubmit(List<String> value) {
        componentAction.submit = value
        setOnEvent()
    }

    /** @see ComponentAction#direct */
    Boolean getDirect() { return componentAction.direct }
    /** @see ComponentAction#direct */
    void setDirect(Boolean value) { componentAction.direct = value }

    /** Whether this link opens its target in a modal dialog. */
    Boolean getModal() { return componentAction.renderProperties.modal }
    /** Sets whether this link opens its target in a modal dialog and re-registers the click event. */
    void setModal(Boolean value) {
        componentAction.renderProperties.modal = value
        setOnEvent()
    }

    /** Whether this link renders in a small (compact) style. */
    Boolean getSmall() { return componentAction.renderProperties.small }
    /** Sets the small (compact) rendering style and re-registers the click event. */
    void setSmall(Boolean value) {
        componentAction.renderProperties.small = value
        setOnEvent()
    }

    /** Whether this link renders in a large style. */
    Boolean getLarge() { return componentAction.renderProperties.large }
    /** Sets the large rendering style and re-registers the click event. */
    void setLarge(Boolean value) {
        componentAction.renderProperties.large = value
        setOnEvent()
    }

    /** The CSS animation name applied when navigating via this link. */
    String getAnimate() { return componentAction.renderProperties.animate }
    /** Sets the CSS animation for navigation and re-registers the click event. */
    void setAnimate(String value) {
        componentAction.renderProperties.animate = value
        setOnEvent()
    }

    /** Whether a close button is shown when the target is displayed in a modal. */
    Boolean getCloseButton() { return componentAction.renderProperties.closeButton }
    /** Sets whether a close button is shown in the modal and re-registers the click event. */
    void setCloseButton(Boolean value) {
        componentAction.renderProperties.closeButton = value
        setOnEvent()
    }

    /** The scroll behaviour applied when navigating via this link (e.g. {@code "reset"}). */
    String getScroll() { return componentAction.renderProperties.scroll }
    /** Sets the scroll behaviour and re-registers the click event. */
    void setScroll(String value) {
        componentAction.renderProperties.scroll = value
        setOnEvent()
    }

    /** @see ComponentAction#getTargetNew() */
    Boolean getTargetNew() { return componentAction.targetNew }
    /** Sets whether the link opens in a new tab and re-registers the click event. */
    void setTargetNew(Boolean value) {
        componentAction.targetNew = value
        setOnEvent()
    }

    /** @see ComponentAction#loading */
    Boolean getLoading() { return componentAction.loading }
    /** Sets the loading indicator flag and re-registers the click event. */
    void setLoading(Boolean value) {
        componentAction.loading = value
        setOnEvent()
    }

    /** @see ComponentAction#infoMessage */
    String getInfoMessage() { return componentAction.infoMessage }
    /** Sets the info message and re-registers the click event. */
    void setInfoMessage(String value) {
        componentAction.infoMessage = value
        setOnEvent()
    }
    /** Sets the info message interpolation arguments and re-registers the click event. */
    void setInfoMessageArgs(List value) {
        componentAction.infoMessageArgs = value
        setOnEvent()
    }

    /** @see ComponentAction#confirmMessage */
    String getConfirmMessage() { return componentAction.confirmMessage }
    /** Sets the confirmation dialog message and re-registers the click event. */
    void setConfirmMessage(String value) {
        componentAction.confirmMessage = value
        setOnEvent()
    }
    /** Sets the confirmation message interpolation arguments and re-registers the click event. */
    void setConfirmMessageArgs(List value) {
        componentAction.confirmMessageArgs = value
        setOnEvent()
    }
    /**
     * Sets the {@link ComponentEvent} to invoke when the user confirms the confirmation dialog,
     * and re-registers the click event.
     *
     * @param value the event to fire on confirmation
     */
    void setConfirmMessageOnConfirm(ComponentEvent value) {
        componentAction.confirmMessageOnConfirm = value.asMap()
        setOnEvent()
    }
}
