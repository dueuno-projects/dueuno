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

class CrudCompanyController implements ElementsController {

    def index() {
        def c = createContent(ContentTable)
        c.with {
            table.with {
                sortable = [
                    name: 'asc',
                ]
                columns = [
                    'dateCreated',
                    'name',
                    'employees',
                ]

                body.eachRow { TableRow row, Map values ->
                    println values
                }
            }
        }

        def query = TCompany.where {}
        c.table.body = query.list(params)
        c.table.paginate = query.count()

        display content: c, modal: true
    }
}
