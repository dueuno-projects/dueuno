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
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class Select extends Control {

    List<Map<String, String>> options
    Closure forEachOption

    Button actions

    String placeholder
    Boolean allowClear
    Boolean autoSelect // Auto selects if not nullable and the control has only one option
    Boolean multiple
    Boolean search

    Integer searchMinInputLength

    Select(Map args) {
        super(args)

        autoSelect = (args.autoSelect == null) ? true : args.autoSelect
        multiple = (args.multiple == null) ? false : args.multiple
        forEachOption = args.forEachOption as Closure ?: null
        placeholder = args.placeholder ? message(args.placeholder as String) : message('control.select.placeholder')

        setTextStyle(args.textStyle)

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

    void setMultiple(Boolean value) {
        this.multiple = (value == null) ? false : value
        if (multiple) allowClear = false
    }

    Map getOptions() {
        return options.collectEntries { [(it.id): it.text]}
    }

    @Override
    Component onSubmit(Map args) {
        String submitEvent = 'change'
        if (!hasEvent(submitEvent)) {
            args.event = submitEvent
            on(args)
        }

        return this
    }

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

    Control addAction(Map args) {
        args.loading = args.loading != null ? args.loading : false
        actions.addAction(args)
        return this
    }

    void removeAction(Map args) {
        actions.removeAction(args)
    }

    //
    // Utils
    //
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

    @CompileDynamic
    static List<Map<String, String>> optionsFromEnum(Map args) {
        args.list = args.enum?.values()
        return optionsFromList(args)
    }

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

    private static String buildKey(Object obj, List<String> keys, String separator) {
        List results = []
        for (__key__ in keys) {
            results.add(obj[__key__])
        }
        return results.join(separator)
    }

    @Override
    String getPrettyValue() {
        return value
    }

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