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
import dueuno.elements.ElementsException
import dueuno.elements.components.Button
import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.TextField
import dueuno.elements.style.TextDefault
import groovy.util.logging.Slf4j

@Slf4j
class HandledExceptionController implements ElementsController {

    def handleException(Exception e) {
        display exception: e
    }

    def handleElementsException(ElementsException e) {
        display exception: e
    }

    def index() {
        def c = createContent(ContentForm)
        c.header.removeNextButton()
        c.form.with {
            validate = HandleExceptionValidator
            addField(
                class: Button,
                id: 'e1',
                action: 'onExceptionMessage',
                cols: 12,
            )

            addField(
                class: TextField,
                id: 'text',
                label: TextDefault.FIND,
                placeholder: "Don't write",
                cols: 8,
            )
            addField(
                class: Button,
                id: 'e2',
                action: 'onExceptionError',
                submit: 'form',
                cols: 4,
            )
            addField(
                class: Button,
                id: 'e3',
                action: 'onExceptionOutputStream',
                submit: 'form',
                cols: 4,
            )
        }

        display content: c
    }

    def onExceptionMessage() {
        throw new ElementsException("handle.exception.message", ["World"])
    }

    def onExceptionError(HandleExceptionValidator val) {
        if (val.hasErrors()) {
            for (error in val.errors.getAllErrors()) {
                throw new ElementsException(error)
            }

        } else {
            throw new ElementsException("I told you not to write :)")
        }
    }

    def onExceptionOutputStream() {
        def image = linkPublicResource("images/acerola_small.png")
        def os = getDownloadOutputStream(image)
        os.flush()
        throw new Exception("There is a problem here...")
    }
}

