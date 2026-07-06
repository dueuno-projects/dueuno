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

import groovy.transform.CompileStatic

/**
 * A visual separator component used to divide sections within a form or layout.
 * <p>
 * {@code Separator} extends {@link Label} so it can display optional text, but it
 * suppresses the field label and help text and skips focus. It is typically rendered
 * as a horizontal rule or a titled divider line.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class Separator extends Label {

    /**
     * Whether the separator collapses its top margin to reduce vertical spacing.
     * Defaults to {@code false}.
     */
    Boolean squeeze

    /**
     * Creates a {@code Separator} instance configured from the supplied argument map.
     * Suppresses the field label and help text, sets focus-skip, and defaults to a
     * full-width (12-column) layout.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code squeeze} ({@link Boolean}, default {@code false}),
     *             {@code cols} ({@link Integer}, default {@code 12}),
     *             plus all keys accepted by {@link Label#Label(Map)}
     */
    Separator(Map args) {
        super(args)

        squeeze = (args.squeeze == null) ? false : args.squeeze
        skipFocus = true

        containerSpecs.cols = args.cols ?: 12
        containerSpecs.displayLabel = false
        containerSpecs.displayHelp = false
    }
}
