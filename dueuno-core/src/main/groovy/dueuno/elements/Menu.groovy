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
 * Represents a single node in the application navigation menu tree.
 * <p>
 * A {@code Menu} can act as either a navigable menu item (backed by a {@link Link} component)
 * or a visual separator. Items are arranged in a tree: each {@code Menu} may contain child
 * {@code Menu} instances ({@link #items}), forming an arbitrarily deep hierarchy.
 * </p>
 * <p>
 * The display order of items is controlled by the {@link #order} property; items with the
 * same parent are sorted by this value. Access to individual items can be restricted via
 * {@link #authorities}: only users holding at least one of the listed roles will see the item.
 * {@code ROLE_ADMIN} always has full visibility.
 * </p>
 * <p>
 * In normal usage, menu items are registered through
 * {@code ApplicationService.registerFeature(Map)} rather than being constructed directly.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class Menu extends Component {

    /** The parent menu node, or {@code null} if this is a root node. */
    private Menu parent

    // SEE: https://stackoverflow.com/questions/13755592/concurrent-modification-exception-while-iterating-over-arraylist
    /** Thread-safe list of direct child menu items. */
    private List<Menu> items = [].asSynchronized() as List<Menu>

    /** Global auto-increment counter used to generate unique menu IDs. */
    private static Integer menuIdCounter = 0

    /** This item's auto-generated numeric ID, used as a fallback sort key. */
    private Integer menuId

    /** Increments and returns the next available menu ID. */
    private Integer generateMenuId() {
        return menuIdCounter++
    }

    /** The {@link Link} component that renders this menu item's label and navigation target. */
    Link link

    /** When {@code true}, this item is rendered as a visual separator rather than a navigable link. */
    Boolean separator

    /** i18n message key prefix prepended to the item's text when resolving the display label. */
    String textPrefix

    /** Numeric sort key controlling the display order of this item among its siblings. */
    Integer order

    /**
     * Spring Security authority strings that gate visibility of this menu item.
     * When non-empty, only users holding at least one of the listed roles can see the item
     * (e.g. {@code ['ROLE_CAN_EDIT_ORDERS', 'ROLE_CAN_EDIT_PRODUCTS']}).
     * Items are always visible to users with {@code ROLE_ADMIN}.
     */
    List<String> authorities = []

    /**
     * Creates a {@code Menu} item from the given argument map.
     * <p>
     * <strong>Internal use only.</strong> Prefer
     * {@code ApplicationService.registerFeature(Map)} for registering menu items at
     * application startup.
     * </p>
     *
     * @param args map of menu properties; all {@link Component} args are supported, plus
     *             {@code separator}, {@code textPrefix}, {@code order}, and {@code authorities}
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

    /**
     * Returns {@code "---"} for separator items or the string representation of the
     * underlying {@link Link} for navigable items.
     *
     * @return a debug-friendly string representation of this menu item
     */
    String toString() {
        return separator ? '---' : link?.toString()
    }

    /**
     * Returns the parent {@code Menu} node of this item, or {@code null} if this is a root node.
     *
     * @return the parent menu node
     */
    Menu getParent() {
        return parent
    }

    /**
     * Returns {@code true} if this menu item has at least one direct child item.
     *
     * @return {@code true} if {@link #items} is non-empty
     */
    Boolean hasSubitems() {
        return items.size() > 0
    }

    /**
     * Creates a new child {@code Menu} item from the given argument map and adds it to
     * this node's {@link #items} list. The {@link #order} defaults to the current child
     * count plus one if not explicitly set.
     *
     * @param args the properties of the new child item (same keys accepted by the constructor)
     * @return the newly created child {@code Menu} item
     */
    Menu addItem(Map args) {
        args.parent = this
        if (!args.id) args.id = menuId.toString()
        args.order = args.order == null ? this.items.size() + 1 : args.order
        Menu menu = new Menu(args)
        addItem(menu)
        return menu
    }

    /**
     * Adds an already-constructed {@code Menu} item to this node's {@link #items} list.
     * The list is re-sorted by {@link #order} if the new item would be out of sequence.
     *
     * @param menu the child item to add
     * @return the added {@code Menu} item
     */
    Menu addItem(Menu menu) {
        items.add(menu)
        if (menu.id < items.last().id) {
            items.sort { it['order'] }
        }

        return menu
    }

    /**
     * Adds a separator item derived from a {@link Feature}, using the feature's
     * {@link Feature#order} and {@link Feature#authorities}.
     *
     * @param feature the feature whose metadata is used to configure the separator
     * @param text    optional display text for the separator label; defaults to empty
     * @return the newly added separator {@code Menu} item
     */
    Menu addSeparator(Feature feature, String text = '') {
        Map args = [:]
        args.text = text
        args.separator = true
        args.order = feature.order
        args.authorities = feature.authorities
        addItem(args)
    }

    /**
     * Returns the direct child items of this menu node.
     *
     * @return the thread-safe list of direct child {@code Menu} items
     */
    List<Menu> getItems() {
        return items
    }

    /**
     * Returns a flat, sorted list of all <em>visible</em> descendant items (those whose
     * {@link Component#display} property is {@code true}), excluding the root node itself.
     *
     * @return a list of visible descendant items sorted by {@link #order}
     */
    List<Menu> listItems() {
        List<Menu> results = listItemsRecursive(false)
        return results.tail().sort { it.order }
    }

    /**
     * Returns a flat, sorted list of <em>all</em> descendant items regardless of their
     * {@link Component#display} state, excluding the root node itself.
     *
     * @return a list of all descendant items sorted by {@link #order}
     */
    List<Menu> listAllItems() {
        List<Menu> results = listItemsRecursive(true)
        return results.tail().sort {it.order }
    }

    /**
     * Recursively collects this node and all descendant items into a flat list.
     *
     * @param displayHiddenItems when {@code true}, hidden items are included; otherwise only visible items
     * @return a flat list starting with this node, followed by all qualifying descendants
     */
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
     * Searches the subtree rooted at this node and returns the first {@code Menu} item
     * whose {@link #controller} matches {@code controllerName}, or {@code null} if not found.
     *
     * @param controllerName the Grails controller name to search for
     * @return the matching {@code Menu} item, or {@code null}
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
     * Removes the given {@code Menu} item from this node's subtree. Searches recursively
     * if the item is not a direct child.
     *
     * @param menu the item to remove
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
     * Removes the menu item associated with the given controller name from this node's subtree.
     * Equivalent to {@code removeItem(byController(controllerName))}.
     *
     * @param controllerName the Grails controller name of the item to remove
     */
    void removeItem(String controllerName) {
        Menu menu = byController(controllerName)
        removeItem(menu)
    }

    /**
     * Recursively removes all descendant items and resets the global menu ID counter.
     * After this call the node has no children and {@link #menuIdCounter} is reset to zero.
     */
    void clear() {
        menuIdCounter = 0
        for (item in items) {
            item.clear()
        }
        items = []
    }

    /**
     * Creates a deep copy of this menu node and its entire subtree.
     * All child items are recursively copied and added to the new node.
     *
     * @return a new {@code Menu} instance that is a deep copy of this node
     */
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

    /**
     * Clears this node's subtree and rebuilds it from the given {@link Feature} hierarchy.
     *
     * @param f          the root {@link Feature} to build the menu from
     * @param favourites when {@code true}, only the features marked as favourites are included
     */
    void createFromFeature(Feature f, Boolean favourites = false) {
        clear()
        items = fromFeature(f, favourites).items
    }

    /**
     * Recursively converts a {@link Feature} and its children into a {@code Menu} subtree.
     * Features without a controller are turned into separators; features with a controller
     * are turned into navigable menu items.
     *
     * @param f          the {@link Feature} to convert
     * @param favourites when {@code true}, only favourite sub-features are included
     * @return the root {@code Menu} node for the converted feature
     */
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
    // LINK SHORTCUTS — convenience delegating accessors for the underlying {@link Link} component.
    //

    /** @see dueuno.core.LinkDefinition#target */
    String getTarget() { return link.target }
    /** @see dueuno.core.LinkDefinition#target */
    void setTarget(String value) { link.target = value }

    /** @see dueuno.core.LinkDefinition#getTargetNew() */
    Boolean getTargetNew() { return link.targetNew }
    /** @see dueuno.core.LinkDefinition#setTargetNew(Boolean) */
    void setTargetNew(Boolean value) { link.targetNew = value }

    /** @see dueuno.core.LinkDefinition#namespace */
    String getNamespace() { return link.namespace }
    /** @see dueuno.core.LinkDefinition#namespace */
    void setNamespace(String value) { link.namespace = value }

    /** @see dueuno.core.LinkDefinition#controller */
    String getController() { return link.controller }
    /** @see dueuno.core.LinkDefinition#controller */
    void setController(String value) { link.controller = value }

    /** @see dueuno.core.LinkDefinition#action */
    String getAction() { return link.action }
    /** @see dueuno.core.LinkDefinition#action */
    void setAction(String value) { link.action = value }

    /** @see dueuno.core.LinkDefinition#params */
    Map getParams() { return link.params }
    /** @see dueuno.core.LinkDefinition#params */
    void setParams(Map value) { link.params = value }

    /** @see dueuno.core.LinkDefinition#fragment */
    String getFragment() { return link.fragment }
    /** @see dueuno.core.LinkDefinition#fragment */
    void setFragment(String value) { link.fragment = value }

    /** @see dueuno.core.LinkDefinition#path */
    String getPath() { return link.path }
    /** @see dueuno.core.LinkDefinition#path */
    void setPath(String value) { link.path = value }

    /** @see dueuno.core.LinkDefinition#url */
    String getUrl() { return link.url }
    /** @see dueuno.core.LinkDefinition#url */
    void setUrl(String value) { link.url = value }

    /** The icon identifier displayed on this menu item's link. */
    String getIcon() { return link.icon }
    /** Sets the icon identifier displayed on this menu item's link. */
    void setIcon(String value) { link.icon = value }

    /** The tooltip text shown on hover for this menu item's link. */
    String getTooltip() { return link.tooltip }
    /** Sets the tooltip text for this menu item's link. */
    void setTooltip(String value) { link.tooltip = value }

    /** The image path or identifier displayed on this menu item's link. */
    String getImage() { return link.image }
    /** Sets the image for this menu item's link. */
    void setImage(String value) { link.image = value }

    /** @see dueuno.core.LinkDefinition#submit */
    List<String> getSubmit() { return link.submit }
    /** @see dueuno.core.LinkDefinition#submit */
    void setSubmit(List<String> value) { link.submit = value }

    /** Whether this menu item opens its target in a modal dialog. */
    Boolean getModal() { return link.modal }
    /** Sets whether this menu item opens its target in a modal dialog. */
    void setModal(Boolean value) { link.modal = value }

    /** Whether this menu item renders in a small (compact) style. */
    Boolean getSmall() { return link.small }
    /** Sets the small (compact) rendering style for this menu item. */
    void setSmall(Boolean value) { link.small = value }

    /** Whether this menu item renders in a large style. */
    Boolean getLarge() { return link.large }
    /** Sets the large rendering style for this menu item. */
    void setLarge(Boolean value) { link.large = value }

    /** The CSS animation name applied when navigating to this menu item's target. */
    String getAnimate() { return link.animate }
    /** Sets the CSS animation applied when navigating to this menu item's target. */
    void setAnimate(String value) { link.animate = value }

    /** @see dueuno.core.LinkDefinition#direct */
    Boolean getDirect() { return link.direct }
    /** @see dueuno.core.LinkDefinition#direct */
    void setDirect(Boolean value) { link.direct = value }

    /** Whether a close button is displayed when this menu item's target is shown in a modal. */
    Boolean getCloseButton() { return link.closeButton }
    /** Sets whether a close button is shown when this menu item's target is displayed in a modal. */
    void setCloseButton(Boolean value) { link.closeButton = value }

    /** The scroll behaviour applied when navigating to this menu item's target (e.g. {@code "reset"}). */
    String getScroll() { return link.scroll }
    /** Sets the scroll behaviour for this menu item's navigation target. */
    void setScroll(String value) { link.scroll = value }

    /** The i18n message key (or literal label) displayed as the menu item text. */
    String getText() { return link.text }
    /**
     * Sets the display text for this menu item and clears the tooltip so they don't conflict.
     *
     * @param value the i18n message key or literal label
     */
    void setText(String value) {
        link.text = value
        link.tooltip = null
    }

    /** The interpolation arguments for the menu item's {@link #getText()} i18n message. */
    String getTextArgs() { return link.textArgs }
    /** Sets the interpolation arguments for the menu item's display text. */
    void setTextArgs(List values) { link.textArgs = values }

    /** @see dueuno.core.LinkDefinition#loading */
    Boolean getLoading() { return link.loading }
    /** @see dueuno.core.LinkDefinition#loading */
    void setLoading(Boolean value) { link.loading = value }

    /** @see dueuno.core.LinkDefinition#infoMessage */
    String getInfoMessage() { return link.infoMessage }
    /** @see dueuno.core.LinkDefinition#infoMessage */
    void setInfoMessage(String value) { link.infoMessage = value }
    /** @see dueuno.core.LinkDefinition#infoMessageArgs */
    void setInfoMessageArgs(List value) { link.infoMessageArgs = value }

    /** @see dueuno.core.LinkDefinition#confirmMessage */
    String getConfirmMessage() { return link.confirmMessage }
    /** @see dueuno.core.LinkDefinition#confirmMessage */
    void setConfirmMessage(String value) { link.confirmMessage = value }
    /** @see dueuno.core.LinkDefinition#confirmMessageArgs */
    void setConfirmMessageArgs(List value) { link.confirmMessageArgs = value }
    /** @see dueuno.core.LinkDefinition#confirmMessageOnConfirm */
    void setConfirmMessageOnConfirm(ComponentEvent value) { link.confirmMessageOnConfirm = value }
}
