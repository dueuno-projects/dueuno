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
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.Checkbox
import dueuno.elements.controls.TextField

class DomainCrudController implements ElementsController {

    def index() {

        def c = createContent(ContentTable)
        c.table.with {
            columns = []

            body.eachRow { TableRow row, Map values ->
                // Do not execute slow operations here to avoid slowing down the table rendering
            }
        }

        def domainClass = grailsApplication.getArtefact('Domain', params.fullName).class
        c.table.body = domainClass.invokeMethod('list', [:])

        display content: c, modal: true
    }

    private buildForm(TCompany obj = null) {
        def c = obj
            ? createContent(ContentEdit)
            : createContent(ContentCreate)

        c.form.with {
            validate = TCompany
            addField(
                class: TextField,
                id: 'name',
                cols: 12,
            )
            addField(
                class: Checkbox,
                id: 'isOwned',
                label: '',
                cols: 4,
            )
            addField(
                class: Checkbox,
                id: 'isClient',
                label: '',
                cols: 4,
            )
            addField(
                class: Checkbox,
                id: 'isSupplier',
                label: '',
                cols: 4,
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
        def obj = companyService.create(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        display action: 'index'
    }

    def edit() {
        def obj = companyService.get(params.id)
        def c = buildForm(obj)
        display content: c, modal: true
    }

    def onEdit() {
        def obj = companyService.update(params)
        if (obj.hasErrors()) {
            display errors: obj
            return
        }

        display action: 'index'
    }

    def onDelete() {
        try {
            companyService.delete(params.id)
            display action: 'index'

        } catch (e) {
            display exception: e
        }
    }
}
