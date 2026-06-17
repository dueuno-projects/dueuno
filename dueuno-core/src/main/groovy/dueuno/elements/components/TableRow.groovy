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
import dueuno.core.Transformer
import dueuno.elements.Component
import dueuno.elements.Elements
import dueuno.elements.controls.Checkbox
import dueuno.elements.controls.HiddenField
import dueuno.elements.style.TextAlign
import dueuno.elements.style.TextStyle
import dueuno.elements.style.TextWrap
import dueuno.elements.style.VerticalAlign
import dueuno.types.Money
import dueuno.types.Quantity
import groovy.contracts.Requires
import groovy.transform.CompileStatic

import java.time.temporal.Temporal

/**
 * A single row within a {@link TableRowset}, containing one {@link TableCell} per table column.
 * <p>
 * The row lifecycle consists of two phases driven by {@link TableRowset}:
 * </p>
 * <ol>
 *   <li>{@link #preProcessRow()} — converts the raw record to a value map, creates all cells,
 *       processes keys, copies actions from the table, applies transformers, creates hidden
 *       submit fields, and applies pretty-printer configuration.</li>
 *   <li>{@link #postProcessRow()} — injects key params into the action button, resolves final
 *       cell values (after the user's {@code eachRow} closure may have mutated them), and sets
 *       cell alignment.</li>
 * </ol>
 * <p>
 * Each row also carries a row-level {@link Button} for per-row actions and a {@link Checkbox}
 * for row selection.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class TableRow extends Component {

    /** The {@link Table} this row belongs to. */
    Table table

    /** The {@link TableRowset} (header, body, or footer band) that owns this row. */
    TableRowset rowset

    /** Map of column name → {@link TableCell} for each cell in this row. */
    Map<String, TableCell> cells

    /** Map of field name → {@link HiddenField} for values submitted with the row. */
    Map<String, HiddenField> submit

    /** Zero-based index of this row within its rowset. */
    Integer index

    /** The raw record or value map used to populate this row's cells. */
    Object values

    /** Per-row action {@link Button} populated from the table's action definitions. */
    Button actions

    /** Row-selection {@link Checkbox}; rendered as a simple checkbox without toggle-switch style. */
    Checkbox selected

    /** {@code true} if this row is a header row. */
    Boolean isHeader

    /** {@code true} if this row is a footer row. */
    Boolean isFooter

    /** {@code true} if the row-selection checkbox should be rendered. Defaults to {@code true}. */
    Boolean hasSelection

    /** Vertical alignment applied to all cells in this row. Defaults to {@link VerticalAlign#MIDDLE}. */
    VerticalAlign verticalAlign

    /** Text styles applied to all cells in this row. */
    List<TextStyle> textStyle

    /**
     * Creates a {@code TableRow} instance configured from the supplied argument map.
     * Initialises the per-row action button and selection checkbox.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code table} ({@link Table}, required),
     *             {@code rowset} ({@link TableRowset}, required),
     *             {@code index} ({@link Integer}, required),
     *             {@code values} (record data),
     *             {@code isHeader} ({@link Boolean}, default {@code false}),
     *             {@code isFooter} ({@link Boolean}, default {@code false}),
     *             {@code hasSelection} ({@link Boolean}, default {@code true}),
     *             {@code checked} ({@link Boolean}, default {@code false}),
     *             {@code textStyle} ({@link TextStyle} or {@link List}&lt;{@link TextStyle}&gt;),
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    @Requires({ args.table && args.rowset && args.index != null })
    TableRow(Map args) {
        super(args)

        table = args.table as Table
        rowset = args.rowset as TableRowset
        index = args.index as Integer

        cells = [:]
        submit = [:]
        values = args.values ?: [:]

        isHeader = (args.isHeader == null) ? false : args.isHeader
        isFooter = (args.isFooter == null) ? false : args.isFooter
        hasSelection = (args.hasSelection == null) ? true : args.hasSelection

        verticalAlign = VerticalAlign.MIDDLE
        setTextStyle(args.textStyle)

        actions = createControl(
                class: Button,
                id: getId() + '-actions',
                dontCreateDefaultAction: true,
        )

        selected = createControl(
                class: Checkbox,
                id: getId() + '-selected',
                simple: true,
                cssClass: 'selectRow',
                checked: (args.checked == null) ? false : args.checked,
        )
    }

    /**
     * First phase of row processing: converts the raw record to a value map, creates all
     * {@link TableCell} instances, and runs the key, action, transformer, submit-value, and
     * pretty-printer processing steps.
     * Called by {@link TableRowset#setRows(Collection)} before the user's {@code eachRow} closure.
     */
    void preProcessRow() {
        selected.readonly = table.readonly
        values = Elements.toMap(values, table.columns, table.includeValues, table.excludeValues)

        createCells()

        processKeys()
        processActions()
        processTransformers()
        processSubmitValues()
        processPrettyPrinters()
    }

    /**
     * Second phase of row processing: injects key params into the action button, updates
     * hidden submit-field values, resolves final cell display values, and sets cell alignment.
     * Called by {@link TableRowset#setRows(Collection)} after the user's {@code eachRow} closure.
     */
    void postProcessRow() {
        if (!isHeader && !isFooter) {
            // Adds key columns to actions params
            Map _21Params = [
                    _21RowId: id,
            ]
            actions.addParams(_21Params + getKeys())
        }

        // Updates submit values
        for (field in submit) {
            String fieldName = field.key
            HiddenField fieldComponent = field.value
            Object fieldValue = values[fieldName]

            if (ObjectUtils.hasId(fieldValue)) {
                fieldComponent.value = fieldValue['id'].toString()

            } else {
                fieldComponent.value = fieldValue
            }
        }

        // Updates cell control values & cell stile
        for (cell in cells) {
            String columnName = cell.key
            TableCell columnCell = cell.value

            if (isHeader) {
                Label cellLabel = columnCell.component as Label
                cellLabel.text = values[columnName]

                if (devDisplayHints) {
                    if (columnName in getKeys()) {
                        cellLabel.prettyPrinterProperties.textPrefix = ''
                        cellLabel.html = '<i class="fa-solid fa-key me-1"></i><span>' + columnName + '</span>'

                    } else {
                        String prefix = cellLabel.prettyPrinterProperties.textPrefix
                        String labelValue = cellLabel.text
                        String labelCode = prefix + '.' + columnName

                        if (labelCode != labelValue) {
                            cellLabel.prettyPrinterProperties.renderTextPrefix = false
                            cellLabel.text = labelValue + ' (' + columnName + ')'
                        }
                    }
                }

            } else {
                // The user could have changed the value in the .eachRow closure that's why we set it
                // in the post-processing instead of the pre-processing
                Object finalValue = ObjectUtils.getValue(values, columnName)
                columnCell.text = finalValue == null ? '' : finalValue
                setCellAlignment(columnName, finalValue)
            }
        }
    }

    /**
     * Copies actions from the table's action {@link Button} into the row's action button,
     * unless this is a header or footer row or the table has no row actions.
     */
    private void processActions() {
        if (isHeader || isFooter) {
            return
        }

        if (table.rowActions) {
            actions.copyActionsFrom(table.actions)
        }
    }

    /**
     * Applies the table's column transformers to the corresponding values in this row.
     * Skipped for header rows.
     */
    private void processTransformers() {
        if (isHeader) {
            return
        }

        for (item in table.transformers) {
            String columnName = item.key
            String transformerName = item.value
            values[columnName] = Transformer.transform(transformerName, values[columnName])
        }
    }

    /**
     * Creates hidden {@link HiddenField} submit components for the row index, key columns,
     * and any columns listed in {@link Table#submit}. Skipped for header rows.
     */
    private void processSubmitValues() {
        if (isHeader) {
            return
        }

        values['_index_'] = index
        submit['_index_'] = createControl(
                class: HiddenField,
                id: '_index_',
                value: index,
        ) as HiddenField

        for (key in getKeys()) {
            String keyName = key.key
            HiddenField hiddenValue = createControl(
                    class: HiddenField,
                    id: keyName,
                    value: key.value,
            )
            submit[keyName] = hiddenValue
        }

        for (columnName in table.submit) {
            HiddenField hiddenValue = createControl(
                    class: HiddenField,
                    id: getId() + '-' + columnName + '-value',
                    value: values[columnName],
            )
            submit[columnName] = hiddenValue
        }
    }

    /**
     * Applies the table's per-column {@link dueuno.core.PrettyPrinterProperties} and
     * pretty-printer class overrides to each cell's inner {@link Label}. Skipped for header rows
     * and cells that contain a custom component instead of a label.
     */
    private void processPrettyPrinters() {
        if (isHeader) {
            return
        }

        for (cell in cells) {
            String columnName = cell.key
            TableCell columnCell = cell.value

            if (columnCell.component !in Label) {
                continue
            }

            Label cellLabel = columnCell.label
            if (table.prettyPrinterProperties[columnName]) {
                cellLabel.prettyPrinterProperties.set(
                        table.prettyPrinterProperties[columnName]
                )
            }

            if (table.prettyPrinters[columnName]) {
                cellLabel.prettyPrinterProperties.prettyPrinter = table.prettyPrinters[columnName]
            }
        }
    }

    /**
     * Returns the first row in this row's {@link TableRowset}.
     *
     * @return the first {@link TableRow} of the rowset
     */
    TableRow getFirst() {
        return rowset.firstRow
    }

    /**
     * Returns the last row in this row's {@link TableRowset}.
     *
     * @return the last {@link TableRow} of the rowset
     */
    TableRow getLast() {
        return rowset.lastRow
    }

    /**
     * Returns {@code true} if the rowset has a first row (i.e. contains at least one row).
     *
     * @return {@code true} when {@link TableRowset#firstRow} is non-null
     */
    Boolean isFirst() {
        return rowset.firstRow != null
    }

    /**
     * Returns {@code true} if the rowset has a last row (i.e. contains at least one row).
     *
     * @return {@code true} when {@link TableRowset#lastRow} is non-null
     */
    Boolean isLast() {
        return rowset.lastRow != null
    }

    /**
     * Sets the text styles for this row, accepting a single {@link TextStyle}, a
     * {@link List}&lt;{@link TextStyle}&gt;, or {@code null}/{@code default} (which resets to
     * {@link TextStyle#NONE}).
     *
     * @param value a {@link TextStyle}, a list of {@link TextStyle} values, or {@code null}
     */
    void setTextStyle(Object value) {
        switch (value) {
            case TextStyle:
                textStyle = [value as TextStyle]
                break

            case List<TextStyle>:
                textStyle = value as List<TextStyle>
                break

            default:
                textStyle = [TextStyle.NONE]
        }
    }

    /**
     * Returns the text styles as a space-separated CSS class string.
     *
     * @return the joined text-style class string
     */
    String getTextStyle() {
        return textStyle.join(' ')
    }

    //
    // KEYS
    //

    /**
     * Resolves the key values from the row's value map according to the table's key column list.
     * GORM object IDs are extracted as strings. Custom key columns (user-declared keys that have
     * no matching value) are back-filled from the {@code id} column to avoid conflicts when
     * passing IDs to another page.
     *
     * @return a map of key column name → resolved key value
     */
    private Map processKeys() {
        Map results = [:]
        List<String> keyColumns = table.keys

        for (keyColumn in keyColumns) {
            Object value

            try {
                value = values[keyColumn]
            } catch (Exception ignore) {
                // ignore
            }

            if (keyColumn == 'id') {
                results[keyColumn] = value

                // We copy id's value into null keyColumns (user declared keyColumns that don't match any record value)
                // These keyColumns are used when passing an id to another page to avoid "id" conflicts with the next page
                List customKeyColumns = keyColumns.findAll { values[it] == null }
                for (customKeyColumn in customKeyColumns) {
                    values[customKeyColumn] = value
                }

            } else if (value) {
                // GORM Objects handling
                if (ObjectUtils.hasId(value)) {
                    // GORM Keys must be strings otherwise GORM sometimes complains (don't really know why)
                    results[keyColumn] = value['id'].toString()

                } else {
                    results[keyColumn] = value
                }
            }
        }

        return results
    }

    /**
     * Returns a map of key column name → current value for all of the table's key columns.
     *
     * @return the key column values map
     */
    private Map getKeys() {
        Map results = [:]
        for (keyColumn in table.keys) {
            results[keyColumn] = values[keyColumn]
        }
        return results
    }

    /**
     * Returns the key column values serialised as a JSON string.
     *
     * @return JSON representation of the keys map
     */
    private String getKeysAsJSON() {
        return Elements.encodeAsJSON(getKeys())
    }

    //
    // CELLS
    //

    /**
     * Creates one {@link TableCell} for each column defined in the table, choosing the
     * appropriate cell type (sortable header, plain header, or body cell).
     */
    private void createCells() {
        cells = [:]
        for (columnName in table.columns) {
            Boolean isSortableHeader = isHeader && columnName in table.sortable
            if (isSortableHeader) {
                addCellHeaderSortable(columnName)

            } else if (isHeader) {
                addCellHeader(columnName)

            } else {
                addCell(columnName)
            }
        }
    }

    /**
     * Auto-detects and applies horizontal and vertical alignment for a cell based on the
     * type of its value, and propagates the horizontal alignment to the corresponding
     * header cells.
     *
     * @param columnName the column whose cell alignment should be set
     * @param value      the resolved cell value used to determine alignment
     */
    private void setCellAlignment(String columnName, Object value) {
        if (value == null) {
            return
        }

        TableCell cell = cells[columnName]
        if (cell.textAlign == TextAlign.DEFAULT) {
            switch (value) {
                case Boolean:
                    cell.textAlign = TextAlign.CENTER
                    break

                case Number:   // BigDecimal, Integer, Float, etc.
                case Date:
                case Temporal: // LocalDate, LocalTime, LocalDateTime, etc.
                case Money:
                case Quantity:
                    if (cell.textAlign == TextAlign.DEFAULT) {
                        cell.textAlign = TextAlign.CENTER
                    }
                    break

                default:
                    cell.textAlign = TextAlign.START
            }
        }

        if (cell.verticalAlign == VerticalAlign.DEFAULT) {
            cell.verticalAlign = verticalAlign
        }

        // Set header alignment
        for (row in table.header.rows) {
            row.cells[columnName].textAlign = cell.textAlign
        }
    }

    /**
     * Creates a plain body (or custom-component) {@link TableCell} for the given column and
     * registers it in {@link #cells}.
     *
     * @param columnName the column name to create a cell for
     * @param component  an optional custom {@link Component} to embed; uses a {@link Label} when {@code null}
     * @return the newly created {@link TableCell}
     */
    private TableCell addCell(String columnName, Component component = null) {
        String cellName = getId() + '-' + columnName
        TableCell cell = createComponent(
                class: TableCell,
                id: cellName,
                table: table,
                column: columnName,
                row: this,
                component: component,
        )

        cells.put(columnName, cell)
        return cell
    }

    /**
     * Creates a non-sortable header {@link TableCell} containing a bold {@link Label}
     * for the given column.
     *
     * @param columnName the column name to create a header cell for
     * @return the newly created header {@link TableCell}
     */
    private TableCell addCellHeader(String columnName) {
        Label header = createComponent(
                class: Label,
                id: columnName,
                action: actionName,
                textPrefix: controllerName,
                renderTextPrefix: isHeader && !table.labels[columnName],
                textWrap: TextWrap.NO_WRAP,
                textStyle: TextStyle.BOLD,
                tag: false,
        )

        return addCell(columnName, header)
    }

    /**
     * Creates a sortable header {@link TableCell} containing a {@link Link} that toggles the
     * sort direction for the given column when clicked.
     *
     * @param columnName the column name to create a sortable header cell for
     * @return the newly created sortable header {@link TableCell}
     */
    private TableCell addCellHeaderSortable(String columnName) {
        String order = table.sort[columnName] == 'asc' ? 'desc' : 'asc'
        Link sortableHeader = createComponent(
                class: Link,
                id: columnName,
                action: actionName,
                params: table.submitParams + (Map) [
                        _21Table    : table.id,
                        _21TableSort: [(columnName): order],
                ],
                textPrefix: controllerName,
                renderTextPrefix: isHeader && !table.labels[columnName],
                textWrap: TextWrap.NO_WRAP,
                textStyle: TextStyle.BOLD,
        )

        return addCell(columnName, sortableHeader)
    }

    /**
     * Hides the row-selection checkbox for this row by setting {@link #hasSelection}
     * to {@code false}.
     */
    void removeSelection() {
        hasSelection = false
    }

    /**
     * Sets whether this row is a header row. When {@code true}, the selection checkbox ID
     * is updated to the table-level "select all" ID.
     *
     * @param value {@code true} if this is a header row
     */
    void setIsHeader(Boolean value) {
        isHeader = value
        if (isHeader) {
            selected.id = table.id + '-select-all'
        }
    }
}
