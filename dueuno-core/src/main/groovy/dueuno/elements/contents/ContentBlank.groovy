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

import dueuno.elements.PageContent
import groovy.transform.CompileStatic

/**
 * A blank {@link PageContent} that renders the generic {@code PageContent} view template
 * without any additional structure or pre-built sub-components.
 * <p>
 * Use {@code ContentBlank} when you need a completely custom page area and want to
 * populate it freely with components, without the grid, form, or table scaffolding
 * provided by other {@code Content*} subclasses.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class ContentBlank extends PageContent {

    /**
     * Creates a {@code ContentBlank} instance configured from the supplied argument map.
     * Sets the view template to {@code "PageContent"}.
     *
     * @param args initialisation arguments forwarded to {@link PageContent#PageContent(Map)}
     */
    ContentBlank(Map args) {
        super(args)

        viewTemplate = 'PageContent'
    }
}
