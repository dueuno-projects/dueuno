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

import dueuno.elements.Control
import groovy.transform.CompileStatic

/**
 * A specialised {@link Form} that provides the filter bar rendered above a {@link Table}.
 * <p>
 * Filter fields are added with {@link #addField(Map)}, which automatically shows the filter
 * panel and prepends the {@code filters.} prefix to the label key. Filter values are resolved
 * from request parameters and the action session via {@link #getValues()}, which also sets
 * {@link #isFiltering} and {@link #prettyValues} as side effects.
 * </p>
 * <p>
 * The component creates a {@link TableActionbar} and two built-in link controls: a
 * {@link #searchButton} that submits the filter form, and a {@link #resetButton} that clears
 * all filters.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class TableFilters extends Form {

    /** The parent {@link Table} that owns this filter bar. */
    Table table

    /** The action bar rendered alongside the filter controls (holds custom actions). */
    TableActionbar actionbar

    /** Whether at least one filter control currently has a non-empty value. */
    Boolean isFiltering

    /** Whether the filter panel is collapsed (folded) by default. */
    Boolean fold

    /** Whether the filter panel folds automatically after a search is performed. */
    Boolean autoFold

    /** A comma-separated human-readable summary of the active filter values. */
    String prettyValues

    /** The search/submit {@link Link} button that triggers the filter query. */
    Link searchButton

    /** The reset {@link Link} button that clears all filter values. */
    Link resetButton

    /**
     * Creates a {@code TableFilters} instance configured from the supplied argument map.
     * Initialises the {@link #actionbar}, {@link #searchButton}, and {@link #resetButton} controls.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code table} ({@link Table}, required),
     *             {@code fold} ({@link Boolean}, default {@code false}),
     *             {@code autoFold} ({@link Boolean}, default {@code false}),
     *             plus all keys accepted by {@link Form#Form(Map)}
     */
    TableFilters(Map args) {
        super(args)

        table = args.table as Table

        display = false
        fold = isMobileDevice() ? true : (args.fold == null) ? false : args.fold
        autoFold = isMobileDevice() ? true : (args.autoFold == null) ? false : args.autoFold
        isFiltering = false
        prettyValues = ''

        // CONTROLS
        //
        actionbar = createComponent(
                class: TableActionbar,
                id: 'tableActionbar',
                table: table,
                filters: this,
        )

        searchButton = createControl(
                class: Link,
                id: 'searchButton',
                action: actionName,
                submit: [id],
                icon: 'fa-magnifying-glass',
                text: '',
        )
        resetButton = createControl(
                class: Link,
                id: 'resetButton',
                action: actionName,
                icon: 'fa-delete-left',
                tooltip: 'component.table.filters.reset',
                text: '',
        )
    }

    /**
     * Serialises this filter form's properties to JSON, finalising submit-parameter
     * assignments for the search and reset buttons before delegating to the parent.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this filter form's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        setSubmitParams()
        Map thisProperties = [
                autoFold: autoFold,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    /**
     * Adds a filter field to this filter form.
     * Auto-generates the label from the {@code filters.<id>} i18n key when no label is supplied,
     * enables the "allow clear" option for Select controls, and makes the filter panel visible.
     *
     * @param args field configuration forwarded to {@link Form#addField(Map)}
     * @return the newly created {@link FormField}
     */
    FormField addField(Map args) {
        args.label = (args.label != null) ? args.label : buildLabel(filtersFieldPrefix + args.id)
        args.allowClear = true // for Select controls
        display = true

        FormField field = super.addField(args)
        return field
    }

    /**
     * Clears all filter values from the action session, effectively resetting the filter state
     * without reloading the page.
     */
    void reset() {
        for (controlEntry in controls) {
            Control control = controlEntry.value
            actionSession.remove(control.id)
        }
    }

    /**
     * Wires the search and reset buttons with the table's submit parameters and configures
     * each filter control to submit the filter form on change.
     */
    private void setSubmitParams() {
        searchButton.params = table.submitParams + (Map)[
                _21Table: table.id,
                _21FiltersSearch: true,
                _21TableOffset: 0,
        ]
        resetButton.params = table.submitParams + (Map)[
                _21Table: table.id,
                _21FiltersReset: true,
                _21TableOffset: 0,
        ]

        for (field in components) {
            if (field.component in Control) {
                Control control = field.component as Control
                Map args = searchButton.properties
                args.loading = true
                control.onSubmit(args)
            }
        }
    }

    /**
     * Returns the i18n label prefix used for filter field keys ({@code "filters."}).
     *
     * @return the filter field prefix string
     */
    private String getFiltersFieldPrefix() {
        return 'filters.'
    }

    /**
     * Resolves and sets the value for a single filter control, following this priority order:
     * <ol>
     *   <li>If a reset was requested ({@code _21FiltersReset}), the control's default value is
     *       restored (or the session entry is removed if no default exists).</li>
     *   <li>If the request parameters contain the control's key, that value is stored in the
     *       session and applied to the control.</li>
     *   <li>If the action session contains a previously saved value, it is restored.</li>
     *   <li>If the control already has a value, it is left unchanged.</li>
     *   <li>If the control has a default value, it is applied and stored in request parameters.</li>
     * </ol>
     *
     * @param control the filter control to initialise
     */
    private void initializeFilter(Control control) {
        if (requestParams._21FiltersReset) {
            if (control.defaultValue != null) {
                Object filterValue = control.defaultValue
                actionSession[control.id] = filterValue
                control.value = filterValue

            } else {
                actionSession.remove(control.id)
            }

            return
        }

        // Gets filters from PARAMS and sets the SESSION
        if (requestParams.containsKey(control.id)) {
            Object filterValue = requestParams[control.id]
            actionSession[control.id] = filterValue
            control.value = filterValue

            // Gets filters from SESSION
        } else if (actionSession[control.id]) {
            Object filterValue = actionSession[control.id]
            control.value = filterValue

        } else if (control.value != null) {
            // Gets filters from the assigned control value
            // See getValues()

        } else if (control.defaultValue != null) {
            control.value = control.defaultValue
            requestParams[control.id] = control.defaultValue
        }
    }

    /**
     * Returns whether the filter panel is currently in its folded (collapsed) state.
     * Checks the action session for a user-toggled fold preference before falling back
     * to the {@link #fold} default.
     *
     * @return {@code true} if the filter panel is folded
     */
    Boolean isFolded() {
        Object sessionParam = actionSession['_21FiltersFolded_' + getId()]
        if (sessionParam) {
            return sessionParam == 'true'
        } else {
            return fold
        }
    }

    /**
     * Resolves and returns the current filter values as a parameter map (keyed by control name
     * without the {@code filters.} prefix). As a side effect, sets {@link #isFiltering} to
     * {@code true} when at least one filter has a non-empty value, and populates
     * {@link #prettyValues} with a human-readable summary of the active filters.
     *
     * @return a map of filter control name → current value for all controls with a non-empty value
     */
    Map getValues() {
        Map results = [:]
        isFiltering = false
        String prettyResults = ''

        for (controlEntry in controls) {
            Control control = controlEntry.value

            initializeFilter(control)

            String controlName = control.id - (filtersFieldPrefix)
            Object controlValue = control.value

            if (controlValue) {
                results[controlName] = controlValue
                isFiltering = true
                prettyResults += (prettyResults == '' ? '' : ', ')
                prettyResults += message(controllerName + '.' + filtersFieldPrefix + controlName) + ': ' + control.prettyValue

            } else {
                results.remove(controlName)
            }
        }

        prettyValues = prettyResults
        return results
    }
}
