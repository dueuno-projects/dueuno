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

import dueuno.elements.Menu
import groovy.transform.CompileStatic

/**
 * A {@link ContentBlank} that renders the application home/dashboard page.
 * <p>
 * {@code ContentHome} uses its own dedicated {@code ContentHome} view template and
 * exposes a {@link #favouriteMenu} component that can be populated with shortcuts to
 * frequently used features. It is typically displayed as the default landing page
 * after login.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class ContentHome extends ContentBlank {

    /** The {@link Menu} component used to display the user's favourite / quick-access items. */
    Menu favouriteMenu

    /**
     * Creates a {@code ContentHome} instance configured from the supplied argument map.
     * Sets the view template to {@code ContentHome} and registers the {@link #favouriteMenu} component.
     *
     * @param args initialisation arguments forwarded to {@link ContentBlank#ContentBlank(Map)}
     */
    ContentHome(Map args) {
        super(args)

        viewPath = '/dueuno/elements/contents/'
        viewTemplate = 'ContentHome'

        favouriteMenu = addComponent(Menu, 'favouriteMenu')
    }
}
