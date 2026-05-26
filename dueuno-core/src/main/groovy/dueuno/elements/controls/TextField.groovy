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

import dueuno.elements.Component
import dueuno.elements.Control
import dueuno.elements.Elements
import dueuno.elements.components.Button
import dueuno.elements.style.TextTransform
import dueuno.types.Type
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class TextField extends Control {

    TextFieldInputType inputType
    TextFieldInputMode inputMode

    Button actions
    String icon
    String prefix

    Integer maxSize
    String placeholder
    Boolean autocomplete
    Boolean autoSelect
    TextTransform textTransform

    Boolean onChangeAsync
    //Integer onChangeMinChars // Maybe in the future

    TextField(Map args) {
        super(args)

        valueType = Type.TEXT
        inputType = args.inputType as TextFieldInputType ?: TextFieldInputType.TEXT
        inputMode = args.inputMode as TextFieldInputMode ?: TextFieldInputMode.TEXT

        icon = args.icon ?: ''
        prefix = args.prefix ?: ''
        maxSize = args.maxSize as Integer ?: 0
        placeholder = args.placeholder == null ? '' : args.placeholder
        autocomplete = args.autocomplete == null ? false : args.autocomplete
        autoSelect = args.autoSelect == null ? true : args.autoSelect
        textTransform = args.textTransform as TextTransform ?: TextTransform.NONE
        prettyPrinterProperties.renderTextPrefix = args.renderTextPrefix == null ? false : args.renderTextPrefix

        onChangeAsync = args.onChangeAsync == null ? false : args.onChangeAsync
        //onChangeMinChars = args.onChangeMinChars ?: 0 // forse in futuro

        actions = createControl(
                class: Button,
                id: 'actions',
                group: true,
                dontCreateDefaultAction: true,
        )
    }

    String getInputType() {
        return inputType.toString().toLowerCase()
    }

    String getInputMode() {
        return inputMode.toString().toLowerCase()
    }

    Control addAction(Map args) {
        args.loading = args.loading != null ? args.loading : false
        actions.addAction(args)
        return this
    }

    void removeAction(Map args) {
        actions.removeAction(args)
    }

    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                autocomplete: autocomplete,
                autoSelect: autoSelect,
                textTransform: textTransform as String,
                onChangeAsync: onChangeAsync,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    @Override
    String getValueAsJSON() {
        Map valueMap = [
                type: valueType,
                value: prettyPrint(value, prettyPrinterProperties),
        ]

        return Elements.encodeAsJSON(valueMap)
    }
}
