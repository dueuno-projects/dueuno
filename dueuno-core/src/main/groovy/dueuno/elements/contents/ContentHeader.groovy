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

import dueuno.elements.components.Header
import groovy.transform.CompileStatic

/**
 * A {@link ContentBlank} that adds a standard {@link Header} component as the first
 * child of the page area.
 * <p>
 * {@code ContentHeader} serves as the common base for all content types that require
 * a titled header bar (e.g. {@link ContentTable}, {@link ContentForm}). A default
 * "next" navigation button is automatically added to the header on construction and
 * can be further customised afterwards.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class ContentHeader extends ContentBlank {

    /** The {@link Header} component rendered at the top of the page area. */
    Header header

    /**
     * Creates a {@code ContentHeader} instance configured from the supplied argument map.
     * Registers a {@link Header} component and adds a default "next" button to it.
     *
     * @param args initialisation arguments forwarded to {@link ContentBlank#ContentBlank(Map)}
     */
    ContentHeader(Map args) {
        super(args)

        header = addComponent(Header)
        header.addNextButton()
    }
}
