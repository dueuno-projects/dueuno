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

import dueuno.commons.utils.LogUtils
import dueuno.core.PrettyPrinterProperties
import dueuno.core.WebRequestAware
import dueuno.elements.style.Color
import dueuno.exceptions.ElementsException
import dueuno.utils.EnvUtils
import groovy.contracts.Requires
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * A Component is the basic building block of the Elements framework. Every UI element displayed to the user is a
 * Component. A Component can be made of subcomponents. Eg. A Table component can be made of several Row components
 * that can be made of several Cell components.
 *
 * A Component can contain one or more Controls.
 * See {@link Control Control}
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@Slf4j
@CompileStatic
abstract class Component implements WebRequestAware, Serializable {

    /** A component can contain Sub-Components */
    private Map<String, Component> components

    /** A component can contain Controls */
    private Map<String, Control> controls

    /** A component can respond to Events */
    private Map<String, ComponentEvent> events

    /** Path of the component GSP file. Defaults to '/dueuno/elements/components/'. Eg: '/mycomponents/' */
    protected String viewPath

    /** Name of the component GSP file. Defaults to the component simple class name. Eg: 'MyChart'. */
    protected String viewTemplate

    /** Id of the component instance. This is mandatory, it must be unique and provided in the constructor. */
    String id

    /** Shows or hides the component without changing the layout */
    Boolean visible

    /** Displays or hides the component, adding or removing it from the layout */
    Boolean display

    /** Readonly controls are disabled */
    Boolean readonly

    /** The component won't participate in keyboard or mouse selection (focus) */
    Boolean skipFocus

    /** The component is sticky on top */
    Boolean sticky

    /**
     * Contains instructions for the container. The container component may or may not respect them,
     * see the documentation for the specific container component.
     * */
    Map containerSpecs

    /** Text color */
    String textColor

    /** Background color */
    String backgroundColor

    /** Colors */
    String mainTextColor
    List<Integer> mainTextColorInt
    String mainBackgroundColor
    List<Integer> mainBackgroundColorInt
    String mainForegroundColor
    List<Integer> mainForegroundColorInt

    String frameTextColor
    List<Integer> frameTextColorInt
    String frameBackgroundColor
    List<Integer> frameBackgroundColorInt

    String primaryTextColor
    List<Integer> primaryTextColorInt
    String primaryBackgroundColor
    List<Integer> primaryBackgroundColorInt
    Double primaryBackgroundColorAlpha

    String secondaryTextColor
    List<Integer> secondaryTextColorInt
    String secondaryBackgroundColor
    List<Integer> secondaryBackgroundColorInt

    String requiredTextColor
    List<Integer> errorTextColorInt

    /** Custom CSS */
    String cssClass

