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

import dueuno.elements.core.Menu
import groovy.contracts.Requires
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 */

@CompileStatic
class ShellMenu extends Menu {

    Shell shell
    Boolean favourite
    Boolean displaySearch

    @Requires({ args.shell })
    ShellMenu(Map args) {
        super(args)

        viewPath = '/dueuno/elements/pages/'

        shell = args.shell as Shell
        favourite = args.favourite == null ? false : args.favourite
        displaySearch = args.displaySearch == null ? true : args.displaySearch
    }
}
