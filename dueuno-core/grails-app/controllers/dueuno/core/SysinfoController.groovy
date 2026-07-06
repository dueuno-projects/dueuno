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
package dueuno.core

import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentTable
import dueuno.elements.style.TextAlign
import dueuno.elements.style.TextStyle
import dueuno.elements.style.TextWrap
import grails.plugin.springsecurity.annotation.Secured

/**
 * @author Gianluca Sartori
 */
@Secured(['ROLE_SUPERADMIN'])
class SysinfoController implements ElementsController {

    SystemInfoService systemInfoService

    def index() {
        def c = createContent(ContentTable)

        c.header.with {
            removeNextButton()
        }

        c.table.with {
            columns = [
                    'key',
                    'value',
            ]
            hasHeader = false
            rowActions = false
            body.eachRow { TableRow row, Map values ->
                row.cells.key.tag = true
                row.cells.key.textAlign = TextAlign.START
                row.textStyle = TextStyle.MONOSPACE
                row.cells.value.textWrap = TextWrap.SOFT_WRAP
            }
            body = systemInfoService.info.collect {[key: it.key, value: it.value]}
        }

        display content: c, modal: true, large: true
    }
}
