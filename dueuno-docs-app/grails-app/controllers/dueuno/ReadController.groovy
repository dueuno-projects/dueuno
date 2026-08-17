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
import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.Select
import dueuno.elements.controls.Textarea

class ReadController implements ElementsController {

    BookService bookService

    def index() {
        def c = createContent(ContentForm)

        c.header.removeNextButton()

        c.form.with {
            addField(
                    class: Select,
                    optionsFromRecordset: bookService.list(),
                    prettyPrinter: 'BOOK',
                    onChange: 'onBookChange',
                    id: 'book',
            )
            addField(
                    class: Textarea,
                    id: 'description',
            )
        }

        display content: c
    }

    def onBookChange() {
        def t = createTransition()
        def book = bookService.get(params.book)
        if (book) {
            t.set('description', 'readonly', true)
            t.setValue('description', book.description)
        } else {
            t.set('description', 'readonly', false)
            t.setValue('description', null)
        }
        display transition: t
    }
}
