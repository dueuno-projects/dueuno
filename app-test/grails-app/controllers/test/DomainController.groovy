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
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable

class DomainController implements ElementsController {

    def index() {
        def c = createContent(ContentTable)
        c.header.removeNextButton()

        c.table.with {
            keys = [
                'fullName',
            ]
            columns = [
                'shortName',
                'packageName',
                'pluginName',
            ]
            actions.removeTailAction()
            actions.defaultAction.controller = 'domainCrud'
            actions.defaultAction.action = 'index'
            body.eachRow { TableRow row, Map values ->
            }
        }

        List domainArtefacts = grailsApplication.getArtefacts("Domain")
        c.table.body = domainArtefacts

        display content: c
    }

    private buildForm(Object obj) {
        def c = createContent(ContentEdit)

        c.form.with {
//            validate = TCompany
        }

        c.form.values = obj

        return c
    }

    def edit() {
        List domainArtefacts = grailsApplication.getArtefacts("Domain")
        def domain = domainArtefacts[params.fullName]
        def c = buildForm(domain)
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
