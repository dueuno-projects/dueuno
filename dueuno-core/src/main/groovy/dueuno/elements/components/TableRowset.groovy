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
import groovy.contracts.Requires
import groovy.transform.CompileStatic

/**
 * A band of {@link TableRow} instances within a {@link Table}, representing the header,
 * body, or footer section.
 * <p>
 * When {@link #setRows(Collection)} is called, each record in the collection is turned into
 * a {@link TableRow}. For each row the processing pipeline runs in order:
 * {@link TableRow#preProcessRow()}, the optional {@link #eachRow(Closure)} user closure,
 * and {@link TableRow#postProcessRow()}. Convenience references to the first and last
 * created rows are kept in {@link #firstRow} and {@link #lastRow}.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class TableRowset extends Component {

    /** The {@link Table} this rowset belongs to. */
    Table table

    /** The raw collection of records used to populate {@link #rows}. */
    Collection recordset

    /** The ordered list of {@link TableRow} instances created from {@link #recordset}. */
    List<TableRow> rows

    /** Reference to the first {@link TableRow} created; {@code null} before {@link #setRows(Collection)} is called. */
    TableRow firstRow

    /** Reference to the last {@link TableRow} created; {@code null} before {@link #setRows(Collection)} is called. */
    TableRow lastRow

    /** Optional user-supplied closure invoked for each row during {@link #setRows(Collection)}. */
    Closure eachRowClosure

    /** {@code true} if this rowset represents the table header section. */
    Boolean isHeader

    /** {@code true} if this rowset represents the table footer section. */
    Boolean isFooter

    /**
     * Creates a {@code TableRowset} instance configured from the supplied argument map.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code table} ({@link Table}, required),
     *             {@code isHeader} ({@link Boolean}, default {@code false}),
     *             {@code isFooter} ({@link Boolean}, default {@code false}),
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    @Requires({ args.table })
    TableRowset(Map args) {
        super(args)

        table = args.table as Table

        rows = []
        firstRow = null
        lastRow = null

        isHeader = (args.isHeader == null) ? false : args.isHeader
        isFooter = (args.isFooter == null) ? false : args.isFooter
    }

    /**
     * Registers a closure to be invoked for each row during {@link #setRows(Collection)}.
     * The closure receives either one parameter ({@link TableRow}) or two parameters
     * ({@link TableRow} and the row's values map).
     *
     * @param c the closure to invoke per row
     */
    void eachRow(Closure c) {
        eachRowClosure = c
    }

    /**
     * Populates this rowset from the given collection, creating a {@link TableRow} for each
     * record. Tracks {@link #firstRow} and {@link #lastRow}, runs the per-row processing
     * pipeline, and invokes {@link #eachRowClosure} when set.
     *
     * @param collection the source records; each element becomes one {@link TableRow}
     */
    void setRows(Collection collection) {
        recordset = collection
        rows = []

        Integer i = 0
        for (record in recordset) {
            TableRow row = addRow(i, record)

            if (i == 0) firstRow = row
            if (i == recordset.size() - 1) lastRow = row

            row.preProcessRow()

            if (eachRowClosure) {
                if (eachRowClosure.maximumNumberOfParameters == 1) {
                    eachRowClosure.call(row)
                } else {
                    eachRowClosure.call(row, row.values)
                }
            }

            row.postProcessRow()
            i++
        }
    }

    /**
     * Builds the component name for a row at the given index, prefixed by the table ID
     * and suffixed with {@code -h} (header), {@code -f} (footer), or nothing (body).
     *
     * @param i the zero-based row index
     * @return the generated row component name
     */
    private String buildRowName(Integer i) {
        if (isHeader) {
            return table.getId() + "-h${i}"
        } else if (isFooter) {
            return table.getId() + "-f${i}"
        } else {
            return table.getId() + "-${i}"
        }
    }

    /**
     * Creates a {@link TableRow} for the given record, registers it in {@link #rows},
     * and returns it.
     *
     * @param index  the zero-based row index
     * @param values the record data for this row
     * @return the newly created {@link TableRow}
     */
    private TableRow addRow(Integer index, Object values) {
        TableRow row = createComponent(TableRow, buildRowName(index), [
                table: table,
                rowset: this,
                index: index,
                isHeader: isHeader,
                isFooter: isFooter,
                values: values,
        ])

        rows.add(row)
        return row
    }

    /**
     * Returns the list of processed {@link TableRow} instances.
     *
     * @return the list of rows
     */
    List getProcessedRows() {
        return rows
    }

    /**
     * Returns {@code true} if this rowset contains at least one row.
     *
     * @return {@code true} when {@link #rows} is non-empty
     */
    Boolean hasRows() {
        return rows.size() > 0
    }

    /**
     * Returns the number of rows in this rowset.
     *
     * @return the row count
     */
    Integer getRowsCount() {
        return rows.size()
    }

    /**
     * Returns the last {@link TableRow} created by {@link #setRows(Collection)},
     * or {@code null} if no rows have been set.
     *
     * @return the last row, or {@code null}
     */
    TableRow getLastRow() {
        return lastRow
    }

    /**
     * Returns the first {@link TableRow} created by {@link #setRows(Collection)},
     * or {@code null} if no rows have been set.
     *
     * @return the first row, or {@code null}
     */
    TableRow getFirstRow() {
        return firstRow
    }
}

