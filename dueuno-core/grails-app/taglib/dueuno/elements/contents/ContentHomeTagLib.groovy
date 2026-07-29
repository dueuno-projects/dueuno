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

import dueuno.elements.core.WebRequestAware
import dueuno.elements.core.Menu
import dueuno.elements.pages.ShellService
import dueuno.elements.pages.Shell

/**
 * INTERNAL USE ONLY
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
class ContentHomeTagLib implements WebRequestAware {

    static namespace = "home"
    String tagsTemplatesPath = '/dueuno/elements/contents/'

    ShellService shellService

    /**
     * INTERNAL USE ONLY. Returns the 'home' menu item that holds all of the session menu items.
     */
    def displayFavourites = { attrs ->
        Shell shell = shellService.shell
        Menu parent = attrs.parent ?: shell.home.favouriteMenu
        List<Menu> favourites = parent.items.findAll { !it.hasSubitems() }

        for (feature in favourites) {
            out << g.render(template: tagsTemplatesPath + "ContentHomeFavourite", model: [feature: feature])
        }
    }
}
