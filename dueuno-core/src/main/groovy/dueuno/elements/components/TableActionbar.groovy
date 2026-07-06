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
import groovy.transform.CompileStatic

/**
 * The action bar rendered above a {@link Table}, hosting table-level action buttons and
 * toggling the visibility of the associated {@link TableFilters}.
 * <p>
 * Actions are added via {@link #addAction(Map)} and are forwarded to an internal
 * {@link Button}. Adding any action automatically makes the filter bar visible
 * ({@link TableFilters#display} is set to {@code true}).
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class TableActionbar extends Component {

    /** The {@link Table} this action bar belongs to. */
    Table table

    /** The {@link TableFilters} associated with the table; made visible when an action is added. */
    TableFilters filters

    /** The {@link Button} that holds the action bar's action items. */
    Button actions

    /**
     * Creates a {@code TableActionbar} instance configured from the supplied argument map.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code table} ({@link Table}),
     *             {@code filters} ({@link TableFilters}),
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    TableActionbar(Map args) {
        super(args)

        table = args.table as Table
        filters = args.filters as TableFilters

        actions = createControl(
                class: Button,
                id: 'actions',
                dontCreateDefaultAction: true,
        )
    }

    /**
     * Adds an action to the action bar's {@link Button} and ensures the filter bar is visible.
     * <p>
     * Defaults {@code controller} to the current controller and {@code action} to {@code "index"}.
     * The action ID defaults to the action name (or {@code controller + action} when the
     * controller differs from the current one).
     * </p>
     *
     * @param args action configuration arguments forwarded to {@link Button#addAction(Map)}
     * @return the action {@link Component} added to the button
     */
    Component addAction(Map args) {
        String controller = args.controller ?: controllerName
        String action = args.action ?: 'index'

        args.id = args.id ?: (controller == controllerName ? action : controller + action?.capitalize())
        args.controller = controller
        args.action = action

        filters.display = true

        return actions.addAction(args + [
                controller: controller,
                action    : action,
        ])
    }

    /**
     * Adds a visual separator to the action bar's {@link Button}.
     *
     * @param text optional label displayed next to the separator; {@code null} for a plain divider
     * @return the separator {@link Component}
     */
    Component addSeparator(String text = null) {
        return actions.addSeparator(text)
    }
}