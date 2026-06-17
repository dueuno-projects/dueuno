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
import dueuno.elements.ComponentEvent
import dueuno.elements.Menu
import dueuno.elements.style.TextWrap
import groovy.transform.CompileStatic
import groovy.transform.Synchronized

/**
 * A clickable button component that can carry one or more navigable actions.
 * <p>
 * A {@code Button} is composed of up to three visual slots:
 * </p>
 * <ul>
 *   <li>{@link #defaultAction} — the main clickable area of the button.</li>
 *   <li>{@link #tailAction} — an optional secondary action rendered at the trailing
 *       edge of the button (e.g. a split-button arrow).</li>
 *   <li>{@link #actionMenu} — a drop-down {@link Menu} listing additional actions
 *       beyond the default and tail slots.</li>
 * </ul>
 * <p>
 * All link-navigation properties ({@code controller}, {@code action}, {@code params},
 * {@code modal}, {@code submit}, etc.) are delegated to the underlying {@link Menu}
 * items and can be set via the corresponding setters on this class.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class Button extends Component {

    /** The primary {@link Menu} item rendered as the main clickable face of the button. */
    Menu defaultAction

    /** An optional secondary {@link Menu} item rendered at the trailing edge of the button. */
    Menu tailAction

    /** The drop-down {@link Menu} that holds all actions registered on this button. */
    Menu actionMenu

    /** Whether this button is rendered as a primary (highlighted) button. */
    Boolean primary

    /** Whether the button stretches to fill its container's width. */
    Boolean stretch

    /** Whether this button is part of a button group. */
    Boolean group

    /** Maximum width of the button in pixels; {@code 0} means no constraint. */
    Integer maxWidth

    /**
     * Creates a {@code Button} instance configured from the supplied argument map.
     * Initialises the action menu, applies optional {@code primary}, {@code stretch},
     * {@code group}, and {@code maxWidth} arguments, and registers a default action
     * unless {@code dontCreateDefaultAction} is set.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code primary} ({@link Boolean}),
     *             {@code stretch} ({@link Boolean}),
     *             {@code group} ({@link Boolean}),
     *             {@code maxWidth} ({@link Integer}),
     *             {@code text} ({@link String}) — label for the default action,
     *             {@code dontCreateDefaultAction} ({@link Boolean}) — skip auto-creation of the default action,
     *             plus all keys accepted by {@link Component#Component(Map)} and {@link Menu}
     */
    Button(Map args) {
        super(args)

        defaultAction = null
        tailAction = null
        actionMenu = createComponent(Menu, id + 'Actions')

        if (args.primary != null) setPrimary(args.primary as Boolean)
//        if (args.params != null) setParams(args.params as Map)

        stretch = (args.stretch == null) ? true : args.stretch
        group = (args.group == null) ? false : args.group
        maxWidth = (args.maxWidth == null) ? 0 : args.maxWidth as Integer

        String buttonText = buildLabel(id)
        containerSpecs.label = (args.label == buttonText) ? '' : args.label
        containerSpecs.help = ''

        Map defaultActionArgs = [:]
        defaultActionArgs.text = args.text ?: buttonText
        for (arg in args) {
            defaultActionArgs[arg.key] = arg.value
        }

        if (!args.dontCreateDefaultAction) {
            defaultActionArgs.remove('id')
            addDefaultAction(defaultActionArgs)
        }
    }

    /**
     * Sets whether this button is rendered as a primary (highlighted) button.
     * When {@code true} and no background colour has been explicitly set, the
     * primary text and background colours are applied automatically.
     *
     * @param value {@code true} to mark this button as primary
     */
    void setPrimary(Boolean value) {
        primary = value

        if (primary && !backgroundColor) {
            textColor = primaryTextColor
            backgroundColor = primaryBackgroundColor
        }
    }

    /**
     * Sets the controller of the default action.
     *
     * @param value the controller name
     */
    void setController(String value) {
        defaultAction.controller = value
    }

    /**
     * Sets the action of the default action.
     *
     * @param value the action name
     */
    void setAction(String value) {
        defaultAction.action = value
    }

    /**
     * Replaces the {@code params} map on every action registered in this button's action menu.
     *
     * @param params the new parameters map to set on all actions
     */
    void setParams(Map params) {
        for (item in actionMenu.items) {
            item.params = params
        }
    }

    /**
     * Merges the given parameters into the existing {@code params} map of every action
     * registered in this button's action menu.
     *
     * @param params the parameters to add to all actions
     * @see Menu
     */
    void addParams(Map params) {
        for (action in actionMenu.items) {
            action.params += params
        }
    }

    /**
     * Sets the submit component ID for all actions, delegating to {@link #setSubmit(List)}.
     *
     * @param value the ID of the form or component to submit
     */
    void setSubmit(String value) {
        setSubmit([value])
    }

    /**
     * Sets the list of component IDs to submit when any action in this button is triggered.
     *
     * @param value list of component IDs to submit
     */
    void setSubmit(List<String> value) {
        for (action in actionMenu.items) {
            action.submit = value
        }
    }

    /** @return whether the default action opens in a modal window */
    Boolean getModal() {
        return defaultAction.modal
    }

    /**
     * Sets whether the default action opens in a modal window.
     *
     * @param value {@code true} to open in a modal
     */
    void setModal(Boolean value) {
        defaultAction.modal = value
    }

    /** @return whether the default action uses a small button style */
    Boolean getSmall() {
        return defaultAction.small
    }

    /**
     * Sets whether the default action uses a small button style.
     *
     * @param value {@code true} for small style
     */
    void setSmall(Boolean value) {
        defaultAction.small = value
    }

    /** @return whether the default action uses a large button style */
    Boolean getLarge() {
        return defaultAction.large
    }

    /**
     * Sets whether the default action uses a large button style.
     *
     * @param value {@code true} for large style
     */
    void setLarge(Boolean value) {
        defaultAction.large = value
    }

    /** @return the animation name applied to the default action transition */
    String getAnimate() {
        return defaultAction.animate
    }

    /**
     * Sets the animation name applied to the default action transition.
     *
     * @param value the animation name
     */
    void setAnimate(String value) {
        defaultAction.animate = value
    }

    /** @return whether the default action performs a direct (non-AJAX) navigation */
    Boolean getDirect() {
        return defaultAction.direct
    }

    /**
     * Sets whether the default action performs a direct (non-AJAX) navigation.
     *
     * @param value {@code true} for direct navigation
     */
    void setDirect(Boolean value) {
        defaultAction.direct = value
    }

    /** @return whether the default action renders as a modal close button */
    Boolean getCloseButton() {
        return defaultAction.closeButton
    }

    /**
     * Sets whether the default action renders as a modal close button.
     *
     * @param value {@code true} to render as a close button
     */
    void setCloseButton(Boolean value) {
        defaultAction.closeButton = value
    }

    /** @return the scroll target of the default action */
    String getScroll() {
        return defaultAction.scroll
    }

    /**
     * Sets the scroll target of the default action.
     *
     * @param value the scroll target identifier
     */
    void setScroll(String value) {
        defaultAction.scroll = value
    }

    /**
     * Sets the label text of the default action.
     *
     * @param value the button label
     */
    void setText(String value) {
        defaultAction.text = value
    }

    /**
     * Sets the icon of the default action.
     *
     * @param value the icon class (e.g. {@code "fa-plus"})
     */
    void setIcon(String value) {
        defaultAction.icon = value
    }

    /**
     * Sets the tooltip text of the default action.
     *
     * @param value the tooltip string
     */
    void setTooltip(String value) {
        defaultAction.tooltip = value
    }

    /**
     * Sets the informational message shown before the default action is executed.
     *
     * @param value the i18n message key or literal message
     */
    void setInfoMessage(String value) {
        defaultAction.infoMessage = value
    }

    /**
     * Sets the interpolation arguments for the info message of the default action.
     *
     * @param value the list of message arguments
     */
    void setInfoMessageArgs(List value) {
        defaultAction.infoMessageArgs = value
    }

    /**
     * Sets the confirmation message shown before the default action is executed.
     * Also wires the "Confirm" button to re-trigger the default action.
     *
     * @param value the i18n message key or literal confirmation message
     */
    void setConfirmMessage(String value) {
        defaultAction.confirmMessage = value
        defaultAction.confirmMessageOnConfirm = new ComponentEvent(defaultAction.link.linkDefinition.asMap())
    }

    /**
     * Sets the interpolation arguments for the confirmation message of the default action.
     *
     * @param value the list of message arguments
     */
    void setConfirmMessageArgs(List value) {
        defaultAction.confirmMessageArgs = value
    }

    /**
     * Sets the {@link ComponentEvent} fired when the user confirms the confirmation dialog
     * of the default action.
     *
     * @param value the event to fire on confirmation
     */
    void confirmMessageOnConfirm(ComponentEvent value) {
        defaultAction.confirmMessageOnConfirm = value
    }

    /**
     * Sets the link target of the default action (e.g. an iframe name or {@code "_blank"}).
     *
     * @param value the target name
     */
    void setTarget(String value) {
        defaultAction.target = value
    }

    /**
     * Sets whether the default action opens in a new browser tab or window.
     *
     * @param value {@code true} to open in a new tab/window
     */
    void setTargetNew(Boolean value) {
        defaultAction.targetNew = value
    }

    /**
     * Sets whether the default action shows a loading indicator while executing.
     *
     * @param value {@code true} to show the loading indicator
     */
    void setLoading(Boolean value) {
        defaultAction.loading = value
    }

    /**
     * Adds a new {@link Menu} item to the button's action menu with the specified parameters.
     * Defaults {@code controller} to the current controller and {@code action} to {@code "index"}
     * when not supplied.
     *
     * @param args action configuration; recognised keys: {@code action}, {@code controller},
     *             {@code id}, {@code loading}, and all keys accepted by {@link Menu}
     * @return the newly created {@link Menu} item
     * @see Menu
     */
    private Menu addMenu(Map args) {
        String controller = args.controller ?: controllerName
        String action = args.action ?: 'index'

        args['class'] = Menu
        args.id = args.id ?: (controller == controllerName ? action : controller + action?.capitalize())
        args.controller = controller
        args.action = action

        if (defaultAction) {
            args.loading = args.loading == null ? defaultAction.loading : args.loading
        }

        return actionMenu.addItem(args)
    }

    /**
     * Adds a new action to the button's action menu.
     * If no default action has been set yet, the new action also becomes the default action.
     *
     * @param args action configuration forwarded to {@link #addMenu(Map)}
     * @return this {@code Button} instance for chaining
     */
    Button addAction(Map args) {
        Menu menu = addMenu(args)
        if (!defaultAction) {
            defaultAction = menu
        }
        return this
    }

    /**
     * Adds a visual separator to the button's drop-down action menu.
     *
     * @param text optional label displayed next to the separator; {@code null} for a plain divider
     * @return this {@code Button} instance for chaining
     */
    Button addSeparator(String text = null) {
        addMenu(
                separator: true,
                text: text,
        )
        return this
    }

    /**
     * Adds a new action and immediately promotes it to the {@link #defaultAction} slot.
     *
     * @param args action configuration forwarded to {@link #addMenu(Map)}
     * @return this {@code Button} instance for chaining
     */
    Button addDefaultAction(Map args) {
        addMenu(args)
        setDefaultAction(args)
        return this
    }

    /**
     * Adds a new action and immediately promotes it to the {@link #tailAction} slot.
     *
     * @param args action configuration forwarded to {@link #addMenu(Map)}
     * @return this {@code Button} instance for chaining
     */
    Button addTailAction(Map args) {
        addMenu(args)
        setTailAction(args)
        return this
    }

    /**
     * Returns {@code true} if this button has at least one non-separator action registered.
     *
     * @return {@code true} if at least one action is present; {@code false} otherwise
     */
    Boolean hasActions() {
        return actionMenu.items.count { !it.separator } > 0
    }

    /**
     * Removes the action identified by the given {@code controller} and {@code action} keys.
     * If the matching action is the default or tail action, the corresponding slot is also cleared.
     *
     * @param args map with {@code action} (required) and optional {@code controller} (defaults to current controller)
     */
    void removeAction(Map args) {
        Menu menu = getAction(args)

        if (menu == defaultAction) {
            removeDefaultAction()

        } else if (menu == tailAction) {
            removeTailAction()

        } else if (menu) {
            actionMenu.removeItem(menu)
        }
    }

    /**
     * Remove the button relative to default action
     */
    void removeDefaultAction() {
        actionMenu.removeItem(defaultAction)
        defaultAction = null
    }

    /**
     * Remove the button relative to tail action
     */

    void removeTailAction() {
        actionMenu.removeItem(tailAction)
        tailAction = null
    }

    /**
     * Removes all actions from this button, including the default action, tail action,
     * and every item in the action menu.
     */
    void removeAllActions() {
        removeDefaultAction()
        removeTailAction()
        actionMenu.clear()
    }

    /**
     * Returns the {@link Menu} item that matches the given {@code controller} and {@code action}.
     *
     * @param args map with {@code action} (required) and optional {@code controller}
     *             (defaults to current controller)
     * @return the matching {@link Menu} item, or {@code null} if not found
     */
    Menu getAction(Map args) {
        String controller = args.controller ?: controllerName
        String action = args.action ?: actionName
        return actionMenu.items.find { it.controller == controller && it.action == action }
    }

    /**
     * Promotes an existing action to the {@link #defaultAction} slot.
     * The action's text-wrap style is reset to {@link TextWrap#DEFAULT}.
     *
     * @param args map with {@code action} and optional {@code controller} identifying the action to promote
     */
    void setDefaultAction(Map args) {
        Menu action = getAction(args)
        action.link.textWrap = TextWrap.DEFAULT
        defaultAction = action
    }

    /**
     * Clears the {@link #defaultAction} slot without removing the underlying action from the menu.
     */
    void unsetDefaultAction() {
        defaultAction = null
    }

    /**
     * Promotes an existing action to the {@link #tailAction} slot.
     * The action's text-wrap style is reset to {@link TextWrap#DEFAULT}.
     *
     * @param args map with {@code action} and optional {@code controller} identifying the action to promote
     */
    void setTailAction(Map args) {
        Menu action = getAction(args)
        action.link.textWrap = TextWrap.DEFAULT
        tailAction = action
    }

    /**
     * Clears the {@link #tailAction} slot without removing the underlying action from the menu.
     */
    void unsetTailAction() {
        tailAction = null
    }

    /**
     * Returns all actions registered in the action menu that are neither the
     * {@link #defaultAction} nor the {@link #tailAction}, sorted by their {@code order} property.
     *
     * @return an ordered list of {@link Menu} items for the drop-down portion of the button
     * @see Menu
     */
    @Synchronized
    List<Menu> getMenuActions() {
        List<Menu> result = []

        for (action in actionMenu.items.sort {it.order }) {
            if (action != defaultAction && action != tailAction) {
                result.add(action)
            }
        }

        return result
    }

    /**
     * Returns {@code true} if there is at least one drop-down menu action (i.e. an action
     * that is neither the default nor the tail action).
     *
     * @return {@code true} if drop-down menu actions exist
     */
    Boolean hasMenuActions() {
        Integer count = 0
        for (action in actionMenu.items) {
            if (action != defaultAction && action != tailAction) {
                count++
            }
        }
        return count > 0
    }

    /**
     * Copies all actions from {@code fromButton} into this button's action menu,
     * preserving the default-action and tail-action slot assignments.
     *
     * @param fromButton the source button whose actions are copied
     */
    void copyActionsFrom(Button fromButton) {
        for (action in fromButton.actionMenu.items) {
            Menu clonedAction = actionMenu.addItem(action.copy())

            if (action == fromButton.defaultAction) {
                defaultAction = clonedAction

            } else if (action == fromButton.tailAction) {
                tailAction = clonedAction
            }
        }
    }

    /**
     * Attaches a DOM event listener to the default action's link.
     * Delegates to {@link dueuno.elements.components.Link#on(Map)} on the underlying link.
     *
     * @param args event configuration map accepted by {@link dueuno.elements.components.Link#on(Map)}
     * @return this component for chaining
     */
    @Override
    Component on(Map args) {
        defaultAction?.link?.on(args)
        return this
    }
}
