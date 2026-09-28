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
import dueuno.elements.components.Table
import dueuno.elements.components.TableRow

class StaticResourcesController implements ElementsController {

    def index() {
        def c = createContent()
        def table = c.addComponent(Table)
        table.with {
            columns = [
                'image',
                'name',
            ]
            sortable = [
                name: 'asc',
            ]
            body.eachRow { TableRow row, Map values ->
                def image = linkPublicResource("images/${values.image}")
                row.cells.image.html = '<img src="' + image + '" width="115" />'
            }

            //max = 20
            body = TFruit.list(params)
            paginate = TFruit.count()
        }
        display content: c
    }
}
