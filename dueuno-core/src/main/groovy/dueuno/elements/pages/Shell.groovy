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

import dueuno.elements.contents.ContentHome
import dueuno.elements.core.Page
import groovy.contracts.Requires
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class Shell extends Page {

    ShellConfig config

    ShellNavbar navbar
    ShellMenu menu
    ShellUserMenu userMenu

    ContentHome home

    String username
    String userFullname

    @Requires({ args.config })
    Shell(Map args) {
        super(args)

        // Default configuration
        config = args.config as ShellConfig

        username = ''
        userFullname = ''

        menu = addComponent(ShellMenu, 'menu', [shell: this, textPrefix: 'shell'])
        menu.createFromFeature(config.features.main)
        menu.displaySearch = config.display.menuSearch

        userMenu = addComponent(ShellUserMenu, 'userMenu', [shell: this, textPrefix: 'shell'])
        userMenu.createFromFeature(config.features.user)

        navbar = addComponent(ShellNavbar, 'navbar', [shell: this, textPrefix: 'shell'])
        home = createComponent(ContentHome, 'home', [shell: this, textPrefix: 'shell'])
    }

    void setUser(String username, String firstname, String lastname) {
        this.username = username
        this.userFullname = firstname + ' ' + lastname
        userMenu.title = firstname ?: lastname ?: username
        home.favouriteMenu.createFromFeature(config.features.main, true)
    }
}
