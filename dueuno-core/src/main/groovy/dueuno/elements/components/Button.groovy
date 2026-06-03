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
 * @author Gianluca Sartori
 */

@CompileStatic
class Button extends Component {

    Menu defaultAction
    Menu tailAction
    Menu actionMenu

    Boolean primary

    Boolean stretch
    Boolean group
    Integer maxWidth

    /**
     * Button
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

    void setPrimary(Boolean value) {
        primary = value

        if (primary && !backgroundColor) {
            textColor = primaryTextColor
            backgroundColor = primaryBackgroundColor
        }
    }

    void setController(String value) {
        defaultAction.controller = value
    }

    void setAction(String value) {
        defaultAction.action = value
    }

    /**
     * Overwrite provided params in all actions Button.params.
     *
     * @param Map params
     */
    void setParams(Map params) {
        for (item in actionMenu.items) {
            item.params = params
        }
    }

    /**
     * Add params to all action Button.params
     *
     * @param Map params
     * @see Menu
     */
    void addParams(Map params) {
        for (action in actionMenu.items) {
            action.params += params
        }
    }

    void setSubmit(String value) {
        setSubmit([value])
    }

    void setSubmit(List<String> value) {
        for (action in actionMenu.items) {
            action.submit = value
        }
    }

    Boolean getModal() {
        return defaultAction.modal
    }

    void setModal(Boolean value) {
        defaultAction.modal = value
    }

    Boolean getSmall() {
        return defaultAction.small
    }

    void setSmall(Boolean value) {
        defaultAction.small = value
    }

    Boolean getLarge() {
        return defaultAction.large
    }

    void setLarge(Boolean value) {
        defaultAction.large = value
    }

    String getAnimate() {
        return defaultAction.animate
    }

    void setAnimate(String value) {
        defaultAction.animate = value
    }

    Boolean getDirect() {
        return defaultAction.direct
    }

    void setDirect(Boolean value) {
        defaultAction.direct = value
    }

    Boolean getCloseButton() {
        return defaultAction.closeButton
    }

    void setCloseButton(Boolean value) {
        defaultAction.closeButton = value
    }

    String getScroll() {
        return defaultAction.scroll
    }

    void setScroll(String value) {
        defaultAction.scroll = value
    }

    void setText(String value) {
        defaultAction.text = value
    }

    void setIcon(String value) {
        defaultAction.icon = value
    }

    void setTooltip(String value) {
        defaultAction.tooltip = value
    }

    void setInfoMessage(String value) {
        defaultAction.infoMessage = value
    }

    void setInfoMessageArgs(List value) {
        defaultAction.infoMessageArgs = value
    }

    void setConfirmMessage(String value) {
        defaultAction.confirmMessage = value
        defaultAction.confirmMessageOnConfirm = new ComponentEvent(defaultAction.link.linkDefinition.asMap())
    }

    void setConfirmMessageArgs(List value) {
        defaultAction.confirmMessageArgs = value
    }

    void confirmMessageOnConfirm(ComponentEvent value) {
        defaultAction.confirmMessageOnConfirm = value
    }

    void setTarget(String value) {
        defaultAction.target = value
    }

    void setTargetNew(Boolean value) {
        defaultAction.targetNew = value
    }

    void setLoading(Boolean value) {
        defaultAction.loading = value
    }

    /**
     *
     * Add a new menu action with specified parameters
     *
     * @param args.action Action to access
     * @param args.controller [controllerName] - Controller relative to action
     * @see Menu*
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

    Button addAction(Map args) {
        Menu menu = addMenu(args)
        if (!defaultAction) {
            defaultAction = menu
        }
        return this
    }

    Button addSeparator(String text = null) {
        addMenu(
                separator: true,
                text: text,
        )
        return this
    }

    Button addDefaultAction(Map args) {
        addMenu(args)
        setDefaultAction(args)
        return this
    }

    Button addTailAction(Map args) {
        addMenu(args)
        setTailAction(args)
        return this
    }

    /**
     *
     * @return Boolean - <code>true</code> if no actions are present <code>false</code> otherwise
     */
    Boolean hasActions() {
        return actionMenu.items.count { !it.separator } > 0
    }

    /**
     *
     * Remove the button/link relative to provided action and controller
     *
     * @param Map args.action - Action to remove
     * @param Map args.controller [controllerName] - Controller relative to action
     *
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

    void removeAllActions() {
        removeDefaultAction()
        removeTailAction()
        actionMenu.clear()
    }

    Menu getAction(Map args) {
        String controller = args.controller ?: controllerName
        String action = args.action ?: actionName
        return actionMenu.items.find { it.controller == controller && it.action == action }
    }

    /**
     * Promote a link action to default action button
     *
     * @param String args.action - Action to promote
     * @param String args.controller [request.controllerName] - Controller relative to action
     */
    void setDefaultAction(Map args) {
        Menu action = getAction(args)
        action.link.textWrap = TextWrap.DEFAULT
        defaultAction = action
    }

    void unsetDefaultAction() {
        defaultAction = null
    }

    /**
     * Promote a link action to tail action button
     *
     * @param Map args.action - Action to promote
     * @param Map args.controller [controllerName] - Controller relative to action
     */
    void setTailAction(Map args) {
        Menu action = getAction(args)
        action.link.textWrap = TextWrap.DEFAULT
        tailAction = action
    }

    void unsetTailAction() {
        tailAction = null
    }


    /**
     *
     * Get all link/action except tail and default action
     *
     * @return List of map where every map entry is a <code>Menu</code>
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
     * Add all action present on actionsButton to this actionsButton
     *
     * @param fromButton
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

    @Override
    Component on(Map args) {
        defaultAction?.link?.on(args)
        return this
    }
}
