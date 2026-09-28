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
import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.DateField
import dueuno.elements.controls.TimeField

import java.time.LocalDate

class DateController implements ElementsController {

    def index() {
        // PAGE SETUP
        //
        def c = createContent(ContentForm)
        c.with {
            form.with {
                addField(
                    class: DateField,
                    id: 'date1',
                    min: LocalDate.now()
                )
                addField(
                    class: DateField,
                    id: 'date2',
                    max: new LocalDate(2017, 5, 5)
                )
                addField(
                    class: TimeField,
                    id: 'time1',
                    //min: new LocalTime(16, 0, 0, 0),
                )
                addField(
                    class: TimeField,
                    id: 'time2',
                    //max: new LocalTime(12, 0, 0, 0),
                )
                addField(
                    class: Button,
                    id: 'test1',
                    isDefaultAction: true,
                    stretch: true,
                )
                //readonly = true
            }
        }

        //c.form.date1.on(event: 'change', action: 'setMinDate')
        //c.form.date2.on(event: 'change', action: 'setMaxDate')
        //c.form.time1.on(event: 'change', action: 'setMinTime')
        //c.form.time2.on(event: 'change', action: 'setMaxTime')

        // RENDERING
        //
        display content: c
    }

    def setMinDate() {
        def t = createTransition()
        t.set('date2', 'min', params.date1)
        display transition: t
    }

    def setMaxDate() {
        def t = createTransition()
        t.set('date1', 'max', params.date2)
        display transition: t
    }

    def setMinTime() {
        def t = createTransition()
        t.set('time2', 'min', params.time1)
        display transition: t
    }

    def setMaxTime() {
        def t = createTransition()
        t.set('time1', 'max', params.time2)
        display transition: t
    }

}
