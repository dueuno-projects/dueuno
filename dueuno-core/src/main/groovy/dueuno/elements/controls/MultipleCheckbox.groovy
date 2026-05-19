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

import dueuno.elements.Control
import dueuno.exceptions.ElementsException
import dueuno.types.Type
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class MultipleCheckbox extends Control {

    List<Map<String, String>> options
    Boolean simple

    Map<String, Checkbox> checkboxes

    MultipleCheckbox(Map args) {
        super(args)

        valueType = Type.LIST

        simple = args.simple == null ? false : args.simple
        prettyPrinterProperties.textPrefix = args.textPrefix ?: controllerName

        if (args.optionsFromRecordset) {
            options = Select.optionsFromRecordset(
                    recordset: args.optionsFromRecordset,
                    keys: args.keys,
                    keysSeparator: args.keysSeparator,
                    forEachOption: args.forEachOption,
                    textPrefix: prettyPrinterProperties.textPrefix,
                    renderTextPrefix: false,
                    locale: locale,
            )

        } else if (args.optionsFromList) {
            options = Select.optionsFromList(
                    list: args.optionsFromList,
                    exclude: args.exclude,
                    forEachOption: args.forEachOption,
                    textPrefix: prettyPrinterProperties.textPrefix,
                    locale: locale,
            )

        } else if (args.optionsFromEnum) {
            options = Select.optionsFromEnum(
                    enum: args.optionsFromEnum,
                    exclude: args.exclude,
                    forEachOption: args.forEachOption,
                    textPrefix: prettyPrinterProperties.textPrefix,
                    locale: locale,
            )

        } else {
            options = Select.options(
                    options: args.options,
                    forEachOption: args.forEachOption,
                    textPrefix: prettyPrinterProperties.textPrefix,
                    locale: locale,
            )
        }

        checkboxes = [:]
        for (option in options) {
            String id = option.id
            Object text = option.text

            Checkbox checkbox = new Checkbox(
                    id: getId() + '.' + id,
                    optionKey: id,
                    optionValue: text,
                    simple: simple,
                    readonly: readonly,
                    primaryTextColor: primaryTextColor,
                    primaryBackgroundColor: primaryBackgroundColor,
                    primaryBackgroundColorAlpha: primaryBackgroundColorAlpha,
            )
            checkboxes.put(id, checkbox)
        }

        containerSpecs.nullable = true
    }

    @Override
    void setValue(Object value) {
        if (value == null) {
            return
        }

        switch (value) {
            case String:
                super.setValue([value])
                break

            case Set:
            case List:
                List listValue = value.collect { it.hasProperty('id') != null ? it['id'] as String : it as String } as List
                super.setValue(listValue)
                break

            default:
                throw new ElementsException("${this.getClass().simpleName} only accepts String, Set o List as value.")
        }

        renderValue()
    }

    void renderValue() {
        if (!checkboxes)
            return

        for (checkboxEntry in checkboxes) {
            Checkbox checkbox = checkboxEntry.value
            checkbox.value = false
        }

        for (item in value) {
            checkboxes[item as String]?.value = true
        }
    }

    @Override
    void setReadonly(Boolean isReadonly) {
        for (checkboxEntry in checkboxes) {
            Checkbox checkbox = checkboxEntry.value
            checkbox.readonly = isReadonly
        }
    }

    void setSimple(Boolean isSimple) {
        simple = isSimple
        for (checkboxEntry in checkboxes) {
            Checkbox checkbox = checkboxEntry.value
            checkbox.simple = isSimple
        }
    }

}
