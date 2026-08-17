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

import dueuno.elements.ElementsException
import dueuno.elements.core.Control
import dueuno.types.Type
import groovy.transform.CompileStatic

/**
 * A control that renders a group of {@link Checkbox} instances from a list of options,
 * storing the selected keys as a {@link List}.
 * <p>
 * Options can be supplied in four ways (checked in order):
 * </p>
 * <ul>
 *   <li>{@code optionsFromRecordset} — built from a GORM/collection result set.</li>
 *   <li>{@code optionsFromList} — built from a plain list of values.</li>
 *   <li>{@code optionsFromEnum} — built from an enum class.</li>
 *   <li>{@code options} — a pre-built list of {@code [id: …, text: …]} maps.</li>
 * </ul>
 * <p>
 * The value type is {@link dueuno.types.Type#LIST}. Each individual checkbox is accessible
 * via the {@link #checkboxes} map, keyed by option ID.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class MultipleCheckbox extends Control {

    /** The resolved list of option maps ({@code id} → key, {@code text} → display label). */
    List<Map<String, String>> options

    /** When {@code true}, all child checkboxes render without the toggle-switch style. */
    Boolean simple

    /** Map of option ID → {@link Checkbox} instance for each rendered checkbox. */
    Map<String, Checkbox> checkboxes

    /** Property names used to build recordset option keys. */
    List<String> keys

    /** Separator used when a recordset option key contains multiple properties. */
    String keysSeparator

    /** Values excluded when options are built from a list or enum. */
    List exclude

    /** Closure invoked once for every option while the option list is built. */
    Closure forEachOption

    /**
     * Creates a {@code MultipleCheckbox} instance configured from the supplied argument map.
     * Resolves options from one of {@code optionsFromRecordset}, {@code optionsFromList},
     * {@code optionsFromEnum}, or {@code options}, then creates a {@link Checkbox} for each.
     *
     * @param args initialisation arguments; recognised keys include:
     * {@code simple} ({@link Boolean}, default {@code false}),
     * {@code textPrefix} ({@link String}, default: controller name),
     * {@code optionsFromRecordset}, {@code optionsFromList}, {@code optionsFromEnum},
     * {@code options}, {@code keys}, {@code keysSeparator}, {@code exclude},
     * {@code forEachOption} ({@link Closure}),
     *             plus all keys accepted by {@link Control#Control(Map)}
     */
    MultipleCheckbox(Map args) {
        super(args)

        valueType = Type.LIST

        simple = args.simple == null ? false : args.simple
        prettyPrinterProperties.textPrefix = args.textPrefix ?: controllerName
        keys = args.keys as List<String> ?: []
        keysSeparator = args.keysSeparator ?: ','
        exclude = args.exclude as List ?: []
        forEachOption = args.forEachOption as Closure

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
                id: getId() + '_' + id,
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

    /**
     * Sets the selected values for this control.
     * <ul>
     *   <li>A {@link String} is wrapped in a single-element list.</li>
     *   <li>A {@link Set} or {@link List} is normalised to a list of ID strings (using the
     *       element's {@code id} property when present, otherwise its string representation).</li>
     * </ul>
     * After setting the value, {@link #renderValue()} is called to update the individual
     * checkbox states.
     *
     * @param value the selected option key(s); accepts {@code null}, {@link String},
     * {@link Set}, or {@link List}
     * @throws ElementsException if {@code value} is of an unsupported type
     */
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

    /**
     * Synchronises the checked state of each {@link Checkbox} in {@link #checkboxes} with
     * the current {@link #value} list. Clears all checkboxes first, then marks those whose
     * option key appears in the value list as checked.
     */
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

    /**
     * Propagates the read-only state to all individual {@link Checkbox} instances.
     *
     * @param isReadonly {@code true} to make all checkboxes read-only
     */
    @Override
    void setReadonly(Boolean isReadonly) {
        super.setReadonly(isReadonly)
        for (checkboxEntry in checkboxes) {
            Checkbox checkbox = checkboxEntry.value
            checkbox.readonly = isReadonly
        }
    }

    /**
     * Sets the simple rendering mode on this control and propagates it to all individual
     * {@link Checkbox} instances.
     *
     * @param isSimple {@code true} to render all checkboxes without the toggle-switch style
     */
    void setSimple(Boolean isSimple) {
        simple = isSimple
        for (checkboxEntry in checkboxes) {
            Checkbox checkbox = checkboxEntry.value
            checkbox.simple = isSimple
        }
    }

    void setOptions(Map value) {
        applyOptions(Select.options(optionConfiguration(options: value)))
    }

    Map getOptions() {
        return options.collectEntries { [(it.id): it.text] }
    }

    void setOptionsFromList(List value) {
        applyOptions(Select.optionsFromList(optionConfiguration(list: value, exclude: exclude)))
    }

    void setOptionsFromEnum(Class value) {
        applyOptions(Select.optionsFromEnum(optionConfiguration(enum: value, exclude: exclude)))
    }

    void setOptionsFromRecordset(Collection value) {
        applyOptions(Select.optionsFromRecordset(optionConfiguration(
            recordset: value,
            keys: keys,
            keysSeparator: keysSeparator,
            renderTextPrefix: false,
        )))
    }

    private Map optionConfiguration(Map source) {
        return [
            forEachOption   : forEachOption,
            textPrefix      : textPrefix,
            renderTextPrefix: renderTextPrefix == null ? true : renderTextPrefix,
            locale          : locale,
        ] + source
    }

    private void applyOptions(List<Map<String, String>> value) {
        this.@options = value ?: []
        checkboxes = [:]
        for (option in options) {
            String optionId = option.id
            Checkbox checkbox = new Checkbox(
                id: getId() + '_' + optionId,
                optionKey: optionId,
                optionValue: option.text,
                simple: simple,
                readonly: readonly,
                primaryTextColor: primaryTextColor,
                primaryBackgroundColor: primaryBackgroundColor,
                primaryBackgroundColorAlpha: primaryBackgroundColorAlpha,
            )
            checkboxes.put(optionId, checkbox)
        }
        renderValue()
    }

}
