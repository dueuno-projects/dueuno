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

import dueuno.elements.Component
import dueuno.exceptions.ElementsException
import groovy.transform.CompileStatic

/**
 * A layout wrapper that pairs a single {@link Component} (typically a {@link dueuno.elements.Control})
 * with its associated label, help text, and Bootstrap grid-column configuration.
 * <p>
 * {@code FormField} instances are created internally by {@link Form#addField(Map)} and are
 * not usually constructed directly. The wrapped {@link #component} is accessible for direct
 * manipulation; its display width is governed by {@link #cols}/{@link #colsSmall} and validated
 * against {@link #acceptedCols}.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class FormField extends Component {

    /** The control or component wrapped by this field. */
    Component component

    /** The i18n label key or literal label text displayed above/beside the control. */
    String label

    /** Interpolation arguments for the {@link #label} message key. */
    List labelArgs

    /** The i18n help-text key or literal help string shown below the control. */
    String help

    /** Interpolation arguments for the {@link #help} message key. */
    List helpArgs

    /** Whether the help text is rendered in a collapsed (hidden) state by default. */
    Boolean helpCollapsed

    /** Whether the field accepts a {@code null} / empty value (driven by GORM {@code nullable} constraint). */
    Boolean nullable

    /** Whether the label is rendered. Set to {@code false} to suppress the label. */
    Boolean displayLabel

    /** Whether the field is visually highlighted (e.g. on validation error). */
    Boolean highlight

    /** Whether the control spans multiple lines (affects the accepted column range). */
    Boolean multiline

    /** The list of Bootstrap column widths (1–12) accepted by this field for medium+ screens. */
    List acceptedCols

    /** The list of accepted row heights; empty means any integer value is accepted. */
    List acceptedRows

    /** Bootstrap column span for medium and larger screens (sm breakpoint). */
    Integer cols

    /** Bootstrap column span for small (xs) screens. */
    Integer colsSmall

    /** Number of visible text rows for multiline controls. */
    Integer rows

    /**
     * Creates a {@code FormField} instance configured from the supplied argument map.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code component} ({@link Component}) — the wrapped control (required),
     *             {@code label} ({@link String}), {@code labelArgs} ({@link List}),
     *             {@code help} ({@link String}), {@code helpArgs} ({@link List}),
     *             {@code helpCollapsed} ({@link Boolean}, default {@code false}),
     *             {@code nullable} ({@link Boolean}, default {@code true}),
     *             {@code displayLabel} ({@link Boolean}, default {@code true}),
     *             {@code highlight} ({@link Boolean}, default {@code false}),
     *             {@code multiline} ({@link Boolean}, default {@code false}),
     *             {@code cols} ({@link Integer}, default {@code 12}),
     *             {@code colsSmall} ({@link Integer}, default {@code 12}),
     *             {@code rows} ({@link Integer}, default {@code 3}),
     *             {@code acceptedCols} ({@link List}), {@code acceptedRows} ({@link List}),
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    FormField(Map args) {
        super(args)

        component = args.component as Component

        label = args.label
        labelArgs = args.labelArgs as List ?: []
        help = args.help
        helpArgs = args.helpArgs as List ?: []
        helpCollapsed = args.helpCollapsed == null ? false : args.helpCollapsed

        nullable = args.nullable == null ? true : args.nullable
        displayLabel = args.displayLabel == null ? true : args.displayLabel
        highlight = args.highlight == null ? false : args.highlight
        multiline = args.multiline == null ? false : args.multiline

        setAcceptedRows(args.acceptedRows == null ? [] : args.acceptedRows as List)
        setAcceptedCols(args.acceptedCols == null ? [] : args.acceptedCols as List)
        setCols(args.cols == null ? 12 : args.cols as Integer, args.colsSmall == null ? 12 : args.colsSmall as Integer)
        setRows(args.rows == null ? 3 : args.rows as Integer)
    }

    /**
     * Sets the accepted Bootstrap column widths for this field.
     * When {@code accepted} is empty, defaults to {@code [3–12]} for multiline controls
     * and {@code [1–12]} for single-line controls.
     *
     * @param accepted explicit list of allowed column values; pass an empty list to use defaults
     */
    void setAcceptedCols(List accepted) {
        if (accepted) {
            acceptedCols = accepted
        } else {
            if (multiline == true) {
                acceptedCols = [3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
            } else {
                acceptedCols = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
            }
        }
    }

    /**
     * Sets the accepted row heights for this field.
     * Pass an empty list to allow any integer value.
     *
     * @param accepted explicit list of allowed row values
     */
    void setAcceptedRows(List accepted) {
        acceptedRows = accepted
    }

    /**
     * Sets the Bootstrap column span for this field, validating both values against
     * {@link #acceptedCols}.
     *
     * @param columns      the column span for medium and larger screens (sm breakpoint)
     * @param columnsSmall the column span for small (xs) screens
     * @throws dueuno.exceptions.ElementsException if either value is not in {@link #acceptedCols}
     */
    void setCols(Integer columns, Integer columnsSmall) {
        if (columns in acceptedCols && columnsSmall in acceptedCols) {
            cols = columns
            colsSmall = columnsSmall
        } else {
            throw new ElementsException("The '${component.getClass().simpleName}' control only accepts one of the following values for 'cols': " + acceptedCols.join(', '))
        }
    }

    /**
     * Sets the number of visible text rows, validating the value against {@link #acceptedRows}
     * when that list is non-empty.
     *
     * @param lines the number of rows
     * @throws dueuno.exceptions.ElementsException if {@code lines} is not in {@link #acceptedRows}
     */
    void setRows(Integer lines) {
        if (acceptedRows) {
            if (lines in acceptedRows) {
                rows = lines
            } else {
                throw new ElementsException("The '${component.getClass().simpleName}' control only accepts one of the following values for 'rows': " + acceptedRows.join(', '))
            }
        } else {
            rows = lines
        }
    }

    /**
     * Returns the Bootstrap CSS column classes for this field (e.g. {@code " col-sm-6 col-4"}).
     * The {@code col-} (xs) class is omitted when {@link #colsSmall} is {@code 12}.
     *
     * @return the CSS column class string
     */
    String getCols() {
        String colClasses = ' col-sm-' + cols
        if (colsSmall != 12) colClasses += ' col-' + colsSmall
        return colClasses
    }

    /**
     * Returns an inline CSS {@code height} style for multiline fields with more than one row.
     * Returns an empty string for single-line fields or when {@link #rows} is {@code ≤ 1}.
     *
     * @return the inline {@code height} style string, or an empty string
     */
    String getRows() {
        if (multiline == false || rows <= 1)
            return ''

        return " height: calc(1rem * 3 * ${rows});"
    }
}