    @Requires({ args.id })
    Component(Map args) {
        this.id = args.id
        components = [:]
        controls = [:]
        events = [:]; registerEvents(args)

        try {
            getRequest()
        } catch (Exception e) {
            throw new ElementsException("A 'Component' can only be instantiated when a 'GrailsWebRequest' is available (eg. in a controller).", e)
        }

        viewTemplate = args.viewTemplate ?: getClassName()
        viewPath = args.viewPath ?: '/dueuno/elements/components/'

        containerSpecs = args.containerSpecs as Map ?: [:]
        containerSpecs.attributes = [:]

        // Avoid calling the setter so we have 'null' instead of 'false' to be able to cleanupProperties()
        this.@visible = args.visible
        this.@display = args.display
        this.@readonly = args.readonly
        this.@skipFocus = args.skipFocus
        this.@sticky = args.sticky

        textColor = args.textColor ?: ''
        backgroundColor = args.backgroundColor ?: ''

        mainTextColor = args.mainTextColor ?: '#333030'
        mainTextColorInt = Color.hexToIntColor(mainTextColor) ?: warnColorError(mainTextColor)
        mainBackgroundColor = args.mainBackgroundColor ?: '#f4f1f1'
        mainBackgroundColorInt = Color.hexToIntColor(mainBackgroundColor) ?: warnColorError(mainBackgroundColor)
        mainForegroundColor = args.mainForegroundColor ?: '#ffffff'
        mainForegroundColorInt = Color.hexToIntColor(mainForegroundColor) ?: warnColorError(mainForegroundColor)

        frameTextColor = args.frameTextColor ?: '#f4f1f1'
        frameTextColorInt = Color.hexToIntColor(frameTextColor) ?: warnColorError(frameTextColor)
        frameBackgroundColor = args.frameBackgroundColor ?: '#333030'
        frameBackgroundColorInt = Color.hexToIntColor(frameBackgroundColor) ?: warnColorError(frameBackgroundColor)

        primaryTextColor = args.primaryTextColor ?: '#ffffff'
        primaryTextColorInt = Color.hexToIntColor(primaryTextColor) ?: warnColorError(primaryTextColor)
        primaryBackgroundColor = args.primaryBackgroundColor ?: '#aa0000'
        primaryBackgroundColorInt = Color.hexToIntColor(primaryBackgroundColor) ?: warnColorError(primaryBackgroundColor)
        primaryBackgroundColorAlpha = (Double) (args.primaryBackgroundColorAlpha == null ? 0.15d : args.primaryBackgroundColorAlpha)

        secondaryTextColor = args.secondaryTextColor ?: '#ffffff'
        secondaryTextColorInt = Color.hexToIntColor(secondaryTextColor) ?: warnColorError(secondaryTextColor)
        secondaryBackgroundColor = args.secondaryBackgroundColor ?: '#504545'
        secondaryBackgroundColorInt = Color.hexToIntColor(secondaryBackgroundColor) ?: warnColorError(secondaryBackgroundColor)

        requiredTextColor = args.requiredTextColor ?: '#aa0000'
        errorTextColorInt = Color.hexToIntColor(requiredTextColor) ?: warnColorError(requiredTextColor)

        cssClass = args.cssClass ?: ''
    }

    private void registerEvents(Map args) {
        List submit = []
        if (args.submit) {
            submit = args.submit in List
                    ? args.submit as List
                    : [args.submit as String]
        }

        for (arg in args) {
            String eventName = arg.key
            String eventValue = arg.value

            if (eventName.startsWith('on')) {
                Map actionMap = parseControllerAction(eventValue)
                String event = (eventName - 'on').toLowerCase()
                Map eventArgs = [
                        event     : event,
                        controller: actionMap.controller,
                        action    : actionMap.action,
                        submit    : submit ?: [getId()],
                        loading   : event == 'load' ? false : args.loading,
                ]
                on(args + eventArgs)
            }
        }
    }

    private Map parseControllerAction(String value) {
        def parts = value.tokenize('.')

        return [
                controller: parts.size() > 1 ? parts[0] : null,
                action    : parts.size() > 1 ? parts[1] : parts[0]
        ]
    }

    String toString() {
        return getClassName() + ' View: ' + getView()
    }

    /**
     * Returns true if the the app is running in grails DEV environment
     * @return true if the the app is running in grails DEV environment
     */
    static Boolean isDevelopment() {
        return EnvUtils.isDevelopment()
    }

    /**
     * Returns true if the the app is running in grails PROD environment
     * @return true if the the app is running in grails PROD environment
     */
    static Boolean isProduction() {
        return EnvUtils.isProduction()
    }

    /**
     * Returns the component class name
     * @return The component class name
     */
    String getClassName() {
        return getClass().getSimpleName()
    }

    /**
     * Automatically builds a prefixed label ready for i18n starting from a name (usually a Component name)
     *
     * @param id Name of the Component
     *
     * @return A prefixed label ready for i18n
     */
    protected String buildLabel(String id, PrettyPrinterProperties prettyPrinterProperties = null) {
        String prefix = prettyPrinterProperties?.textPrefix ?: controllerName
        return prefix + '.' + id
    }

    /**
     * Returns the view pathname used by the component
     * @return The view pathname used by the component
     */
    String getView() {
        return viewPath + viewTemplate
    }

