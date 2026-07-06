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

import dueuno.elements.Component
import dueuno.elements.style.TextDefault
import groovy.transform.CompileStatic

/**
 * A page-header component that displays a title and optionally a "next" (primary action)
 * button and a "back" (secondary navigation) button.
 * <p>
 * The header becomes sticky automatically when either the next or back button is present,
 * unless the {@code sticky} property has been set explicitly. The sticky state is serialised
 * to JSON and consumed by the client-side renderer.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class Header extends Component {

    /** Internal sticky flag set automatically when buttons are added/removed; {@code null} means "not yet decided". */
    private Boolean isSticky

    /** The i18n message key or literal title text displayed in the header. */
    String text

    /** Interpolation arguments for the {@link #text} message key. */
    List textArgs

    /** Optional icon class (e.g. {@code "fa-home"}) displayed alongside the title. */
    String icon

    /** Whether a next/confirm button is currently registered in this header. */
    Boolean hasNextButton

    /** Whether a back/cancel button is currently registered in this header. */
    Boolean hasBackButton

    /** The primary action {@link Button} (right-aligned, rendered as primary style). */
    Button nextButton

    /** The secondary navigation {@link Button} (left-aligned, e.g. "Back" or "Cancel"). */
    Button backButton

    /**
     * Creates a {@code Header} instance configured from the supplied argument map.
     * Initialises the {@link #nextButton} (primary) and {@link #backButton} controls
     * without registering default actions.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code text} ({@link String}) — header title (defaults to {@code controller.action.id}),
     *             {@code textArgs} ({@link List}),
     *             {@code icon} ({@link String}),
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    Header(Map args) {
        super(args)

        icon = args.icon
        String defaultText = controllerName + '.' + actionName + '.' + getId()
        text = (args.text == null) ? defaultText : args.text
        textArgs = (args.textArgs == null) ? [] : args.textArgs as List

        hasBackButton = false
        hasNextButton = false

        backButton = createControl(
                class: Button,
                id: 'backButton',
        )
        nextButton = createControl(
                class: Button,
                id: 'nextButton',
                primary: true,
        )
    }

    /**
     * Adds or replaces the primary action button in this header.
     * Defaults to {@code action: "onConfirm"}, {@code submit: "form"},
     * label {@link TextDefault#CONFIRM}, and icon {@code "fa-solid fa-check"}.
     * Also makes the header sticky unless {@code sticky} was set explicitly.
     *
     * @param args button configuration; all keys are optional and override the defaults above
     * @return the {@link #nextButton} instance
     */
    Button addNextButton(Map args = [:]) {
        args.controller = args.controller ?: controllerName
        args.action = args.action ?: 'onConfirm'
        args.submit = (args.submit == null) ? 'form' : args.submit
        args.text = (args.text == null) ? TextDefault.CONFIRM : args.text
        args.icon = (args.icon == null) ? 'fa-solid fa-check' : args.icon
        if (args.group) nextButton.group = args.group

        nextButton.removeDefaultAction()
        nextButton.addDefaultAction(args)
        hasNextButton = true
        if (sticky == null) isSticky = true

        return nextButton
    }

    /**
     * Removes the primary action (next) button from this header.
     * If no back button is present and {@code sticky} was not set explicitly,
     * the header becomes non-sticky.
     *
     * @return the {@link #nextButton} instance (with its default action removed)
     */
    Button removeNextButton() {
        if (sticky == null && !hasBackButton) isSticky = false
        nextButton.removeDefaultAction()
        hasNextButton = false
        return nextButton
    }

    /**
     * Adds a close button to this header, defaulting to {@code action: "onClose"} and
     * icon {@code "fa-times"} with an empty label. Delegates to {@link #addBackButton(Map)}.
     *
     * @param args optional overrides forwarded to {@link #addBackButton(Map)}
     * @return the {@link #backButton} instance
     */
    Button addCloseButton(Map args = [:]) {
        args.action = args.action ?: 'onClose'
        args.icon = args.icon ?: 'fa-times'
        args.text = args.text ?: ''
        return addBackButton(args)
    }

    /**
     * Adds a cancel button to this header with label {@link TextDefault#CANCEL}.
     * Delegates to {@link #addBackButton(Map)}.
     *
     * @param args optional overrides forwarded to {@link #addBackButton(Map)}
     * @return the {@link #backButton} instance
     */
    Button addCancelButton(Map args = [:]) {
        args.text = args.text ?: TextDefault.CANCEL
        return addBackButton(args)
    }

    /**
     * Adds or replaces the secondary navigation (back) button in this header.
     * If a return-point is available and no explicit {@code controller}/{@code action} is given,
     * the return-point destination is used. Defaults to label {@link TextDefault#BACK} and icon
     * {@code "fa-angle-left"}. Also makes the header sticky unless {@code sticky} was set explicitly.
     *
     * @param args button configuration; all keys are optional and override the defaults above
     * @return the {@link #backButton} instance
     */
    Button addBackButton(Map args = [:]) {
        if (!args.controller && !args.action && hasReturnPoint()) {
            args.controller = args.controller ?: returnPointController ?: controllerName
            args.action = args.action ?: returnPointAction ?: 'index'
            args.params = (args.params in Map ? args.params as Map : [:]) + returnPointParams

        } else {
            args.controller = args.controller ?: controllerName
            args.action = args.action ?: 'index'
            args.params = args.params ?: [:]
        }

        args.text = (args.text == null) ? TextDefault.BACK: args.text
        args.icon = (args.icon == null) ? 'fa-angle-left' : args.icon
        if (args.group) nextButton.group = args.group

        backButton.removeDefaultAction()
        backButton.addDefaultAction(args)
        hasBackButton = true
        if (sticky == null) isSticky = true

        return backButton
    }

    /**
     * Removes the secondary navigation (back) button from this header.
     * If no next button is present and {@code sticky} was not set explicitly,
     * the header becomes non-sticky.
     *
     * @return the {@link #backButton} instance (with its default action removed)
     */
    Button removeBackButton() {
        if (sticky == null && !hasNextButton) isSticky = false
        backButton.removeDefaultAction()
        hasBackButton = false
        return backButton
    }

    /**
     * Sets the header title by pretty-printing the given value through the
     * {@link dueuno.core.PrettyPrinter}.
     *
     * @param value the value to format and use as the header title
     */
    void setText(Object value) {
        text = prettyPrint(value)
    }

    /**
     * Serialises this header's properties to JSON, adding the resolved {@code sticky} state.
     * The sticky value is determined by the explicit {@code sticky} property if set, otherwise
     * by the internal {@link #isSticky} flag.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this header's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                sticky: sticky == null ? isSticky : sticky,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

}
