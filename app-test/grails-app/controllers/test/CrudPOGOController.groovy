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
package test

import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.TextField

import java.time.LocalDateTime

class CrudPOGOController implements ElementsController {

    def index() {
        def c = createContent(ContentTable)
        c.with {
            table.with {
                filters.with {
                    addField(
                        class: TextField,
                        id: 'test',
                        cols: 12,
                    )
                }
                columns = [
                    'dateCreated',
                    'name',
                ]

                body.eachRow { TableRow row, Map values ->
                    println values
                }
            }
        }

        c.table.body = [
            new Company(dateCreated: LocalDateTime.now(), name: 'POGO Company 1'),
            new Company(dateCreated: LocalDateTime.now(), name: 'POGO Company 2'),
            new Company(dateCreated: LocalDateTime.now(), name: 'POGO Company 3'),
        ]
        c.table.paginate = 3

        display content: c
    }
}