    /**
     * Returns the component itself to be used as model for the view (in the GSP view call properties and methods
     * prefixing them with 'c.')
     *
     * @return The component itself to be used as model for the view
     */
    Map getModel() {
        return [c: this]
    }

    /**
     * Returns an instance of a specific component Class
     * @return An instance of a Component
     */
    static <T> T createInstance(Class<T> clazz, String id = null, Map args = [:]) {
        try {
            args['id'] = id ?: clazz.simpleName.toLowerCase()
            Object instance = clazz.newInstance(args)
            return clazz.cast(instance)

        } catch (Exception e) {
            log.error LogUtils.logStackTrace(e)
            throw new ElementsException("Cannot instantiate class '${clazz.name}': ${e.message}")
        }
    }

    /**
     * Creates an instance of a Component
     *
     * @param args initialization Map
     *
     * @return An instance of the specified component class
     */
    @Requires({ args.id })
    public <T> T createComponent(Map args) {
        Class<T> clazz = args.class as Class<T>
        String id = args.id

        throwExceptionIfNameExists(args)
        throwExceptionIfNotComponent(clazz)
        initializeComponent(args)

        args.remove('class')
        args.remove('id')
        return createInstance(clazz, id, args)
    }

    /**
     * Creates an instance of a Component
     *
     * @param clazz The component Class
     * @param id The component name
     * @param args initialization Map
     *
     * @return An instance of the specified component class
     */
    public <T> T createComponent(Class<T> clazz, String id = null, Map args = [:]) {
        args['class'] = clazz
        args['id'] = id ?: clazz.simpleName.toLowerCase()
        return createComponent(args)
    }

    private void throwExceptionIfNameExists(Map args) {
        if (args.replace)
            return

        String id = args.id
        String err = "with the same id '${id}' aready exists, please choose a different id."
        if (getControl(id)) {
            throw new ElementsException("A control ${err}")
        }
        if (getComponent(id)) {
            throw new ElementsException("A component ${err}")
        }
    }

    private void throwExceptionIfNotComponent(Class clazz) {
        if (clazz !in Component) {
            throw new ElementsException("Cannot create '${clazz}' because it's not of type '${Component.canonicalName}'.")
        }
    }

    private List<Integer> warnColorError(String hexRgbColor) {
        log.warn "Color '${hexRgbColor}' is not a valid RGB color. Component '${getId()}', primaryTextColor: '${primaryTextColor}', primaryBackgroundColor: '${primaryBackgroundColor}'"
        return []
    }

    private void initializeComponent(Map args) {
        args.textColor = args.textColor ?: this.textColor
        args.backgroundColor = args.backgroundColor ?: this.backgroundColor

        args.mainTextColor = this.mainTextColor
        args.mainForegroundColor = this.mainForegroundColor
        args.mainBackgroundColor = this.mainBackgroundColor
        args.frameTextColor = this.frameTextColor
        args.frameBackgroundColor = this.frameBackgroundColor
        args.primaryTextColor = this.primaryTextColor
        args.primaryBackgroundColor = this.primaryBackgroundColor
        args.primaryBackgroundColorAlpha = this.primaryBackgroundColorAlpha
        args.secondaryTextColor = this.secondaryTextColor
        args.secondaryBackgroundColor = this.secondaryBackgroundColor
        args.requiredTextColor = this.requiredTextColor
    }

    String getPrimaryBackgroundColorShade() {
        return "rgba(${primaryBackgroundColorInt.join(', ')}, ${primaryBackgroundColorAlpha}) !important"
    }

    /**
     * Creates a component and adds it as sub-component with name = lowercase of class name.
     *
     * @param clazz The component Class
     * @param args initialization Map
     *
     * @return An instance of the specified component class
     */
    public <T> T addComponent(Class<T> clazz, Map args = [:]) {
        args['class'] = clazz
        args['id'] = clazz.simpleName.toLowerCase()
        return addComponent(args)
    }

    /**
     * Creates a component and adds it as sub-component.
     *
     * @param clazz The component Class
     * @param id The component name
     * @param args initialization Map
     *
     * @return An instance of the specified component class
     */
    public <T> T addComponent(Class<T> clazz, String id, Map args = [:]) {
        args['class'] = clazz
        args['id'] = id
        return addComponent(args)
    }

