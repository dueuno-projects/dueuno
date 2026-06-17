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

import dueuno.commons.utils.ObjectUtils
import dueuno.elements.Component
import dueuno.elements.Control
import dueuno.elements.Elements
import dueuno.elements.controls.HiddenField
import dueuno.exceptions.ElementsException
import dueuno.types.Type
import dueuno.types.Types
import grails.gorm.validation.ConstrainedProperty
import grails.validation.Validateable
import groovy.contracts.Requires
import groovy.transform.CompileStatic

import java.lang.reflect.Field

/**
 * A form component that groups one or more {@link FormField} controls and manages
 * data binding, constraint resolution, and key-field tracking.
 * <p>
 * Fields are added via {@link #addField(Map)}, which automatically reads GORM or
 * {@link grails.validation.Validateable} constraints (nullability, max size) from the
 * class supplied as {@code validate}. Hidden key fields can be registered with
 * {@link #addKeyField} to carry primary-key values across AJAX submissions.
 * </p>
 * <p>
 * Values are populated in bulk by calling {@link #setValues(Object)} with a domain
 * object or command object; each control's value is resolved from the matching
 * property name, falling back to request parameters and then to the control's own
 * default value.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class Form extends Component {

    /** Hidden {@link FormField} instances that carry primary-key or surrogate-key values. */
    List keyFields

    /** The GORM domain class or {@link grails.validation.Validateable} command class
     *  used to resolve field constraints ({@code nullable}, {@code maxSize}). */
    Class validate

    /** Whether the browser's autocomplete feature is enabled for this form. */
    Boolean autocomplete

    /** Whether the first field in the form should receive focus automatically on render. */
    Boolean autofocus

    /**
     * Creates a {@code Form} instance configured from the supplied argument map.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code validate}/{@code constraints} ({@link Class}) — domain or command class for constraint lookup,
     *             {@code autocomplete} ({@link Boolean}, default {@code false}),
     *             {@code autofocus} ({@link Boolean}, default {@code true}),
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    Form(Map args) {
        super(args)

        // DEFAULTS
        //
        keyFields = []
        validate = args.validate as Class ?: args.constraints as Class ?: null

        autocomplete = (args.autocomplete == null) ? false : args.autocomplete
        autofocus = (args.autofocus == null) ? true : args.autofocus
    }

    /**
     * Returns all child components of this form that are {@link FormField} instances.
     *
     * @return an ordered list of {@link FormField} children
     */
    List<FormField> getComponents() {
        List<FormField> fields = []

        for (component in super.components) {
            if (component in FormField) {
                fields.add(component as FormField)
            }
        }

        return fields
    }

    /**
     * Adds a new {@link FormField} to this form, automatically resolving GORM/command
     * constraints ({@code nullable}, {@code maxSize}) for the field identified by
     * {@code args.id}.
     * <p>
     * If {@code args.class} is a {@link Control} subclass, the control is registered
     * in the form's controls map as well as wrapped in a {@code FormField}. Otherwise
     * it is added as a plain component.
     * </p>
     *
     * @param args field configuration; required keys: {@code id} ({@link String}),
     *             {@code class} ({@link Class}); optional keys include {@code nullable},
     *             {@code maxSize}, {@code label}, {@code help}, {@code cols}, {@code submit},
     *             {@code readonly}, and all keys accepted by the target control/component
     * @return the newly created {@link FormField}
     */
    @Requires({ args.class && args.id })
    FormField addField(Map args) {
        Class clazz = args.class as Class
        String id = args.id

        Map fieldConstraints = getFieldConstraints(validate, id)
        // Auto assigns 'nullable' flag
        if (args.nullable == null) {
            args.nullable = fieldConstraints.nullable == null ? true : fieldConstraints.nullable
        }
        // Auto assign 'maxSize'
        if (args.maxSize == null) {
            args.maxSize = fieldConstraints.maxSize ?: 255 // GORM default value for strings
        }

        // Auto squeeze if first component
        if (args.sqeeze == null && !components.size()) {
            args.squeeze = true
        }

        // Set common args
        if (args.submit == null) args.submit = getId()
        if (args.readonly == null) args.readonly = readonly
        if (!args.primaryTextColor) args.primaryTextColor = primaryTextColor
        if (!args.primaryBackgroundColor) args.primaryBackgroundColor = primaryBackgroundColor

        // Add control/component. We add them to the components to be able to address
        // them directly instead of passing through the FormField (Eg. form['controlName'])
        Component component
        if (clazz in Control) {
            component = addControl(args)
            setDefaultValue(component)

        } else {
            component = addComponent(args)
        }

        // Set field specific args
        if (args.help == null) args.help = ''
        if (args.label == null) args.label = buildLabel(id)
        if (args.cols == null) args.cols = 12

        args.remove('cssClass')
        args.remove('events')
        args.component = component
        args.putAll(component.containerSpecs)

        FormField field = addComponent(FormField, id + 'Field', args)
        return field
    }

    /**
     * Resolves the GORM or {@link grails.validation.Validateable} constraints for the
     * named field in the given class, following dot-notation paths recursively.
     * Returns an empty map when the class or field cannot be found, or when the class
     * is neither a domain class nor a {@code Validateable}.
     *
     * @param domainOrCommandClass the domain or command class to inspect
     * @param fieldName            the field name or dot-separated path (e.g. {@code "address.city"})
     * @return a map with {@code nullable} and {@code maxSize} keys, or an empty map
     * @throws dueuno.exceptions.ElementsException if the class is neither a GORM domain nor a {@code Validateable}
     */
    private Map getFieldConstraints(Class domainOrCommandClass, String fieldName) {
        if (!domainOrCommandClass || !fieldName)
            return [:]

        List<String> fieldParts = fieldName.split('\\.') as List<String>
        Field field = domainOrCommandClass.declaredFields.find { it.name == fieldParts[0] }
        if (!field) {
            return [:]
        }

        Boolean isDomainClass = Elements.isDomainClass(domainOrCommandClass)
        Boolean isCommandClass = Validateable.isAssignableFrom(domainOrCommandClass)
        if (!isDomainClass && !isCommandClass) {
            throw new ElementsException(
                    "Cannot retrieve constraints from class '${domainOrCommandClass}', " +
                            "please specify a GORM Domain class or a class that implements '${Validateable.name}'")
        }

        Map<String, ConstrainedProperty> constraintsMap = isDomainClass
                ? domainOrCommandClass['constrainedProperties'] as Map<String, ConstrainedProperty>
                : domainOrCommandClass['constraintsMap'] as Map<String, ConstrainedProperty>

        Boolean isSimpleField = (fieldParts.size() == 1)
        if (isSimpleField) {
            Map result = [:]
            ConstrainedProperty fieldConstraints = constraintsMap[fieldName]
            if (fieldConstraints) {
                result.nullable = fieldConstraints['nullable']
                result.maxSize = fieldConstraints['maxSize']
            }
            return result

        } else { // move to the next field in path
            Class nextFieldClass = field.getType()
            String nextFieldName = fieldParts.tail().join('.')
            return getFieldConstraints(nextFieldClass, nextFieldName)
        }
    }

    /**
     * Adds a hidden key field whose type is inferred automatically from the given value.
     *
     * @param id    the field identifier (maps to the corresponding parameter name on submission)
     * @param value the key value; its type is detected via {@link Types#getType(Object)}
     */
    void addKeyField(String id, Object value = null) {
        String valueType = Types.getType(value)
        addKeyField(id, valueType, value)
    }

    /**
     * Adds a hidden key field with an explicit {@link Type} constant.
     *
     * @param id        the field identifier
     * @param valueType the {@link Type} of the key value
     * @param value     the key value
     */
    void addKeyField(String id, Type valueType, Object value) {
        addKeyField(id, valueType.toString(), value)
    }

    /**
     * Adds a hidden key field using a raw type name string, accommodating custom types
     * (e.g. {@code "MONEY"}, {@code "QUANTITY"}) beyond the built-in {@link Type} constants.
     * Enum values are automatically coerced to their {@link String} representation with type
     * {@link Type#TEXT}.
     *
     * @param id        the field identifier
     * @param valueType the type name string (must be a registered type)
     * @param value     the key value
     * @throws dueuno.exceptions.ElementsException if {@code valueType} is not a registered type
     */
    // We are using a Sting instead of a Type to accomodate custom types (Eg. Money, Quantity, etc)
    void addKeyField(String id, String valueType, Object value) {
        if (!Types.isType(valueType)) {
            throw new ElementsException("Type '${valueType}' does not exist. Please choose one from: ${Types.availableTypeNames}")
        }

        if (value in Enum) {
            valueType = Type.TEXT.toString()
            value = value.toString()
        }

        FormField field = addField(
                class: HiddenField,
                id: id,
                valueType: valueType,
                value: value,
        )
        keyFields += field
    }

    /**
     * Serialises this form's properties to JSON, adding {@link #autofocus} to the
     * standard component properties.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this form's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                autofocus: autofocus,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    /**
     * Propagates the read-only state to this form and all of its field components.
     *
     * @param isReadonly {@code true} to make the form and all its fields read-only
     */
    @Override
    void setReadonly(Boolean isReadonly) {
        super.setReadonly(isReadonly)
        for (field in components) {
            (field as FormField).component.readonly = isReadonly
        }
    }

    /**
     * Populates all controls in this form from the properties of the given object.
     * <p>
     * If {@code obj} has an {@code id} property and the form does not already contain an
     * {@code id} control, a hidden key field is added automatically. Each control's value
     * is resolved from the matching property on {@code obj}, falling back to request
     * parameters and then to the control's own default value.
     * </p>
     *
     * @param obj the domain object or command object whose properties are used to populate the form
     */
    void setValues(Object obj) {
        if (ObjectUtils.hasId(obj) && !getControl('id')) {
            // This is not sufficient, we must add the fields that represents the real GORM key
            // that could be a composite key
            addKeyField('id', Type.NUMBER, obj['id'])
        }

        for (controlEntry in controls) {
            Control control = controlEntry.value
            setValue(control, obj)
        }
    }

    /**
     * Sets the value of a single control from the given object.
     * <p>
     * If the control's current value equals its default value (i.e. no explicit value was
     * set), the value is cleared so that the object's property takes precedence.
     * If a non-null property value is found on {@code obj}, it is applied; otherwise
     * {@link #setDefaultValue(Control)} is called.
     * </p>
     *
     * @param control the control to populate
     * @param obj     the source object; may be {@code null}
     */
    private void setValue(Control control, Object obj = null) {
        if (control.value == control.defaultValue) {
            // If value == defaultValue then the default value was applied when the control
            // was added to the form. That covers the case when setValues() is not called
            // (eg: form is displayed to create a new object with default values)
            //
            // If we are here then we want to set the default value only if the corresponding
            // property is null in the obj
            control.value = null
        }

        if (control.value != null) {
            return
        }

        Object value = ObjectUtils.getValue(obj, control.id)
        if (value != null) {
            control.value = value

        } else {
            setDefaultValue(control)
        }
    }

    /**
     * Applies a default value to the given control if it does not already have one.
     * Checks request parameters first; if a matching parameter exists it is used.
     * Otherwise the control's own {@code defaultValue} is applied.
     *
     * @param control the control to receive the default value
     */
    private void setDefaultValue(Control control) {
        if (control.value != null) {
            return
        }

        if (requestParams.containsKey(control.id)) {
            control.value = requestParams[control.id]

        } else if (control.defaultValue != null) {
            control.value = control.defaultValue
        }
    }

}

