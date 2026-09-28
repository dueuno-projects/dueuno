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
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable

class CrudRestController implements ElementsController {

    String LAYOUT = 'W_T002_Artidoli_dettaglio'
//    FileMakerClient fileMakerClient

    def index() {
        ContentTable c = createContent(ContentTable)
        c.table.with {
            keys = [
                'RecordID',
            ]
            columns = [
                'RecordID',
                'C001_CodArt',
                'C002_Descrizione_IT',
                'C002_Descrizione_EN',
                'C002_Descrizione_RU',
                'C150_Disponibilita',
            ]
            actions.removeDefaultAction()
            actions.removeTailAction()
            actions.addDefaultAction(action: 'edit', text: 'Ordina', icon: 'fa-shopping-cart')
        }

        def filters = c.table.filterParams

        fileMakerClient.connect(
            'https',
            'fm6.zanatto.com',
            443,
            'apitest',
            'Karma8LHR',
            'WebSiteMirho'
        )
        Map rs = fileMakerClient.list(LAYOUT, filters, params)

        c.table.body = rs.list
        c.table.paginate = rs.count

        fileMakerClient.disconnect()

        display content: c
    }

    private buildForm(Map args = [:]) {
        def c = args.create
            ? createContent(ContentCreate)
            : createContent(ContentEdit)

        c.form.with {
            validate = CrudRestValidator
            addKeyField('RecordID')
//            addField(
//                    class: 'TextField',
//                    id: 'C001_CodArt',
//            )
//            addField(
//                    class: 'TextField',
//                    id: 'C002_Descrizione_IT',
//            )
            addField(
                class: 'NumberField',
                id: 'C150_Disponibilita',
            )
        }
        return c
    }

    def create() {
        def c = buildForm(create: true)
        display content: c, modal: true
    }

    def onCreate(CrudRestValidator val) {
        if (val.hasErrors()) {
            display errors: val
            return
        }

        try {
            fileMakerClient.create(LAYOUT, params.RecordID, params)
            display action: 'index'

        } catch (e) {
            e.printStackTrace()
            display exception: e
        }
    }

    def edit() {
        def c = buildForm(create: false)
        c.form.values = fileMakerClient.get(LAYOUT, params.RecordID)
        display content: c, modal: true
    }

    def onEdit(CrudRestValidator val) {
        if (val.hasErrors()) {
            display errors: val
            return
        }

        try {
            fileMakerClient.update(LAYOUT, params.RecordID, params)
            display action: 'index'

        } catch (e) {
            e.printStackTrace()
            display exception: e
        }
    }

    def onDelete() {
        try {
            fileMakerClient.delete(LAYOUT, params.RecordID)
            display action: 'index'

        } catch (e) {
            e.printStackTrace()
            display exception: e
        }
    }
}
