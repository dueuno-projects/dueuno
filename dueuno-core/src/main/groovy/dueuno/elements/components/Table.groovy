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
import dueuno.elements.Component
import dueuno.elements.Elements
import dueuno.elements.style.TextDefault
import groovy.transform.CompileStatic

/**
 * A data-grid component that renders a recordset as a pageable, sortable, filterable HTML table.
 * <p>
 * {@code Table} manages three logical sections — {@link #header}, {@link #body}, and
 * {@link #footer} — each backed by a {@link TableRowset}. Data is bound by calling
 * {@link #setBody(Collection)} (and optionally {@link #setHeader(Collection)} /
 * {@link #setFooter(Collection)}). Columns are inferred automatically from the recordset or
 * can be specified explicitly via {@link #columns}. Per-column labels, widths, sort order,
 * transformers, and pretty-printer properties are configurable through the corresponding maps.
 * </p>
 * <p>
 * The table provides a built-in row-actions {@link Button} ({@link #actions}) pre-configured
 * with "Edit" and "Delete" actions, a group-actions button ({@link #groupActions}), a
 * {@link TableFilters} bar, a {@link TablePagination} control, and a {@link TableDataset}
 * for client-side data exchange.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class Table extends Component {

    /** Internal recordset cache; populated during body initialisation. */
    private List<Map> recordset

    /** Ordered list of column property names to display. */
    List<String> columns

    /** Property names used as row identity keys (submitted with row selections). */
    List<String> keys

    /** Map of column name → default sort direction ({@code "asc"}/{@code "desc"}) for sortable columns. */
    Map<String, String> sortable

    /** Current sort state: column name → sort direction. */
    Map<String, String> sort

    /** Parameters merged into the data-fetch request (e.g. {@code sort}, filters). */
    Map<String, Object> fetchParams

    /** Additional parameters submitted with each row action. */
    Map<String, Object> submitParams

    /** Component IDs whose values are submitted along with row actions. */
    List<String> submit

    /** Per-column width overrides in pixels. */
    Map<String, Integer> widths

    /** Per-column label overrides (i18n key or literal text). */
    Map<String, String> labels

    /** Per-column transformer names applied during rendering. */
    Map<String, String> transformers

    /** Per-column custom pretty-printer instances. */
    Map<String, Object> prettyPrinters

    /** Per-column {@link dueuno.core.PrettyPrinterProperties} maps. */
    Map<String, Map> prettyPrinterProperties

    /** Column names whose values are always included in the rendered output. */
    List<String> includeValues

    /** Column names whose values are never included in the rendered output. */
    List<String> excludeValues

    /** Optional title {@link Separator} displayed above the table. */
    Separator title

    /** The header row set (column headings). */
    TableRowset header

    /** The body row set (data rows). */
    TableRowset body

    /** The footer row set (summary/aggregate rows). */
    TableRowset footer

    /** The filter/action bar rendered above the table body. */
    TableActionbar actionbar

    /** The filter controls panel. */
    TableFilters filters

    /** The client-side dataset component used for AJAX data exchange. */
    TableDataset dataset

    /** Per-row action {@link Button} (pre-configured with Edit and Delete actions). */
    Button actions

    /** Group-selection action {@link Button} for bulk operations. */
    Button groupActions

    /** The pagination control rendered below the table body. */
    TablePagination pagination

    /** Whether the table header sticks to the top of the viewport on scroll. Defaults to {@code true}. */
    Boolean stickyHeader

    /** Vertical offset (in pixels) applied to the sticky header position. */
    Double stickyHeaderOffset

    /** CSS {@code z-index} of the sticky header. Defaults to {@code 0}. */
    Integer stickyHeaderZIndex

    /** Whether the column header row is displayed. Defaults to {@code true}. */
    Boolean hasHeader

    /** Whether the footer row set is displayed. Defaults to {@code true}. */
    Boolean hasFooter

    /** Whether pagination is enabled. Defaults to {@code false}. */
    Boolean hasPagination

    /** Whether the table contains embedded child components (e.g. buttons) in its cells. Defaults to {@code false}. */
    Boolean hasComponents

    /** Whether the per-row action button column is displayed. Defaults to {@code true}. */
    Boolean rowActions

    /** Whether hovered rows are visually highlighted. Defaults to {@code true}. */
    Boolean rowHighlight

    /** Whether alternating rows use a striped background. Defaults to {@code false}. */
    Boolean rowStriped

    /** Whether row borders are suppressed. Defaults to {@code true}. */
    Boolean rowBorderless

    /** Whether the table scrolls to the last changed row after a data update. Defaults to {@code false}. */
    Boolean rowScrollToLastChanged

    /** Whether the last changed row is visually highlighted after a data update. Defaults to {@code false}. */
    Boolean rowHighlightLastChanged

    /** Whether a "no results" placeholder is displayed when the body is empty. Defaults to {@code true}. */
    Boolean noResults

    /** Icon class used in the "no results" placeholder. */
    String noResultsIcon

    /** i18n message key for the "no results" placeholder text. */
    String noResultsMessage

    /**
     * Creates a {@code Table} instance configured from the supplied argument map.
     * Initialises all sub-components (header, body, footer, filters, dataset, pagination,
     * actions, group-actions) and applies default values for all display flags.
     *
     * @param args initialisation arguments; recognised keys include all public fields above,
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    Table(Map args) {
        super(args)

        columns = []
        keys = []
        sortable = [:]
        sort = [:]
        fetchParams = [:]
        submit = []
        submitParams = [:]

        includeValues = []
        excludeValues = []

        widths = [:]
        labels = [:]
        transformers = [:]
        prettyPrinters = [:]
        prettyPrinterProperties = [:]

        recordset = []

        // Number of rows needed to trigger the sticky header
        stickyHeader = args.stickyHeader == null ? true : args.stickyHeader as Boolean
        stickyHeaderOffset = args.stickyHeaderOffset as Double
        stickyHeaderZIndex = args.stickyHeaderZIndex == null ? 0 : args.stickyHeaderZIndex as Integer

        rowActions = (args.rowActions == null) ? true : args.rowActions
        hasHeader = (args.hasHeader == null) ? true : args.hasHeader
        hasFooter = (args.hasFooter == null) ? true : args.hasFooter
        hasPagination = (args.hasPagination == null) ? false : args.hasPagination
        hasComponents = (args.hasComponents == null) ? false : args.hasComponents

        rowHighlight = (args.rowHighlight == null) ? true : args.rowHighlight
        rowStriped = (args.rowStriped == null) ? false : args.rowStriped
        rowBorderless = (args.rowBorderless == null) ? true : args.rowBorderless
        rowScrollToLastChanged = (args.rowScrollToLastChanged == null) ? false : args.rowScrollToLastChanged
        rowHighlightLastChanged = (args.rowHighlightLastChanged == null) ? false : args.rowHighlightLastChanged

        noResults = (args.noResults == null) ? true : args.noResults
        noResultsIcon = (args.noResultsIcon == null) ? 'fa-regular fa-folder-open mb-n2' : args.noResultsIcon
        noResultsMessage = (args.noResultsMessage) ?: 'component.table.no.results'

        readonly = (args.readonly == null) ? false : args.readonly

        // COMPONENTS
        //
        title = createControl(
                class: Separator,
                id: 'title',
                text: buildLabel(getId()),
                squeeze: true,
                display: false,
        )
        header = createComponent(TableRowset, 'header', [
                table: this,
                isHeader: true,
        ])
        body = createComponent(TableRowset, 'body', [
                table: this,
        ])
        footer = createComponent(TableRowset, 'footer', [
                table: this,
                isFooter: true,
        ])
        filters = createComponent(TableFilters, 'filters', [
                table: this,
        ])
        actionbar = filters.actionbar
        dataset = createComponent(TableDataset, 'dataset', [
                table: this,
        ])
        pagination = createComponent(TablePagination, 'pagination', [
                table: this,
        ])

        // CONTROLS
        //
        actions = createControl(
                class: Button,
                id: 'commonActions',
                dontCreateDefaultAction: true,
        )
        actions.addDefaultAction(
                action: 'edit',
                text: '',
                icon: 'fa-pencil-alt',
                tooltip: TextDefault.EDIT,
        )
        actions.addTailAction(
                action: 'onDelete',
                text: '',
                icon: 'fa-solid fa-trash-alt',
                tooltip: TextDefault.HARD_DELETE,
                confirmMessage: TextDefault.MESSAGE_CONFIRM_UNRECOVERABLE_OPERATION,
        )
        groupActions = createControl(
                class: Button,
                id: 'groupActions',
                dontCreateDefaultAction: true,
                display: false,
        )
    }

    /**
     * Extracts the IDs of all selected rows from the request parameters map produced by a
     * table row-selection submission.
     *
     * @param params the request parameters map containing a {@code rows} list
     * @return a list of the {@code id} values for rows where {@code selected} is {@code true}
     */
    static List<Serializable> getSelected(Map params) {
        List<Serializable> ids = params.rows.findAll { it['selected'] }*.getAt('id') as List<Serializable>
        return ids
    }

    /**
     * Serialises this table's client-side properties to JSON, adding {@link #hasComponents},
     * {@link #stickyHeaderOffset}, and {@link #stickyHeaderZIndex}.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this table's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                hasComponents: hasComponents,
                stickyHeaderOffset: stickyHeaderOffset,
                stickyHeaderZIndex: stickyHeaderZIndex,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    /**
     * Returns the session key used to persist the requested sort state for this table instance.
     *
     * @return the session key string
     */
    private String getRequestedSortName() {
        return id + 'RequestedSort'
    }

    /**
     * Initialises table-level display state before rendering (e.g. shows the filter bar
     * only when filter controls are present).
     */
    private void initializeTable() {
        filters.display = hasFilters()
    }

    /**
     * Applies the given sort map to this table, merging it with any sort override requested
     * via the current HTTP request parameters. The resolved sort is stored in the action session
     * and propagated to {@link #fetchParams}.
     *
     * @param values the default sort map ({@code column → direction}); ignored if empty or {@code null}
     */
    void setSort(Map values) {
        if (!values) {
            return
        }

        Map<String, String> currentSort = actionSession[requestedSortName] as Map<String, String> ?: values
        Map<String, String> requestedSort = requestParams._21TableSort as Map ?: currentSort
        Boolean hasTableRequested = requestParams._21Table == id

        if (hasTableRequested && requestParams._21TableSort) {
            // The required sort column must be the first one on the map
            Map<String, String> head = [:]
            Map<String, String> body = [:]
            for (entry in currentSort) {
                String columnName = entry.key
                String requestedSortColumnName = requestedSort.keySet()[0]
                String requestedSortOrder = requestedSort.values()[0]

                if (columnName == requestedSortColumnName) {
                    head << [(columnName): requestedSortOrder]
                } else {
                    body << [(columnName): entry.value as String]
                }
            }

            sort = head + body
            actionSession[requestedSortName] = sort
            fetchParams.sort = sort as Map<String, Object>

        } else {
            sort = currentSort
            fetchParams.sort = currentSort
        }

        requestParams.sort = sort
    }

    /**
     * Sets the current page offset for pagination.
     *
     * @param value the zero-based row offset
     */
    void setOffset(Integer value) {
        pagination.setOffset(value)
    }

    /**
     * Sets the maximum number of rows per page for pagination.
     *
     * @param value the page size
     */
    void setMax(Integer value) {
        pagination.setMax(value)
    }

    /**
     * Populates {@link #columns} from the declared properties of the given class.
     * For GORM domain classes the constrained-properties map is used; for other classes
     * the declared fields are used. The {@code class} meta-property is excluded and the
     * resulting list is sorted alphabetically.
     *
     * @param clazz the domain class or POGO whose properties define the column list
     */
    void setColumnsFromClass(Class clazz) {
        List<String> properties
        if (Elements.isDomainClass(clazz)) {
            properties = (clazz['constrainedProperties'] as Map).collect { Map.Entry it -> it.key as String }
        } else {
            properties = clazz.declaredFields*.name
        }

        properties.remove("class")
        columns = properties.sort()
    }

    /**
     * Configures the sortable columns and applies the initial sort order.
     * The {@code values} map uses column names as keys and default sort directions as values.
     *
     * @param values the sortable columns map ({@code column → direction}); ignored if empty or {@code null}
     */
    void setSortable(Map values) {
        sortable = values
        if (sortable) {
            setSort(sortable)
        }
    }

    /**
     * Enables pagination and sets the total number of records (used to calculate page count).
     *
     * @param total the total number of records in the full result set
     */
    void setPaginate(Number total) {
        pagination.total = total
        hasPagination = true
    }

    /**
     * Sets a custom header recordset, overriding the auto-generated column headings.
     *
     * @param recordset the collection of maps to use as header rows
     */
    void setHeader(Collection recordset) {
        header.rows = recordset
    }

    /**
     * Binds the data rows to this table, triggering full table initialisation (key columns,
     * dev columns, filter display, and header generation).
     * Does nothing if {@code recordset} is empty or {@code null}.
     *
     * @param recordset the collection of domain objects or maps to display
     */
    void setBody(Collection recordset) {
        if (!recordset)
            return

        initializeTable()
        initializeKeyColumns(recordset)
        initializeDevColumns()

        // Loading table data
        header.rows = buildHeaders()
        body.rows = recordset
    }

    /**
     * Sets the footer rows for this table.
     * Does nothing if {@code recordset} is empty/null or if the body has no rows.
     *
     * @param recordset the collection of maps to use as footer rows
     */
    void setFooter(Collection recordset) {
        if (!recordset)
            return

        if (!body.hasRows())
            return

        // Table initialization
        footer.rows = recordset
    }

    /**
     * Adds a download/export action to the table's action bar, enabling the data to be
     * saved (e.g. as a CSV file).
     *
     * @param recordset unused; the parameter exists for API symmetry with {@link #setBody(Collection)}
     */
    void setSave(Object recordset) {
        actionbar.addAction(controller: 'table', action: 'download', icon: 'fa-download')
    }

    /**
     * Builds the header row list from the {@link #columns} list, substituting any
     * per-column label overrides from {@link #labels}.
     *
     * @return a single-element list containing a map of {@code column → label}
     */
    private List buildHeaders() {
        Map displayColumns = [:]

        for (column in columns) {
            if (labels[column] != null) {
                displayColumns[column] = labels[column]
            } else {
                displayColumns[column] = column
            }
        }

        return [displayColumns]
    }

    /**
     * Ensures that the {@link #keys} list is populated.
     * If the first record has an {@code id} property, {@code "id"} is added to {@link #keys}.
     * If no keys are configured and at least one column exists, the first column is used as the key.
     *
     * @param recordset the data recordset (must be non-empty)
     */
    private void initializeKeyColumns(Collection recordset) {
        // Add 'id' key if the first record contains an id
        if (recordset.size() > 0 && ObjectUtils.hasId(recordset[0])) {
            if ('id' !in keys) keys.add('id')

            // First column is the default key
        } else if (columns.size() > 0 && keys.size() == 0) {
            keys = [columns[0]]
        }
    }

    /**
     * Prepends key columns that are not already in {@link #columns} to the displayed column list
     * when developer display hints are active. This makes hidden key fields visible during
     * development.
     */
    void initializeDevColumns() {
        if (devDisplayHints) {
            List<String> devCols = []
            for (keyColumn in keys) {
                if (keyColumn !in columns) {
                    devCols.add(keyColumn)
                }
            }
            columns = devCols + columns
        }
    }

    /**
     * Returns the total number of rendered columns, including the row-actions column (if enabled)
     * and the group-actions column (if group actions are present).
     *
     * @return the total rendered column count
     */
    Integer getColumnsNumber() {
        Integer result = columns.size()
        if (rowActions) result++
        if (groupActions.hasActions()) result++
        return result
    }

    //
    // FILTERS
    //

    /**
     * Returns {@code true} if this table has at least one filter control registered.
     *
     * @return {@code true} if filters are present
     */
    Boolean hasFilters() {
        return filters.controls
    }

    /**
     * Returns the current values of all filter controls as a parameter map.
     *
     * @return a map of filter control ID → current value
     */
    Map getFilterParams() {
        return filters.values
    }
}
