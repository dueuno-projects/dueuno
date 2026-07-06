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
 * A lightweight component that holds a reference to the owning {@link Table} and provides
 * the rendering context for the table's dataset section.
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class TableDataset extends Component {

    /** The {@link Table} this dataset component belongs to. */
    Table table

    /**
     * Creates a {@code TableDataset} instance configured from the supplied argument map.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code table} ({@link Table}, required),
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    @Requires({ args.table })
    TableDataset(Map args) {
        super(args)

        table = args.table as Table
    }

}
