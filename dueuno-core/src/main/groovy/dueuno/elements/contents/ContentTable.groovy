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
package dueuno.elements.contents

import dueuno.elements.components.Table
import dueuno.elements.style.TextDefault
import groovy.transform.CompileStatic

/**
 * A {@link ContentHeader} that pairs a standard page header with a {@link Table} component,
 * providing the canonical list/browse layout used throughout the Elements framework.
 * <p>
 * On construction a "New" button (linking to the {@code create} action) is automatically
 * added to the header, and an empty {@link Table} is registered as the main content
 * component. Both can be further configured after instantiation.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class ContentTable extends ContentHeader {

    /** The {@link Table} component that displays the list of records. */
    Table table

    /**
     * Creates a {@code ContentTable} instance configured from the supplied argument map.
     * Adds a "New" button to the header and initialises the {@link #table} component.
     *
     * @param args initialisation arguments forwarded to {@link ContentHeader#ContentHeader(Map)}
     */
    ContentTable(Map args) {
        super(args)

        header.addNextButton(
                action: 'create',
                text: TextDefault.NEW,
                icon: 'fa-plus',
        )

        table = addComponent(Table)
    }
}
