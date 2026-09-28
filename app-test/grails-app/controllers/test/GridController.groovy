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
import dueuno.elements.components.Form
import dueuno.elements.components.Grid
import dueuno.elements.components.Label
import dueuno.elements.controls.NumberField
import dueuno.elements.controls.TextField
import dueuno.elements.style.TextAlign

class GridController implements ElementsController {

    def index() {
        def c = createContent()
        c.header.removeNextButton()

        Grid grid = c.addComponent(Grid)
        grid.xs = 12
        grid.sm = 6
        grid.lg = 3

        for (i in 1..4) {
            def col = grid.addColumn(lg: i == 4 ? 12 : i * 2)
            def form = col.addComponent(Form, "form${i}")
            form.with {
                addField(
                    class: TextField,
                    id: "text${i}",
                    cols: 6,
                )
                addField(
                    class: NumberField,
                    id: "number${i}",
                    cols: 6,
                )
            }
        }

        def grid2 = c.addComponent(Grid, "grid2")
        grid2.setBreakpoints()
        grid2.xs = 6
        grid2.backgroundColor = c.primaryBackgroundColor
        grid2.textColor = c.primaryTextColor
        grid2.spacing = 4
        grid2.tag = true

        for (i in 1..10) {
            def col = grid2.addColumn()
            col.addComponent(
                class: Label,
                id: "label${i}",
                text: "Label ${i}",
                textAlign: TextAlign.CENTER,
                cssClass: 'd-block',
            )
        }

        display content: c
    }
}
