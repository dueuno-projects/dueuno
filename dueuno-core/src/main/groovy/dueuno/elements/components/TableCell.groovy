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
import dueuno.core.PrettyPrinterProperties
import dueuno.elements.Component
import dueuno.elements.controls.HiddenField
import dueuno.elements.style.TextAlign
import dueuno.elements.style.TextStyle
import dueuno.elements.style.TextWrap
import dueuno.elements.style.VerticalAlign
import groovy.contracts.Requires
import groovy.transform.CompileStatic

/**
 * A single cell within a {@link TableRow}, bound to a named {@link Table} column.
 * <p>
 * By default the cell renders its value through an internal {@link Label}. An arbitrary
 * {@link Component} can replace the label via {@link #setComponent(Component)} or
 * {@link #setComponent(Map)}. Convenience setters delegate to the internal label when present.
 * An optional hidden {@link dueuno.elements.controls.HiddenField} can be added to submit an
 * extra value alongside the row via {@link #setSubmitValue(Object)}.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class TableCell extends Component {

    /** The {@link Table} this cell belongs to. */
    Table table

    /** The name of the {@link Table} column this cell is bound to. */
    String column

    /** The {@link TableRow} this cell belongs to. */
    TableRow row

    /** Number of columns this cell spans; {@code 0} means no spanning. */
    Integer colspan

    /** Number of rows this cell spans; {@code 0} means no spanning. */
    Integer rowspan

    /** Horizontal alignment of the cell content. Defaults to {@link TextAlign#DEFAULT}. */
    TextAlign textAlign

    /** Vertical alignment of the cell content. Defaults to {@link VerticalAlign#DEFAULT}. */
    VerticalAlign verticalAlign

    /** The component ID of the inner content component (label or custom component). */
    String componentId

    /**
     * Creates a {@code TableCell} instance configured from the supplied argument map.
     * If {@code args.component} is provided it replaces the default {@link Label};
     * otherwise a {@link Label} is created automatically.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code table} ({@link Table}, required),
     *             {@code row} ({@link TableRow}, required),
     *             {@code column} ({@link String}, required),
     *             {@code component} ({@link Component}),
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    @Requires({ args.table && args.row && args.column })
    TableCell(Map args) {
        super(args)

        table = args.table as Table
        row = args.row as TableRow
        column = args.column

        colspan = 0
        rowspan = 0
        textAlign = TextAlign.DEFAULT
        verticalAlign = VerticalAlign.DEFAULT

        componentId = getId() + '-component'

        buildCellComponent(args.component as Component)
    }

    /**
     * Serialises this cell's properties to JSON, including the bound {@link #column} name.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this cell's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                column: column,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    /**
     * Initialises the inner content component: registers the supplied {@link Component} if
     * non-null, or creates a default {@link Label} otherwise.
     *
     * @param component a custom component to embed, or {@code null} to use a {@link Label}
     */
    private void buildCellComponent(Component component) {
        if (component) {
            setComponent(component)
        } else {
            setLabel()
        }
    }

    /**
     * Creates and registers the default {@link Label} as the inner content component.
     */
    private void setLabel() {
        addComponent(
                class: Label,
                id: componentId,
                replace: true,
                textPrefix: controllerName,
                textWrap: TextWrap.NO_WRAP,
                tag: false,
        )
    }

    /**
     * Returns the inner {@link Label} if the current content component is a {@link Label},
     * or {@code null} if a custom component has replaced it.
     *
     * @return the inner {@link Label}, or {@code null}
     */
    Label getLabel() {
        Component component = getComponent()
        if (component in Label) {
            return component as Label
        } else {
            return null
        }
    }

    /**
     * Replaces the default label with the given {@link Component} instance.
     * Flags the parent table and row as containing components (switching the row template).
     *
     * @param component the component to embed in this cell
     */
    void setComponent(Component component) {
        if (!row.isHeader) {
            table.hasComponents = true
            row.viewTemplate = 'TableRowComponent'
        }

        component.id = componentId
        addComponent(component)
    }

    /**
     * Replaces the default label with a component built from the given argument map.
     * Flags the parent table and row as containing components (switching the row template).
     *
     * @param args component configuration arguments; {@code id} defaults to {@link #componentId}
     */
    void setComponent(Map args) {
        if (!row.isHeader) {
            table.hasComponents = true
            row.viewTemplate = 'TableRowComponent'
        }

        if (args.id) {
            componentId = args.id
        } else {
            args.id = componentId
        }
        args.replace = true
        addComponent(args)
    }

    /**
     * Returns the current inner content component (label or custom component).
     *
     * @return the inner {@link Component}
     */
    Component getComponent() {
        return getComponent(componentId)
    }

    /**
     * Adds a hidden {@link dueuno.elements.controls.HiddenField} to this cell so that
     * {@code value} is submitted alongside the row data.
     *
     * @param value the value to submit
     */
    void setSubmitValue(Object value) {
        addComponent(
                class: HiddenField,
                id: getId() + '-value',
                value: value,
                replace: true,
        )
    }

    /**
     * Returns the hidden submit-value component added via {@link #setSubmitValue(Object)},
     * or {@code null} if none was added.
     *
     * @return the hidden submit-value {@link Component}, or {@code null}
     */
    Component getSubmitValue() {
        return getComponent(getId() + '-value')
    }

    /**
     * Applies the given pretty-printer properties to the inner {@link Label}.
     * Has no effect if the cell contains a custom component.
     *
     * @param value a map of {@link dueuno.core.PrettyPrinterProperties} settings
     */
    void setPrettyPrinterProperties(Map value) {
        Label label = getLabel()
        if (label) {
            label.prettyPrinterProperties.set(value)
        }
    }

    /**
     * Returns the {@link dueuno.core.PrettyPrinterProperties} of the inner {@link Label},
     * or a default instance if the cell contains a custom component.
     *
     * @return the label's pretty-printer properties
     */
    PrettyPrinterProperties getPrettyPrinterProperties() {
        Label label = getLabel()
        if (label) {
            return label.prettyPrinterProperties
        } else {
            return new PrettyPrinterProperties()
        }
    }

    /**
     * Sets the text-wrap mode on the inner {@link Label}.
     *
     * @param value the desired {@link TextWrap} mode
     */
    void setTextWrap(TextWrap value) {
        Label label = getLabel()
        if (label) {
            label.textWrap = value
        }
    }

    /**
     * Sets a single text style on the inner {@link Label}.
     *
     * @param value the {@link TextStyle} to apply
     */
    void setTextStyle(TextStyle value) {
        Label label = getLabel()
        if (label) {
            label.setTextStyle(value)
        }
    }

    /**
     * Sets multiple text styles on the inner {@link Label}.
     *
     * @param value the list of {@link TextStyle} values to apply
     */
    void setTextStyle(List<TextStyle> value) {
        Label label = getLabel()
        if (label) {
            label.setTextStyle(value)
        }
    }

    /**
     * Sets the display text on the inner {@link Label}.
     *
     * @param value the text value or i18n key to display
     */
    void setText(Object value) {
        Label label = getLabel()
        if (label) {
            label.text = value
        }
    }

    /**
     * Sets the icon class on the inner {@link Label}.
     *
     * @param value the icon CSS class (e.g. {@code "fa-star"})
     */
    void setIcon(String value) {
        Label label = getLabel()
        if (label) {
            label.icon = value
        }
    }

    /**
     * Sets the tooltip on the inner {@link Label}.
     *
     * @param value the tooltip text
     */
    void setTooltip(String value) {
        Label label = getLabel()
        if (label) {
            label.tooltip = value
        }
    }

    /**
     * Enables or disables the tag (badge) rendering mode on the inner {@link Label}.
     * When enabled, centres the cell content and applies an appropriate background colour
     * that respects the table's row-striping setting.
     *
     * @param value {@code true} to render the cell value as a tag/badge
     */
    void setTag(Boolean value) {
        Label label = getLabel()
        Object labelValue = ObjectUtils.getValue(row.values, column)

        if (label && (labelValue != null || label.icon)) {
            String backgroundColor = table.rowStriped
                    ? (row.index % 2 == 0 ? mainForegroundColor : mainBackgroundColor)
                    : mainBackgroundColor

            textAlign = TextAlign.CENTER
            label.tag = value
            label.backgroundColor = value
                    ? backgroundColor
                    : null
        }
    }

    /**
     * Sets a hyperlink URL on the inner {@link Label}.
     *
     * @param value the URL string
     */
    void setUrl(String value) {
        Label label = getLabel()
        if (label) {
            label.url = value
        }
    }

    /**
     * Sets raw HTML content on the inner {@link Label}.
     *
     * @param value the HTML string to render
     */
    void setHtml(String value) {
        Label label = getLabel()
        if (label) {
            label.html = value
        }
    }

    /**
     * Returns {@code true} if this cell is covered by a {@code colspan} from a preceding
     * cell in the same row, meaning it should not be rendered independently.
     *
     * @return {@code true} if this cell falls within another cell's column span
     */
    Boolean isColumnSpanned() {
        Integer span = 0
        table.columns.find { String col ->
            span--
            if (col == column) return true
            TableCell cell = row.cells[col]
            if (cell.colspan > 1) {
                span = cell.colspan
            }
            return false
        }
        return span > 0
    }

}