    /**
     * Adds a component as sub-component.
     *
     * @param component The component to be added as sub-component
     *
     * @return An instance of the added component
     */
    Component addComponent(Component component) {
        components[component.id] = component
        return component
    }

    Component addComponentBefore(String beforeComponentName, Component component) {
        Map<String, Component> results = [:]

        for (item in components) {
            String componentName = item.value.id
            if (componentName == beforeComponentName) {
                results.put(component.id, component)
            }

            results.put(item.key, item.value)
        }

        components = results
        return component
    }

    public <T> T addComponentBefore(String beforeComponentName, Class<T> clazz, String id = null, Map args = [:]) {
        T newComponent = createComponent(clazz, id, args)
        Map<String, Component> results = [:]

        for (item in components) {
            String componentName = item.value.id
            if (componentName == beforeComponentName) {
                results.put(id, newComponent as Component)
            }

            results.put(item.key, item.value)
        }

        components = results
        return newComponent
    }

    /**
     * Creates a component and adds it as sub-component
     *
     * @param args initialization Map
     *
     * @return An instance of the specified component class
     */
    public <T> T addComponent(Map args) {
        T component = createComponent(args)

        String before = args.before
        if (before) {
            addComponentBefore(before, component as Component)
        } else {
            addComponent(component as Component)
        }

        return component
    }

    /**
     * Removes a component as sub-component
     * @param id The name of the component to remove
     */
    void removeComponent(String id) {
        components.remove(id)
    }

    /**
     * Removes all sub-components
     */
    void removeAllComponents() {
        components.clear()
    }

    /**
     * Returns true if this component contains a sub-component with the specified class
     *
     * @param clazz The class of the component to test
     *
     * @return true if the component has been found, false otherwise
     */
    Boolean hasComponent(Class clazz) {
        return components.find { it.value?.getClass() == clazz }
    }

    /**
     * Returns true if this component contains a sub-component with the specified name and class
     *
     * @param id The name of the component to test
     * @param clazz The class of the component to test
     *
     * @return true if the component has been found, false otherwise
     */
    Boolean hasComponent(String id, Class clazz) {
        Component c = getComponent(id)
        if (c) {
            return c.getClass() == clazz
        } else {
            return false
        }
    }

    /**
     * Retrieves an instance of the sub-component identified by its name
     *
     * @param id The name of the component to retrieve
     *
     * @return The component matching the provided name
     */
    Component getComponent(String id) {
        return components[id]
    }

    /**
     * Returns a list containing all first level sub-components. This method does not recursively descend into the
     * sub-components tree
     *
     * @return A list containing all first level sub-components
     */
    List<Component> getComponents() {
        return components.collect { it.value }
    }


    /**
     * Creates an instance of a Control
     *
     * @param args Named parameters. At least the 'class' of the control that has to be created and its 'name'
     * must be specified. You can provide other parameters to configure the control, please see the documentation
     * of the specific control.
     *
     * @return An instance of the specified control class
     */
    @Requires({ args.id && args.class })
    protected <T> T createControl(Map args) {
        Class<T> clazz = args.class as Class<T>
        String id = args.id

        throwExceptionIfNameExists(args)
        initializeComponent(args)

        args.remove('class')
        args.remove('id')
        return createInstance(clazz, id, args)
    }

    /**
     * Creates a control and adds it as sub-component.
     * See {@link Component#createControl() createControl}
     *
     * @param args Named parameters. At least the 'class' of the control that has to be created and its 'name'
     * must be specified. You can provide other parameters to configure the control, please see the documentation
     * for the control itself.
     *
     * @return An instance of the specified component class
     */
    Control addControl(Map args) {
        Control control = createControl(args)
        return addControl(control)
    }

    /**
     * Adds a Control as sub-component.
     *
     * @param control The control to be added as sub-component
     *
     * @return An instance of the added control
     */
    Control addControl(Control control) {
        controls[control.id] = control
        return control
    }

