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

import custom.CustomPage
import dueuno.elements.ElementsController
import dueuno.elements.components.Form
import dueuno.elements.components.Label
import dueuno.elements.components.Table
import dueuno.elements.components.TableRow
import dueuno.security.SecurityService
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class CustomPageController implements ElementsController {

    SecurityService securityService

    def index() {
        def p = createPage(CustomPage)
        def header = p.content.addComponent(Form, 'headerForm')
        header.with {
            addField(
                class: Label,
                id: 'status',
                html: buildStatusLabel(getCurrentStatus()),
                cols: 12,
                displayLabel: false,
            )
        }

        def table = p.content.addComponent(Table)
        table.with {
            hasHeader = false
            rowHighlight = false
//            filters.with {
//                folded = false
//                addField(
//                        class: Select,
//                        id: 'clientRef',
//                )
//                addField(
//                        class: TextField,
//                        id: 'markplateKey',
//                )
//            }

            actionbar.with {
                actions.group = true
//                addAction(
//                        action: 'onConnect'
//                )
//                addAction(
//                        action: 'onExecute'
//                )
//                addAction(
//                        action: 'onDisconnect'
//                )
//                addAction(
//                        action: 'onTestStatus',
//                )
            }
            sortable = [
                username: 'asc',
            ]
            columns = [
                'username',
                'firstname',
                'lastname',
            ]

            actions.removeTailAction()
            actions.removeDefaultAction()

            body.eachRow { TableRow row, Map values ->
            }
        }

        def filters = table.filterParams
        table.body = securityService.listUser(filters, params)
        table.paginate = securityService.countUser(filters)

        display page: p
    }

    private String buildStatusLabel(Map status) {
        return """
            <div class="${status.color}" style="padding: 10px; margin-top: -18px; text-align: center; color: white;">
            <h1>${status.description}</h1>
            </div>
        """
    }

    private Map getCurrentStatus() {
        String status
        try {
            status = new Random().nextInt(9 - 0 + 1) + 0

        } catch (Exception ignore) {
            status = '0'
        }

        return getStatus(status)
    }

    private Map getStatus(String status) {
        return [
            status     : status,
            description: getStatusDescription(status),
            color      : 'bg-light',
        ]
    }

    private String getStatusDescription(String status) {
        Map descriptions = [
            '0': 'MARKER SPENTO, PREMI "RESET EMERGENZE"',
            '1': 'MARKER IN RISCALDAMENTO, ATTENDI...',
            '2': 'MARKER SPENTO, GIRA LA CHIAVE',
            '3': 'MARKER IN STANDBY',
            '4': 'MARKER PRONTO, CHIUDI IL COPERCHIO PER STAMPARE',
            '5': 'MARKER PRONTO A STAMPARE',
            '6': 'MARKER PRONTO, SHUTTER CHIUSO',
            '7': 'STAMPA IN CORSO',
            '8': 'STAMPA IN CORSO, SHUTTER CHIUSO',
            '9': 'MARKER SPENTO, RIMUOVI IL COPERCHIO',
            ':': 'ERRORE DI SISTEMA, USA INTERRUTTORE PRINCIPALE "ON/OFF"',
        ]

        return descriptions[status]
    }

}
