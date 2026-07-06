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

import dueuno.core.LinkDefinition
import dueuno.elements.ComponentEvent
import groovy.transform.CompileStatic

/**
 * A {@link Label}-based component that renders a navigable link.
 * <p>
 * {@code Link} wraps a {@link dueuno.core.LinkDefinition} and registers a client-side
 * event (default: {@code "click"}) that triggers the configured server-side action.
 * All navigation properties ({@code controller}, {@code action}, {@code url}, etc.)
 * are delegated to the underlying {@link #linkDefinition}; changing any of them
 * automatically re-registers the click event via {@link #setOnEvent(String)}.
 * </p>
 * <p>
 * Additional render options (modal display, animations, scroll behaviour, etc.) are
 * stored in {@link dueuno.elements.PageRenderProperties} accessible through
 * {@link #linkDefinition}.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class Link extends Label {

    /** The underlying link definition holding all navigation and render properties. */
    LinkDefinition linkDefinition

    /** The DOM event name that triggers the navigation action. Defaults to {@code "click"}. */
    String eventName

    /**
     * Creates a {@code Link} component from the given argument map.
     * All {@link Label} arguments are supported, plus all {@link dueuno.core.LinkDefinition}
     * properties. If no {@code action} is provided it defaults to {@code "index"}.
     * An optional {@code onClick} key may specify an action name override for the click event.
     *
     * @param args map of link and label properties
     */
    Link(Map args = [:]) {
        super(args)

        linkDefinition = new LinkDefinition(args)
        linkDefinition.action = args.action ?: 'index'

        tag = args.tag == null ? false : args.tag

        html = args.html
        containerSpecs.label = ''
        containerSpecs.help = ''

        eventName = 'click'
        setOnEvent(args.onClick as String)
    }

    /**
     * Registers (or re-registers) the client-side event that triggers this link's action.
     * Uses the current {@link #linkDefinition} properties, resolved i18n messages for
     * {@code infoMessage} and {@code confirmMessage}, and the optional {@code onEvent}
     * action override.
     *
     * @param onEvent optional action name to use instead of {@link dueuno.core.LinkDefinition#action};
     *                when {@code null} the link definition's own action is used
     */
    void setOnEvent(String onEvent = null) {
        on(linkDefinition.properties + [
                event: eventName,
                action: onEvent ?: linkDefinition.action,
                infoMessage: message(linkDefinition.infoMessage, linkDefinition.infoMessageArgs),
                confirmMessage: message(linkDefinition.confirmMessage, linkDefinition.confirmMessageArgs),
        ])
    }

    /**
     * Returns a JSON string of this link's client-side properties, adding the
     * {@link #loading} flag on top of the inherited {@link Label} properties.
     *
     * @param properties additional properties to merge
     * @return a JSON representation of all component properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                loading: loading,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    /**
     * Returns a debug-friendly string showing the link path and query parameters.
     *
     * @return a string in the form {@code "Link: /namespace/controller/action?key=value&..."}
     */
    String toString() {
        String ns = linkDefinition.namespace ?: ''
        String path = "${ns}/${linkDefinition.controller}/${linkDefinition.action}"
        String queryParams = linkDefinition.params.collect { "${it.key}=${it.value}" }.join('&')
        String queryString = queryParams ? "?${queryParams}" : ''
        return "Link: ${path}${queryString}"
    }

    /**
     * Returns a simplified URL string suitable for development/debugging purposes.
     * Returns the explicit {@link #url} if set, a {@code /controller/action} path if both
     * are present, or {@code null} otherwise.
     *
     * @return a dev-friendly URL string, or {@code null}
     */
    String getDevUrl() {
        if (url) return url
        if (controller && action) return "/${controller}/${action}"
        return null
    }

    /** @see dueuno.core.LinkDefinition#target */
    String getTarget() { return linkDefinition.target }
    /** @see dueuno.core.LinkDefinition#target */
    void setTarget(String value) { linkDefinition.target = value }

    /** @see dueuno.core.LinkDefinition#namespace */
    String getNamespace() { return linkDefinition.namespace }
    /** Sets the controller namespace and re-registers the click event. */
    void setNamespace(String value) {
        linkDefinition.namespace = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#controller */
    String getController() { return linkDefinition.controller }
    /** Sets the controller name and re-registers the click event. */
    void setController(String value) {
        linkDefinition.controller = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#action */
    String getAction() { return linkDefinition.action }
    /** Sets the action name and re-registers the click event. */
    void setAction(String value) {
        linkDefinition.action = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#params */
    Map getParams() { return linkDefinition.params }
    /** Sets the request parameters and re-registers the click event. */
    void setParams(Map value) {
        linkDefinition.params = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#fragment */
    String getFragment() { return linkDefinition.fragment }
    /** Sets the URL fragment and re-registers the click event. */
    void setFragment(String value) {
        linkDefinition.fragment = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#path */
    String getPath() { return linkDefinition.path }
    /** Sets the path and re-registers the click event. */
    void setPath(String value) {
        linkDefinition.path = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#url */
    String getUrl() { return linkDefinition.url }
    /** Sets the explicit URL and re-registers the click event. */
    void setUrl(String value) {
        linkDefinition.url = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#submit */
    List<String> getSubmit() { return linkDefinition.submit }

    /**
     * Convenience overload that wraps a single component name in a list before delegating
     * to {@link #setSubmit(List)}.
     *
     * @param value the name of the component to submit
     */
    void setSubmit(String value) { setSubmit([value]) }

    /** Sets the submit component list and re-registers the click event. */
    void setSubmit(List<String> value) {
        linkDefinition.submit = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#direct */
    Boolean getDirect() { return linkDefinition.direct }
    /** @see dueuno.core.LinkDefinition#direct */
    void setDirect(Boolean value) { linkDefinition.direct = value }

    /** Whether this link opens its target in a modal dialog. */
    Boolean getModal() { return linkDefinition.renderProperties.modal }
    /** Sets whether this link opens its target in a modal dialog and re-registers the click event. */
    void setModal(Boolean value) {
        linkDefinition.renderProperties.modal = value
        setOnEvent()
    }

    /** Whether this link renders in a small (compact) style. */
    Boolean getSmall() { return linkDefinition.renderProperties.small }
    /** Sets the small (compact) rendering style and re-registers the click event. */
    void setSmall(Boolean value) {
        linkDefinition.renderProperties.small = value
        setOnEvent()
    }

    /** Whether this link renders in a large style. */
    Boolean getLarge() { return linkDefinition.renderProperties.large }
    /** Sets the large rendering style and re-registers the click event. */
    void setLarge(Boolean value) {
        linkDefinition.renderProperties.large = value
        setOnEvent()
    }

    /** The CSS animation name applied when navigating via this link. */
    String getAnimate() { return linkDefinition.renderProperties.animate }
    /** Sets the CSS animation for navigation and re-registers the click event. */
    void setAnimate(String value) {
        linkDefinition.renderProperties.animate = value
        setOnEvent()
    }

    /** Whether a close button is shown when the target is displayed in a modal. */
    Boolean getCloseButton() { return linkDefinition.renderProperties.closeButton }
    /** Sets whether a close button is shown in the modal and re-registers the click event. */
    void setCloseButton(Boolean value) {
        linkDefinition.renderProperties.closeButton = value
        setOnEvent()
    }

    /** The scroll behaviour applied when navigating via this link (e.g. {@code "reset"}). */
    String getScroll() { return linkDefinition.renderProperties.scroll }
    /** Sets the scroll behaviour and re-registers the click event. */
    void setScroll(String value) {
        linkDefinition.renderProperties.scroll = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#getTargetNew() */
    Boolean getTargetNew() { return linkDefinition.targetNew }
    /** Sets whether the link opens in a new tab and re-registers the click event. */
    void setTargetNew(Boolean value) {
        linkDefinition.targetNew = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#loading */
    Boolean getLoading() { return linkDefinition.loading }
    /** Sets the loading indicator flag and re-registers the click event. */
    void setLoading(Boolean value) {
        linkDefinition.loading = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#infoMessage */
    String getInfoMessage() { return linkDefinition.infoMessage }
    /** Sets the info message and re-registers the click event. */
    void setInfoMessage(String value) {
        linkDefinition.infoMessage = value
        setOnEvent()
    }
    /** Sets the info message interpolation arguments and re-registers the click event. */
    void setInfoMessageArgs(List value) {
        linkDefinition.infoMessageArgs = value
        setOnEvent()
    }

    /** @see dueuno.core.LinkDefinition#confirmMessage */
    String getConfirmMessage() { return linkDefinition.confirmMessage }
    /** Sets the confirmation dialog message and re-registers the click event. */
    void setConfirmMessage(String value) {
        linkDefinition.confirmMessage = value
        setOnEvent()
    }
    /** Sets the confirmation message interpolation arguments and re-registers the click event. */
    void setConfirmMessageArgs(List value) {
        linkDefinition.confirmMessageArgs = value
        setOnEvent()
    }
    /**
     * Sets the {@link ComponentEvent} to invoke when the user confirms the confirmation dialog,
     * and re-registers the click event.
     *
     * @param value the event to fire on confirmation
     */
    void setConfirmMessageOnConfirm(ComponentEvent value) {
        linkDefinition.confirmMessageOnConfirm = value.asMap()
        setOnEvent()
    }
}
