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

import dueuno.core.PrettyPrinterProperties
import dueuno.elements.Component
import dueuno.elements.Control
import dueuno.types.Type
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class Checkbox extends Control {

    String text
    List textArgs
    Boolean simple
    Object optionKey
    Object optionValue

    Checkbox(Map args) {
        super(args)

        valueType = Type.BOOL

        textArgs = args.textArgs as List ?: []
        text = args.text == null ? buildLabel(id + ".text", new PrettyPrinterProperties(textArgs: textArgs)) : args.text
        simple = (args.simple == null) ? false : args.simple
        if (args.option) {
            setOption(args.option as Map ?: [:])
        } else {
            optionKey = args.optionKey ?: ''
            optionValue = args.optionValue ?: ''
        }

        containerSpecs.label = args.label ?: ''
        containerSpecs.nullable = true
    }

    Boolean getChecked() {
        if (!value) return false
        if (value == true) return true
        if (optionKey && (value == optionKey)) return true
        return false
    }

    void setOption(Map option) {
        option.find {key, value ->
            optionKey = key
            optionValue = value
            return true
        }
    }

    Map getOption() {
        Map option = [:]
        option.put(optionKey, optionValue)
        return option
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
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                simple: simple,
                toggle: 'toggle',
                option: optionKey,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }
}