    /**
     * Removes a control as sub-component
     * @param id The name of the control to remove
     */
    void removeControl(String id) {
        controls.remove(id)
    }

    /**
     * Retrieves an instance of the control identified by its name
     *
     * @param id The name of the control to retrieve
     *
     * @return The control matching the provided name
     */
    Control getControl(String id) {
        return controls[id]
    }

    /**
     * Returns a Map containing all controls
     *
     * @return A Map containing all controls
     */
    Map<String, Control> getControls() {
        return controls
    }

    /**
     * Returns a component or a control identified by the specified name. This enables the squared brackets syntax to
     * access sub-components and controls in a component
     *
     * @param id The name of the component or control to access
     *
     * @return The instance of the specified component or control
     */
    Component getAt(String id) {
        Component component = getComponent(id)
        if (component) {
            return component
        }

        Control control = getControl(id)
        if (control) {
            return control

        }

        return null
    }

    /**
     * Registers an event for a component. Components can provide some events to which one can attach an action.
     * Please refer to each specific component documentation to find out which events can be registered.
     *
     * @param args Named parameters. At least an 'event' and an 'action' must be specified
     */
    @Requires({ args.event && args.action })
    Component on(Map args) {
        args.controller = args.controller ?: controllerName

        List eventList = (args.event in List)
                ? (List) args.event
                : [args.event]
        args.remove('event')

        Map<String, ComponentEvent> eventMap = [:]
        for (event in eventList) {
            String eventName = event.toString().toLowerCase()
            ComponentEvent eventObj = new ComponentEvent(args)

            eventObj.on = eventName
            eventMap[eventName] = eventObj
        }

        events += eventMap
        return this
    }

    Boolean hasEvent(String event) {
        String eventName = event.toString().toLowerCase()
        return events.find { it.key == eventName } ? true : false
    }

    void addContainerAttribute(String name, String value) {
        containerSpecs.attributes[name] = value
    }

    String getContainerAttributes() {
        Map attributes = containerSpecs.attributes as Map
        String result = ''

        for (attribute in attributes) {
            result += (attribute.key as String + '="' + attribute.value as String + '" ')
        }

        return result
    }

    /**
     * Returns a list of all registered events in JSON format
     * @return A list of all registered events in JSON format
     */
    String getEventsAsJSON() {
        Map<String, Map> results = [:]

        for (event in events) {
            String name = event.key
            ComponentEvent value = event.value
            results[name] = value.asMap()
        }

        Map cleanedUpEvents = cleanupProperties(results)
        return Elements.encodeAsJSON(cleanedUpEvents)
    }

    /**
     * Returns the component properties in JSON format
     * @return the component properties in JSON format
     */
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [:]
        if (visible != null) thisProperties.visible = visible
        if (display != null) thisProperties.display = display
        if (readonly != null) thisProperties.readonly = readonly
        if (sticky != null) thisProperties.sticky = sticky

        Map cleanedupProperties = cleanupProperties(thisProperties + properties)
        return Elements.encodeAsJSON(cleanedupProperties)
    }

    String getCssStyleColors() {
        String result = ''
        if (textColor) result += "color: ${textColor}; "
        if (backgroundColor) result += "background-color: ${backgroundColor};"
        return result
    }

    private static Map cleanupProperties(Map items) {
        Map results = [:]

        for (item in items) {
            String name = item.key
            Object value = item.value

            if (value in Map) {
                Map cleanedUpProperty = cleanupProperties(value as Map)
                if (cleanedUpProperty) {
                    results[name] = cleanedUpProperty
                }
                continue
            }

            if (value == null || value == '' || value == [] || value == [:]) {
                continue
            }

            results[name] = value
        }

        return results
    }

    // We implement the setters to cover the case 'display = null'
    // so that is is interpreted as false
    void setDisplay(Boolean value) {
        display = value ? true : false
    }

    void setVisible(Boolean value) {
        visible = value ? true : false
    }

    void setReadonly(Boolean value) {
        readonly = value ? true : false
    }

}


