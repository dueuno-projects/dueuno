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

import dueuno.elements.components.Button
import dueuno.elements.components.Form
import dueuno.elements.components.Separator
import dueuno.elements.controls.Checkbox
import dueuno.elements.controls.Select
import dueuno.elements.controls.TextField
import dueuno.elements.ElementsController
import dueuno.elements.TransitionService
import dueuno.security.SecurityService

class WebsocketController implements ElementsController {

    TransitionService transitionService
    SecurityService securityService

    def index() {
        def c = createContent()
        c.header.removeNextButton()
        def form = c.addComponent(Form)
        form.with {
            addField(
                    class: Separator,
                    id: 'info',
                    text: 'websocket.info',
            )
            addField(
                    class: Select,
                    id: 'queue',
                    optionsFromList: ['channel', 'user'],
                    renderTextPrefix: false,
            )
            addField(
                    class: TextField,
                    id: 'channel',
                    defaultValue: 'macchina-1',
            )
            addField(
                    class: Select,
                    id: 'usr',
                    optionsFromRecordset: securityService.listAllUser(),
                    keys: ['username'],
                    renderTextPrefix: false,
                    defaultValue: securityService.currentUsername,
            )
            addField(
                    class: TextField,
                    id: 'contr',
                    defaultValue: 'form',
            )
            addField(
                    class: TextField,
                    id: 'acti',
            )
            addField(
                    class: Checkbox,
                    id: 'moda',
                    defaultValue: true,
            )
            addField(
                    class: Button,
                    id: 'send',
                    action: 'onSend',
                    submit: 'form',
                    stretch: true,
            )
        }

        display content: c
    }

    def onSend() {
        def t = transitionService.createTransition()
        t.redirect(
                controller: params.contr,
                action: params.acti,
                modal: params.moda,
        )
        t.setValue('form.modal', true)
        t.setValue('form.large', true)
        t.setValue('form.closeButton', true)

        if (params.queue == 'user')
            transitionService.send(params.usr, t)
        else
            transitionService.publish(params.channel, t)

        display
    }
}
