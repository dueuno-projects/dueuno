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
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.SelectX
import dueuno.elements.controls.TextField

class BookController implements ElementsController {

    BookService bookService

    def index() {
        def c = createContent(ContentTable)

        c.table.with {
            filters.with {
                addField(
                        class: SelectX,
                        optionsFromRecordset: bookService.list(),
                        prettyPrinter: 'BOOK',
                        id: 'book',
                        cols: 4,
                )
                addField(
                        class: TextField,
                        id: 'search',
                        cols: 8,
                )
            }
            columns = [
                    'title',
                    'author',
            ]
        }

        c.table.body = bookService.list(c.table.filterParams)

        display content: c
    }

    private buildForm(Map obj = null) {
        def c = obj
                ? createContent(ContentEdit)
                : createContent(ContentCreate)

        c.form.with {
            addField(
                    class: TextField,
                    id: 'title',
            )
            addField(
                    class: TextField,
                    id: 'author',
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

    def onCreate(BookValidator obj) {
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        bookService.create(params)
        display action: 'index'
    }

    def edit() {
        def book = bookService.get(params.id)
        def c = buildForm(book)
        display content: c, modal: true
    }

    def onEdit(BookValidator obj) {
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        bookService.update(params)
        display action: 'index'
    }

    def onDelete() {
        try {
            bookService.delete(params.id)
            display action: 'index'

        } catch (Exception e) {
            display exception: e
        }
    }
}

