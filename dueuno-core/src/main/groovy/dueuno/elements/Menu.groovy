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

import dueuno.core.Feature
import dueuno.elements.components.Link
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 */

@CompileStatic
class Menu extends Component {

    private Menu parent
    // SEE: https://stackoverflow.com/questions/13755592/concurrent-modification-exception-while-iterating-over-arraylist
    private List<Menu> items = [].asSynchronized() as List<Menu>

    private static Integer menuIdCounter = 0
    private Integer menuId

    private Integer generateMenuId() {
        return menuIdCounter++
    }

    /** The menu item link */
    Link link

    /** The menu item is a separator */
    Boolean separator

    /** Prefix to use for text i18n */
    String textPrefix

    /** Display order */
    Integer order

    /**
     * Display the menu item only to the users granted with the specified authorities (eg: <code>['ROLE_CAN_EDIT_ORDERS', 'ROLE_CAN_EDIT_PRODUCTS']</code>).
     * A menu is always visible for <code>ROLE_ADMIN</code>
     */
    List<String> authorities = []

    /**
     * INTERNAL USE ONLY. Use {@link dueuno.elements.core.ApplicationService#registerFeature(java.util.Map)} instead.
     *
     * @param args
     */
    Menu(Map args = [:]) {
        super(args)

        this.menuId = generateMenuId()
        separator = args.separator == null ? false : args.separator
        textPrefix = args.textPrefix

        order = (Integer) args.order ?: this.menuId * 10

        // Set authorities if specified
        List<String> configuredAuthorities = (List<String>) args.authorities ?: []
        authorities.addAll(configuredAuthorities)

        // Creates the link
        String id = '' + args.id ?: ('link' + (args.order ?: this.menuId))
        link = (Link) addComponent(Link, id, args)
    }

    String toString() {
        return separator ? '---' : link?.toString()
    }

    Menu getParent() {
        return parent
    }

    /**
     * True if the menu item has subitems
     */
    Boolean hasSubitems() {
        return items.size() > 0
    }

    /**
     * Adds a subitem
     * @param args
     * @return the newly added item
     */
    Menu addItem(Map args) {
        args.parent = this
        if (!args.id) args.id = menuId.toString()
        args.order = args.order == null ? this.items.size() + 1 : args.order
        Menu menu = new Menu(args)
        addItem(menu)
        return menu
    }

    Menu addItem(Menu menu) {
        items.add(menu)
        if (menu.id < items.last().id) {
            items.sort { it['order'] }
        }

        return menu
    }

    Menu addSeparator(Feature feature, String text = '') {
        Map args = [:]
        args.text = text
        args.separator = true
        args.order = feature.order
        args.authorities = feature.authorities
        addItem(args)
    }

    /**
     * Returns the list of subitems
     */
    List<Menu> getItems() {
        return items
    }

    /**
     * Returns a flat list of the VISIBLE menu items and subitems
     */
    List<Menu> listItems() {
        List<Menu> results = listItemsRecursive(false)
        return results.tail().sort { it.order }
    }

    /**
     * Returns a flat list of ALL the menu items and subitems
     */
    List<Menu> listAllItems() {
        List<Menu> results = listItemsRecursive(true)
        return results.tail().sort {it.order }
    }

    private List<Menu> listItemsRecursive(Boolean displayHiddenItems = false) {
        List<Menu> results = []
        for (item in items) {
            if (item.display || displayHiddenItems) {
                results.addAll(item.listItemsRecursive())
            }
        }
        return [this] + results
    }

    /**
     * Returns the menu item for the specified controller
     */
    Menu byController(String controllerName) {
        Menu result = (Menu) items.find { it['controller'] == controllerName }

        if (result)
            return result

        items.any { item ->
            result = item.byController(controllerName)
            if (result) return true
        }

        return result
    }

    /**
     * Remove a subitem
     */
    void removeItem(Menu menu) {
        Menu found = items.find { it == menu }
        if (found) {
            items.remove(menu)
            return
        }

        items.any { item ->
            item.removeItem(menu)
        }
    }

    /**
     * Remove a subitem by controller name
     */
    void removeItem(String controllerName) {
        Menu menu = byController(controllerName)
        removeItem(menu)
    }

    /**
     * Deletes all the subitems
     */
    void clear() {
        menuIdCounter = 0
        for (item in items) {
            item.clear()
        }
        items = []
    }

    Menu copy() {
        Map args = this.properties + this.link.properties
        args.id = this.getId()
        args.parent = this.parent

        Menu menu = new Menu(args)
        for (item in this.items) {
            menu.addItem(item.copy())
        }

        return menu
    }

