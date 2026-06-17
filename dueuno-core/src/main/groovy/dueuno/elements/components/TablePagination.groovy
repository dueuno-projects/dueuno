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
import dueuno.elements.style.TextDefault
import groovy.contracts.Requires
import groovy.transform.CompileStatic

/**
 * The pagination bar rendered below a {@link Table}, providing navigation links and
 * page-size selectors.
 * <p>
 * Manages the current {@link #offset} and {@link #max} values, persisting them in the
 * action session so they survive page reloads. Navigation {@link Link} controls
 * ({@link #goFirst}, {@link #goPrev}, {@link #goNext}) and page-size selectors
 * ({@link #goMax20}, {@link #goMax50}) are created automatically. The component is
 * hidden ({@link #display} is {@code false}) when the total record count is zero.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class TablePagination extends Component {

    /** The {@link Table} this pagination bar belongs to. */
    Table table

    /** Navigation link that jumps to the first page (offset = 0). */
    Link goFirst

    /** Navigation link that moves to the previous page. */
    Link goPrev

    /** Navigation link that moves to the next page. */
    Link goNext

    /** Page-size selector link that sets the page size to 20 records. */
    Link goMax20

    /** Page-size selector link that sets the page size to 50 records. */
    Link goMax50

    /** The current page size (number of records per page). */
    Number max

    /** The current zero-based record offset (index of the first displayed record). */
    Number offset

    /** The total number of records across all pages. */
    Number total

    /**
     * Creates a {@code TablePagination} instance configured from the supplied argument map.
     * Builds all navigation and page-size {@link Link} controls, then restores the last
     * known offset and max from the action session.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code table} ({@link Table}, required),
     *             {@code total} ({@link Integer}),
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    @Requires({ args.table })
    TablePagination(Map args) {
        super(args)

        table = args.table as Table
        total = args.total as Integer

        goFirst = createControl(
                class: Link,
                id: 'goFirst',
                controller: controllerName,
                action: actionName,
                submit: [table.filters.id],
                params: [
                        _21Table: table.id,
                        _21TableOffset: 0,
                ],
                icon: 'fa-angles-left',
                text: '',
                scroll: table.id,
        )
        goPrev = createControl(
                class: Link,
                id: 'goPrev',
                controller: controllerName,
                action: actionName,
                submit: [table.filters.id],
                icon: 'fa-angle-left',
                text: '',
                scroll: table.id,
        )
        goNext = createControl(
                class: Link,
                id: 'goNext',
                controller: controllerName,
                action: actionName,
                submit: [table.filters.id],
                icon: 'fa-angle-right',
                text: TextDefault.NEXT,
                scroll: table.id,
        )
        goMax20 = createControl(
                class: Link,
                id: 'goMax20',
                controller: controllerName,
                action: actionName,
                submit: [table.filters.id],
                params: [
                        _21Table: table.id,
                        _21TableOffset: 0,
                        _21TableMax: 20,
                ],
                text: 'component.table.pagination.display',
                textArgs: [20],
                scroll: table.id,
        )
        goMax50 = createControl(
                class: Link,
                id: 'goMax50',
                controller: controllerName,
                action: actionName,
                submit: [table.filters.id],
                params: [
                        _21Table: table.id,
                        _21TableOffset: 0,
                        _21TableMax: 50,
                ],
                text: 'component.table.pagination.display',
                textArgs: [50],
                scroll: table.id,
        )

        initializeDefaultParams()
    }

    /**
     * Returns the action-session key used to persist the current page offset for this table.
     *
     * @return the session key string for the requested offset
     */
    private String getRequestedOffsetName() {
        return table.id + 'RequestedOffset'
    }

    /**
     * Returns the action-session key used to persist the current page size for this table.
     *
     * @return the session key string for the requested max
     */
    private String getRequestedMaxName() {
        return table.id + 'RequestedMax'
    }

    /**
     * Restores the offset and max from the action session, or applies the defaults
     * ({@code offset = 0}, {@code max = 20}) if no session values are present.
     */
    private void initializeDefaultParams() {
        if (actionSession[requestedOffsetName] != null) {
            setOffset(actionSession[requestedOffsetName] as Integer)
        } else {
            setOffset(0)
        }

        if (actionSession[requestedMaxName] != null) {
            setMax(actionSession[requestedMaxName] as Integer)
        } else {
            setMax(20)
        }
    }

    /**
     * Sets the current page offset, honouring any {@code _21TableOffset} request parameter
     * for this table. Updates the action session and the table's fetch params accordingly.
     *
     * @param value the fallback offset to use when no request parameter is present
     */
    void setOffset(Integer value) {
        Integer currentOffset = actionSession[requestedOffsetName] as Integer ?: value
        Integer requestedOffset = requestParams._21TableOffset != null
                ? requestParams._21TableOffset as Integer
                : currentOffset
        Boolean hasTableRequest = requestParams._21Table == table.id

        if (hasTableRequest && requestParams._21TableOffset != null) {
            offset = requestedOffset
            actionSession[requestedOffsetName] = requestedOffset
            table.fetchParams.offset = requestedOffset

        } else {
            offset = currentOffset
            table.fetchParams.offset = currentOffset
        }

        requestParams.offset = offset
    }

    /**
     * Sets the current page size, honouring any {@code _21TableMax} request parameter
     * for this table. Updates the action session, the table's fetch params, and recalculates
     * the prev/next link params.
     *
     * @param value the fallback page size to use when no request parameter is present
     */
    void setMax(Integer value) {
        Integer currentMax = actionSession[requestedMaxName] as Integer ?: value ?: null
        Integer requestedMax = requestParams._21TableMax as Integer ?: currentMax
        Boolean hasTableRequested = requestParams._21Table == table.id

        if (hasTableRequested && requestParams._21TableMax) {
            max = requestedMax
            actionSession[requestedMaxName] = requestedMax
            table.fetchParams.max = requestedMax

        } else {
            max = currentMax
            table.fetchParams.max = currentMax
        }

        goPrev.params = [
                _21Table: table.id,
                _21TableOffset: offset - (max ?: 0),
        ]
        goNext.params = [
                _21Table: table.id,
                _21TableOffset: offset + (max ?: 0),
        ]
        requestParams.max = max
    }

    /**
     * Resets the pagination to the first page (offset = 0) unless an explicit
     * {@code _21TableOffset} request parameter is already present.
     */
    void reset() {
        if (requestParams._21TableOffset != null) {
            return
        }

        offset = 0
        actionSession[requestedOffsetName] = 0
        table.fetchParams.offset = 0
        requestParams._21TableOffset = 0
        goNext.params = [
                _21Table: table.id,
                _21TableOffset: max ?: 0,
        ]
    }

    /**
     * Sets the total record count and hides the pagination bar when the total is zero.
     *
     * @param value the total number of records
     */
    void setTotal(Number value) {
        total = value
        display = total > 0
    }

    /**
     * Returns {@code true} if the total number of records exceeds the current page size,
     * meaning multiple pages exist.
     *
     * @return {@code true} when pagination is needed
     */
    Boolean hasPages() {
        if (max == null) return false
        if (!total) return false
        return total > (max ?: 0)
    }

    /**
     * Returns {@code true} if there is a previous page (i.e. the current offset is greater than zero).
     *
     * @return {@code true} when a previous page exists
     */
    Boolean requiresPrev() {
        return offset > 0
    }

    /**
     * Returns {@code true} if there is a next page (i.e. the end of the current page does not
     * reach the total record count).
     *
     * @return {@code true} when a next page exists
     */
    Boolean requiresNext() {
        return offset + (max ?: 0) < total
    }

    /**
     * Returns the total record count formatted as a localised string, or an empty string
     * when the total is zero or null.
     *
     * @return the pretty-printed total, or {@code ""}
     */
    String getPrettyTotal() {
        if (!total) return ''
        return prettyPrint(total)
    }

    /**
     * Returns a localised pagination summary in the form {@code "first–last of total"}
     * (e.g. {@code "1–20 of 157"}), or an empty string when the total is zero or null.
     *
     * @return the formatted pagination summary string, or {@code ""}
     */
    String getPrettyPagination() {
        if (!total) return ''

        Number offset = offset ?: 0
        Number first = offset + 1
        Number last = (offset + (max ?: 0) < total) ? offset + (max ?: 0) : total
        String of = message('component.table.pagination.of')

        return  first + '-' + last + " ${of} " + prettyPrint(total)
    }
}
