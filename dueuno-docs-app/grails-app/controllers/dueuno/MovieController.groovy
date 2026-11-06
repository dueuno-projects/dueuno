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
package dueuno

import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.NumberField
import dueuno.elements.controls.TextField
import grails.plugin.springsecurity.annotation.Secured
import jakarta.annotation.PostConstruct

@Secured(['ROLE_USER', /* other ROLE_... */])
class MovieController implements ElementsController {

    MovieService movieService

    @PostConstruct
    void init() {
        // Executes only once when the application starts
    }

    def index() {
        ContentTable c = createContent(ContentTable)
        c.table.with {
            filters.with {
                addField(
                        class: TextField,
                        id: 'find',
                        label: 'default.filters.text',
                        cols: 12,
                )
            }
            sortable = [
                    title: 'asc',
            ]
            columns = [
                    'title',
                    'released',
            ]

            body.eachRow { TableRow row, Map values ->
                // Do not execute slow operations here to avoid slowing down the table rendering
            }
        }

        def filters = c.table.filterParams
        c.table.body = movieService.list(filters, params)
        c.table.paginate = movieService.count(filters)

        display content: c
    }

    private buildForm(TMovie obj = null) {
        def c = obj
                ? createContent(ContentEdit)
                : createContent(ContentCreate)

        c.form.with {
            validate = TMovie
            addField(
                    class: TextField,
                    id: 'title',
            )
            addField(
                    class: NumberField,
                    id: 'released',
            )
        }

        if (obj) {
            c.form.values = obj
        }

        return c
    }

    def create() {
        def c = buildForm()
        display content: c, modal: true
    }

    def onCreate() {
        def obj = movieService.create(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        display action: 'index'
    }

    def edit() {
        def obj = movieService.get(params.id)
        def c = buildForm(obj)
        display content: c, modal: true
    }

    def onEdit() {
        def obj = movieService.update(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        display action: 'index'
    }

    def onDelete() {
        try {
            movieService.delete(params.id)
            display action: 'index'

        } catch (e) {
            display exception: e
        }
    }
}
