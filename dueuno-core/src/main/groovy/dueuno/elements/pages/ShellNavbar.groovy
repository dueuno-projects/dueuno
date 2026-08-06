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
package dueuno.elements.pages

import dueuno.elements.components.Button
import dueuno.elements.components.Image
import dueuno.elements.components.Link
import dueuno.elements.core.Component
import groovy.contracts.Requires
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 */

@CompileStatic
class ShellNavbar extends Component {

    Shell shell
    Button home
    Link logo

    @Requires({ args.shell })
    ShellNavbar(Map args) {
        super(args)

        viewPath = '/dueuno/elements/pages/'

        shell = args.shell as Shell
        home = (Button) createComponent(
            class: Button,
            id: 'home',
            controller: 'shell',
            icon: 'fa-solid fa-home',
            text: '',
            tooltip: 'shell.home.menu',
            animate: 'fade',
        )
        logo = (Link) createComponent(
            class: Link,
            id: 'logo',
            controller: 'shell',
            animate: 'fade',
        )
        logo.addComponent(
            class: Image,
            id: 'logoImg',
            image: shell.config.display.logo,
        )
    }
}
