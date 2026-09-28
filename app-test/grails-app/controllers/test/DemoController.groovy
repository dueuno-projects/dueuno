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
import dueuno.elements.contents.ContentTable
import dueuno.types.Money

class DemoController implements ElementsController {

    def index() {
        def c = createContent(ContentTable)

        c.header.text = "Shopping cart"

        c.table.columns = [
            'product',
            'quantity',
            'price',
        ]

        c.table.body = [
            ['Pasta', 1, new Money(2.3)],
            ['Nutella', 2, new Money(4)],
            ['Milk', 6, new Money(8)],
            ['Coffee', 1, new Money(3.4)],
            ['Potatoes', 5, new Money(7)],
        ]

        display content: c
    }
}