    void createFromFeature(Feature f, Boolean favourites = false) {
        clear()
        items = fromFeature(f, favourites).items
    }

    private Menu fromFeature(Feature f, Boolean favourites = false) {
        Map menuItemArgs = f.properties
        menuItemArgs.id = f.getId()
        menuItemArgs.text = f.text
        menuItemArgs.icon = f.icon
        menuItemArgs.tooltip = f.tooltip
        menuItemArgs.image = f.image
        menuItemArgs.separator = f.controller ? false : true
        menuItemArgs.textPrefix = textPrefix
        menuItemArgs.order = f.order
        menuItemArgs.renderProperties['scroll'] = 'reset'
        menuItemArgs.renderProperties['animate'] = 'fade'

        // Feature has a "parent" property that overlaps with parent component
        // we need to programmatically remove it
        menuItemArgs.parent = null

        Menu menu = new Menu(menuItemArgs)
        List<Feature> features

        if (favourites) {
            features = f.favouriteFeatures
        } else {
            features = f.features
        }

        for (feature in features) {
            if (feature.controller) { // It's not a separator
                Menu featureMenu = fromFeature(feature, favourites)
                menu.addItem(featureMenu)
            } else {
                menu.addSeparator(feature)
            }
        }

        return menu
    }


    //
    // LINK SHORTCUTS
    //

    String getTarget() {
        return link.target
    }

    void setTarget(String value) {
        link.target = value
    }

    Boolean getTargetNew() {
        return link.targetNew
    }

    void setTargetNew(Boolean value) {
        link.targetNew = value
    }

    String getNamespace() {
        return link.namespace
    }

    void setNamespace(String value) {
        link.namespace = value
    }

    String getController() {
        return link.controller
    }

    void setController(String value) {
        link.controller = value
    }

    String getAction() {
        return link.action
    }

    void setAction(String value) {
        link.action = value
    }

    Map getParams() {
        return link.params
    }

    void setParams(Map value) {
        link.params = value
    }

    String getFragment() {
        return link.fragment
    }

    void setFragment(String value) {
        link.fragment = value
    }

    String getPath() {
        return link.path
    }

    void setPath(String value) {
        link.path = value
    }

    String getUrl() {
        return link.url
    }

    void setUrl(String value) {
        link.url = value
    }

    String getIcon() {
        return link.icon
    }

    void setIcon(String value) {
        link.icon = value
    }

    String getTooltip() {
        return link.tooltip
    }

    void setTooltip(String value) {
        link.tooltip = value
    }

    String getImage() {
        return link.image
    }

    void setImage(String value) {
        link.image = value
    }

    List<String> getSubmit() {
        return link.submit
    }

    void setSubmit(List<String> value) {
        link.submit = value
    }

    Boolean getModal() {
        return link.modal
    }

    void setModal(Boolean value) {
        link.modal = value
    }

    Boolean getSmall() {
        return link.small
    }

    void setSmall(Boolean value) {
        link.small = value
    }

    Boolean getLarge() {
        return link.large
    }

    void setLarge(Boolean value) {
        link.large = value
    }

    String getAnimate() {
        return link.animate
    }

    void setAnimate(String value) {
        link.animate = value
    }

    Boolean getDirect() {
        return link.direct
    }

    void setDirect(Boolean value) {
        link.direct = value
    }

    Boolean getCloseButton() {
        return link.closeButton
    }

    void setCloseButton(Boolean value) {
        link.closeButton = value
    }

    String getScroll() {
        return link.scroll
    }

    void setScroll(String value) {
        link.scroll = value
    }

    String getText() {
        return link.text
    }

    void setText(String value) {
        link.text = value
        link.tooltip = null
    }

    String getTextArgs() {
        return link.textArgs
    }

    void setTextArgs(List values) {
        link.textArgs = values
    }

    Boolean getLoading() {
        return link.loading
    }

    void setLoading(Boolean value) {
        link.loading = value
    }

    String getInfoMessage() {
        return link.infoMessage
    }

    void setInfoMessage(String value) {
        link.infoMessage = value
    }

    void setInfoMessageArgs(List value) {
        link.infoMessageArgs = value
    }

    String getConfirmMessage() {
        return link.confirmMessage
    }

    void setConfirmMessage(String value) {
        link.confirmMessage = value
    }

    void setConfirmMessageArgs(List value) {
        link.confirmMessageArgs = value
    }

    void setConfirmMessageOnConfirm(ComponentEvent value) {
        link.confirmMessageOnConfirm = value
    }
}
