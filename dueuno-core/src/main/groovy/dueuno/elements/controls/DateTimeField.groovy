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
import dueuno.types.Type
import groovy.transform.CompileStatic

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * A date-and-time input control backed by {@link LocalDateTime}.
 * <p>
 * Serves as the base class for {@link DateField} and {@link TimeField}. The value type
 * defaults to {@link dueuno.types.Type#DATETIME} and can be overridden by subclasses.
 * Optional {@link #min} and {@link #max} boundaries accept {@link LocalDateTime},
 * {@link LocalDate}, or {@link LocalTime} and are normalised to {@link LocalDateTime}
 * internally. An input validation regex pattern is applied client-side to restrict entry
 * to valid date/time characters.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class DateTimeField extends Control {

    /** Minimum selectable date/time (inclusive); {@code null} means no lower bound. */
    LocalDateTime min

    /** Maximum selectable date/time (inclusive); {@code null} means no upper bound. */
    LocalDateTime max

    /** Step size in minutes for the time picker; {@code null} uses the picker's default. */
    Integer timeStep

    /**
     * Whether the field is automatically populated with a default value (e.g. "now")
     * when the form is rendered with no pre-set value.
     */
    Boolean autoPopulate

    /**
     * Creates a {@code DateTimeField} instance configured from the supplied argument map.
     * Normalises {@code min} and {@code max} to {@link LocalDateTime} regardless of whether
     * a {@link LocalDate}, {@link LocalTime}, or {@link LocalDateTime} is supplied.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code min} ({@link LocalDateTime}/{@link LocalDate}/{@link LocalTime}),
     *             {@code max} ({@link LocalDateTime}/{@link LocalDate}/{@link LocalTime}),
     *             {@code timeStep} ({@link Integer}),
     *             {@code autoPopulate} ({@link Boolean}, default {@code false}),
     *             plus all keys accepted by {@link Control#Control(Map)}
     */
    DateTimeField(Map args) {
        super(args)

        valueType = Type.DATETIME

        addContainerAttribute('id', id)
        addContainerAttribute('data-td-target-input', 'nearest')
        addContainerAttribute('data-td-target-toggle', 'nearest')

        if (args.min in LocalTime) {
            min = LocalDateTime.of(LocalDate.of(1900, 1, 1), args.min as LocalTime)
        } else if (args.min in LocalDate) {
            min = LocalDateTime.of(args.min as LocalDate, LocalTime.of(0, 0))
        } else if (args.min in LocalDateTime) {
            min = args.min as LocalDateTime
        }

        if (args.max in LocalTime) {
            max = LocalDateTime.of(LocalDate.of(1900, 1, 1), args.max as LocalTime)
        } else if (args.max in LocalDate) {
            max = LocalDateTime.of(args.max as LocalDate, LocalTime.of(0, 0))
        } else if (args.max in LocalDateTime) {
            max = args.max as LocalDateTime
        }

        timeStep = args.timeStep as Integer
        autoPopulate = args.autoPopulate as Boolean ?: false
    }

    /**
     * Registers {@code enter} and {@code change} event listeners that submit the form
     * when the user confirms a date/time value, if those handlers have not already been set.
     *
     * @param args event configuration forwarded to {@link dueuno.elements.Component#on(Map)}
     * @return this component for chaining
     */
    @Override
    Component onSubmit(Map args) {
        String submitEvent = 'enter'
        if (!hasEvent(submitEvent)) {
            args.event = submitEvent
            on(args)
        }

        submitEvent = 'change'
        if (!hasEvent(submitEvent)) {
            args.event = submitEvent
            on(args)
        }

        return this
    }

    /**
     * Serialises this field's properties to JSON, adding {@link #min}, {@link #max},
     * the input validation pattern, {@link #timeStep}, and {@link #autoPopulate}.
     * The default pattern ({@code ^[0-9/: ]*$}) can be overridden by passing a
     * {@code pattern} key in {@code properties}.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this field's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                min         : min?.toString(),
                max         : max?.toString(),
                pattern     : properties.pattern ?: '^[0-9/: ]*$',
                timeStep: timeStep,
                autoPopulate: autoPopulate,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }
}
