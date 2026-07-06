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
package dueuno.elements.controls

import dueuno.commons.utils.ObjectUtils
import dueuno.core.PrettyPrinter
import dueuno.core.PrettyPrinterProperties
import dueuno.elements.Component
import dueuno.elements.Control
import dueuno.elements.Elements
import dueuno.elements.components.Button
import dueuno.exceptions.ElementsException
import dueuno.types.Type
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * A dropdown/select control that renders a list of options backed by Select2.
 * <p>
 * Options can be supplied in four ways (checked in order):
 * </p>
 * <ul>
 *   <li>{@code optionsFromRecordset} — built from a GORM/collection result set.</li>
 *   <li>{@code optionsFromList} — built from a plain list of values.</li>
 *   <li>{@code optionsFromEnum} — built from an enum class.</li>
 *   <li>{@code options} — a pre-built {@code [key: label]} map.</li>
 * </ul>
 * <p>
 * Supports single and multiple selection, optional search, auto-clear, and an optional
 * action {@link Button} rendered next to the selector. When a single option is available
 * and {@link #autoSelect} is {@code true}, that option is pre-selected automatically.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class Select extends Control {

    /** The resolved list of option maps ({@code id} → key, {@code text} → display label). */
    List<Map<String, String>> options

    /** Optional closure invoked for each option during option-list construction. */
    Closure forEachOption

    /** Action button rendered adjacent to the selector (hidden by default). */
    Button actions

    /** Placeholder text shown when no value is selected. */
    String placeholder

    /** When {@code true}, a clear (×) button is shown to deselect the current value. */
    Boolean allowClear

    /** When {@code true}, auto-selects the single available option if the field is not nullable. */
    Boolean autoSelect

    /** When {@code true}, allows selection of multiple values. */
    Boolean multiple

    /** When {@code true}, the Select2 search box is enabled. */
    Boolean search

    /** Minimum number of characters required before the search triggers. Defaults to {@code 0}. */
    Integer searchMinInputLength

    /**
     * Creates a {@code Select} instance configured from the supplied argument map.
     * Resolves options from one of {@code optionsFromRecordset}, {@code optionsFromList},
     * {@code optionsFromEnum}, or {@code options}, then auto-selects when applicable and
     * creates the adjacent action button.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code autoSelect} ({@link Boolean}, default {@code true}),
     *             {@code multiple} ({@link Boolean}, default {@code false}),
     *             {@code search} ({@link Boolean}),
     *             {@code allowClear} ({@link Boolean}),
     *             {@code placeholder} ({@link String}),
     *             {@code searchMinInputLength} ({@link Integer}, default {@code 0}),
     *             {@code optionsFromRecordset}, {@code optionsFromList}, {@code optionsFromEnum},
     *             {@code options}, {@code keys}, {@code keysSeparator}, {@code exclude},
     *             {@code transformer}, {@code renderTextPrefix}, {@code forEachOption} ({@link Closure}),
     *             plus all keys accepted by {@link Control#Control(Map)}
     */
    Select(Map args) {
        super(args)

        autoSelect = (args.autoSelect == null) ? true : args.autoSelect
        multiple = (args.multiple == null) ? false : args.multiple
        forEachOption = args.forEachOption as Closure ?: null
        placeholder = args.placeholder ? message(args.placeholder as String) : message('control.select.placeholder')

        searchMinInputLength = (args.searchMinInputLength == null) ? 0 : args.searchMinInputLength as Integer

        if (args.optionsFromRecordset) {
            search = (args.search == null) ? true : args.search
            allowClear = (args.allowClear == null) ? true : args.allowClear
            options = optionsFromRecordset(
                    recordset: args.optionsFromRecordset,
                    keys: args.keys,
                    keysSeparator: args.keysSeparator,
                    prettyPrinter: prettyPrinter,
                    transformer: args.transformer,
                    forEachOption: args.forEachOption,
                    textPrefix: prettyPrinterProperties.textPrefix,
                    renderTextPrefix: args.renderTextPrefix == null ? false : args.renderTextPrefix,
                    locale: locale,
            )

        } else if (args.optionsFromList) {
            search = (args.search == null) ? false : args.search
            allowClear = (args.allowClear == null) ? false : args.allowClear
            options = optionsFromList(
                    list: args.optionsFromList,
                    exclude: args.exclude,
                    prettyPrinter: prettyPrinter,
                    transformer: args.transformer,
                    forEachOption: args.forEachOption,
                    textPrefix: prettyPrinterProperties.textPrefix,
                    renderTextPrefix: args.renderTextPrefix == null ? true : args.renderTextPrefix,
                    locale: locale,
            )

        } else if (args.optionsFromEnum) {
            search = (args.search == null) ? false : args.search
            allowClear = (args.allowClear == null) ? false : args.allowClear
            options = optionsFromEnum(
                    enum: args.optionsFromEnum,
                    exclude: args.exclude,
                    prettyPrinter: prettyPrinter,
                    transformer: args.transformer,
                    forEachOption: args.forEachOption,
                    textPrefix: prettyPrinterProperties.textPrefix,
                    renderTextPrefix: args.renderTextPrefix == null ? true : args.renderTextPrefix,
                    locale: locale,
            )

        } else {
            search = (args.search == null) ? true : args.search
            allowClear = (args.allowClear == null) ? false : args.allowClear
            options = options(
                    options: args.options,
                    prettyPrinter: prettyPrinter,
                    transformer: args.transformer,
                    forEachOption: args.forEachOption,
                    textPrefix: prettyPrinterProperties.textPrefix,
                    renderTextPrefix: args.renderTextPrefix == null ? true : args.renderTextPrefix,
                    locale: locale,
            )
        }

        // Automatically selects the first element if it's the only choice
        if (autoSelect && !nullable && options.size() == 1) {
            defaultValue = options[0]
        }

        setMultiple(args.multiple as Boolean)

        for (option in options) {
            option.value = prettyPrint(option.value)
        }

        actions = createControl(
                class: Button,
                id: 'actions',
                group: true,
                dontCreateDefaultAction: true,
                cssClass: 'hide',
        )
    }

    /**
     * Sets whether multiple values can be selected.
     * When {@code true}, {@link #allowClear} is forced to {@code false} as it is
     * incompatible with multiple selection mode.
     *
     * @param value {@code true} to enable multiple selection; {@code null} is treated as {@code false}
     */
    void setMultiple(Boolean value) {
        this.multiple = (value == null) ? false : value
        if (multiple) allowClear = false
    }

    /**
     * Returns the resolved options as a flat map of option ID → display text.
     *
     * @return a map of option key strings to their localised display labels
     */
    Map getOptions() {
        return options.collectEntries { [(it.id): it.text]}
    }

    /**
     * Registers a {@code change} event handler on this control if one has not already been set,
     * so that form submission is triggered on selection change.
     *
     * @param args event handler arguments forwarded to {@link Control#on(Map)}
     * @return this control
     */
    @Override
    Component onSubmit(Map args) {
        String submitEvent = 'change'
        if (!hasEvent(submitEvent)) {
            args.event = submitEvent
            on(args)
        }

        return this
    }

    /**
     * Sets the selected value(s) for this control.
     * <ul>
     *   <li>A {@link Collection} whose elements have an {@code id} property is unwrapped to a list of IDs.</li>
     *   <li>A single object with an {@code id} property is unwrapped to its ID.</li>
     *   <li>All other values are passed directly to the superclass.</li>
     * </ul>
     *
     * @param value the value to select; accepts {@code null}, a scalar, or a {@link Collection}
     */
    @Override
    void setValue(Object value) {
        if (value in Collection) {
            Collection collection = value as Collection
            if (ObjectUtils.hasId(collection[0])) {
                super.setValue(value.collect { it['id'] })
            }

        } else if (ObjectUtils.hasId(value)) {
            super.setValue(value['id'])

        } else {
            super.setValue(value)
        }
    }

    /**
     * Adds an action to the adjacent action {@link Button}.
     * Defaults {@code loading} to {@code false} if not specified.
     *
     * @param args action configuration arguments forwarded to {@link Button#addAction(Map)}
     * @return this control
     */
    Control addAction(Map args) {
        args.loading = args.loading != null ? args.loading : false
        actions.addAction(args)
        return this
    }

    /**
     * Removes an action from the adjacent action {@link Button}.
     *
     * @param args action identification arguments forwarded to {@link Button#removeAction(Map)}
     */
    void removeAction(Map args) {
        actions.removeAction(args)
    }

    //
    // Utils
    //

    /**
     * Builds a list of {@code [id, text]} option maps from a GORM/collection result set.
     * <p>
     * If the records have an {@code id} property and no explicit {@code keys} are given,
     * {@code "id"} is used automatically. Multiple keys are joined with {@code keysSeparator}
     * (default: {@code ","}). Each record's display text is rendered via
     * {@link PrettyPrinter#print(Object, PrettyPrinterProperties)}.
     * </p>
     *
     * @param args configuration map; recognised keys:
     *             {@code recordset} ({@link Collection}),
     *             {@code keys} ({@link List}&lt;{@link String}&gt;),
     *             {@code keysSeparator} ({@link String}, default {@code ","}),
     *             {@code forEachOption} ({@link Closure}),
     *             {@code prettyPrinter}, {@code transformer}, {@code textPrefix},
     *             {@code renderTextPrefix}, {@code locale}
     * @return a list of {@code [id: key, text: label]} maps
     * @throws dueuno.exceptions.ElementsException if the record has no {@code id} and no {@code keys} are given
     */
    static List<Map<String, String>> optionsFromRecordset(Map args) {
        Collection recordset = args.recordset as Collection ?: []
        List<String> keys = args.keys as List<String>
        String keysSeparator = args.keysSeparator ?: ','
        Closure forEachOption = args.forEachOption as Closure ?: null

        PrettyPrinterProperties prettyPrinterProperties = initializePrettyPrinterProperties(args, recordset.getAt(0))

        List<Map<String, String>> results = []

        // If the first record is a domain class we auto-setup 'id' as key
        Object firstRecord = recordset ? recordset.getAt(0) : null

        // If the object is a Hibernate Proxy then we get the real Domain Object
        // before to check for an ID
        if (firstRecord && !Elements.isDomainClass(firstRecord.getClass())) {
            firstRecord = GrailsHibernateUtil.unwrapIfProxy(firstRecord)
        }

        if (firstRecord) {
            if (!ObjectUtils.hasId(firstRecord) && !keys) {
                throw new ElementsException("Object does not contain an 'id' property. You must specify at least one key in the 'keys' list.")
            } else if (ObjectUtils.hasId(firstRecord) && !keys) {
                keys = ['id']
            }
        }

        for (row in recordset) {
            if (forEachOption) {
                forEachOption.call(row)
            }

            String text = PrettyPrinter.print(row, prettyPrinterProperties)

            results.add([id: buildKey(row, keys, keysSeparator), text: text])
        }
        return results
    }

    /**
     * Builds a list of {@code [id, text]} option maps from a plain list of values.
     * Values present in the {@code exclude} list are omitted.
     *
     * @param args configuration map; recognised keys:
     *             {@code list} ({@link List}),
     *             {@code exclude} ({@link List}),
     *             {@code forEachOption} ({@link Closure}),
     *             {@code prettyPrinter}, {@code transformer}, {@code textPrefix},
     *             {@code renderTextPrefix}, {@code locale}
     * @return a list of {@code [id: value, text: label]} maps
     */
    static List<Map<String, String>> optionsFromList(Map args) {
        List list = args.list as List ?: []
        List exclude = args.exclude as List ?: []
        Closure forEachOption = args.forEachOption as Closure ?: null
        PrettyPrinterProperties prettyPrinterProperties = initializePrettyPrinterProperties(args, list.getAt(0))

        List<Map<String, String>> results = []
        for (value in list) {
            if (exclude.contains(value)) {
                continue
            }

            if (forEachOption) {
                forEachOption.call(value)
            }

            String text = PrettyPrinter.print(value, prettyPrinterProperties)
            results.add([id: value as String, text: text])
        }

        return results
    }

    /**
     * Builds a list of {@code [id, text]} option maps from the values of an enum class.
     * Delegates to {@link #optionsFromList(Map)} after converting the enum constants to a list.
     *
     * @param args configuration map; recognised keys:
     *             {@code enum} (enum {@link Class}),
     *             plus all keys accepted by {@link #optionsFromList(Map)}
     * @return a list of {@code [id: enumName, text: label]} maps
     */
    @CompileDynamic
    static List<Map<String, String>> optionsFromEnum(Map args) {
        args.list = args.enum?.values()
        return optionsFromList(args)
    }

    /**
     * Builds a list of {@code [id, text]} option maps from a pre-built {@code [key: label]} map.
     *
     * @param args configuration map; recognised keys:
     *             {@code options} ({@link Map}),
     *             {@code forEachOption} ({@link Closure}),
     *             {@code prettyPrinter}, {@code transformer}, {@code textPrefix},
     *             {@code renderTextPrefix}, {@code locale}
     * @return a list of {@code [id: key, text: label]} maps
     */
    static List<Map<String, String>> options(Map args) {
        Map options = args.options as Map ?: [:]
        Closure forEachOption = args.forEachOption as Closure ?: null
        PrettyPrinterProperties prettyPrinterProperties = initializePrettyPrinterProperties(args, options.keySet().getAt(0))

        List<Map<String, String>> results = []
        for (entry in options) {
            if (forEachOption) {
                forEachOption.call(entry)
            }

            String text = PrettyPrinter.print(entry, prettyPrinterProperties)
            results.add([id: entry.key as String, text: text])
        }

        return results
    }

    /**
     * Initialises a {@link PrettyPrinterProperties} instance from the given argument map,
     * auto-detecting the pretty-printer from the first item when not explicitly provided.
     *
     * @param args  configuration map containing {@code textPrefix}, {@code renderTextPrefix},
     *              {@code locale}, {@code transformer}, and optionally {@code prettyPrinter}
     * @param firstItem the first item in the option source, used for auto-detecting the pretty-printer
     * @return a configured {@link PrettyPrinterProperties} instance
     */
    private static PrettyPrinterProperties initializePrettyPrinterProperties(Map args, Object firstItem) {
        PrettyPrinterProperties result = new PrettyPrinterProperties()
        result.textPrefix = args.textPrefix
        result.renderTextPrefix = args.renderTextPrefix
        result.locale = args.locale as Locale

        // We set the 'transformer' property to PrettyPrint the options
        if (args.transformer) result.transformer = args.transformer

        if (args.prettyPrinter) {
            result.prettyPrinter = args.prettyPrinter

        } else if (firstItem) {
            result.prettyPrinter = firstItem.getClass()
            if (PrettyPrinter.isRegistered(result.prettyPrinter)) {
                result.renderTextPrefix = false
            }
        }

        return result
    }

    /**
     * Builds a composite key string from the specified properties of an object,
     * joining multiple key values with the given separator.
     *
     * @param obj       the source object to read key values from
     * @param keys      the list of property names to use as key parts
     * @param separator the string used to join multiple key parts
     * @return the composite key string
     */
    private static String buildKey(Object obj, List<String> keys, String separator) {
        List results = []
        for (__key__ in keys) {
            results.add(obj[__key__])
        }
        return results.join(separator)
    }

    /**
     * Returns the selected value as-is (no additional pretty-printing is applied).
     *
     * @return the raw selected value as a {@link String}
     */
    @Override
    String getPrettyValue() {
        return value
    }

    /**
     * Serialises the current selected value(s) to a JSON string.
     * A {@link Collection} value is serialised as {@link dueuno.types.Type#LIST};
     * a scalar value is serialised as {@link dueuno.types.Type#TEXT}.
     *
     * @return a JSON string representing the current selection
     */
    @Override
    String getValueAsJSON() {
        Map valueMap

        if (value in Collection) {
            valueMap = [
                    type : Type.LIST.toString(),
                    value: value.collect { it != null ? it as String : null },
            ]
        } else {
            valueMap = [
                    type : Type.TEXT.toString(),
                    value: value != null ? value as String : null,
            ]
        }

        return Elements.encodeAsJSON(valueMap)
    }

    /**
     * Serialises this control's client-side configuration to JSON, including Select2 options
     * ({@link #multiple}, {@link #searchMinInputLength}, {@link #allowClear}, {@link #autoSelect},
     * {@link #placeholder}, {@link #search}) and the localised UI strings for the search widget.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this control's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                multiple            : multiple,
                searchMinInputLength: searchMinInputLength,
                allowClear          : allowClear,
                autoSelect          : autoSelect,
                placeholder         : placeholder,
                search              : search,
                text                : [
                        inputTooShort: message('control.select.inputTooShort'),
                        errorLoading : message('control.select.errorLoading'),
                        noResults    : message('control.select.noResults'),
                        searching    : message('control.select.searching'),
                ]
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }
}