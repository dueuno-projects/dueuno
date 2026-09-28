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
package dueuno.elements.pages

import dueuno.elements.components.Button
import dueuno.elements.components.Form
import dueuno.elements.components.Label
import dueuno.elements.components.Link
import dueuno.elements.controls.PasswordField
import dueuno.elements.controls.TextField
import dueuno.elements.core.KeyPress
import dueuno.elements.core.Page
import dueuno.elements.style.TextAlign
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class Login extends Page {

    Boolean autocomplete

    String copy
    String registerUrl
    String passwordRecoveryUrl

    String backgroundImage
    String logoImage

    KeyPress loginKeyPress
    Form form

    Login(Map args) {
        super(args)

        keyPress.enabled = false
        loginKeyPress = createComponent(KeyPress, 'loginKeyPress')

        autocomplete = (args.autocomplete == null) ? false : args.autocomplete

        copy = args.copy
        registerUrl = args.registerUrl
        passwordRecoveryUrl = args.passwordRecoveryUrl

        logoImage = args.logoImage
        backgroundImage = args.backgroundImage

        form = createComponent(Form, 'loginForm')
        form.with {
            addField(
                class: TextField,
                id: 'username',
                placeholder: 'authentication.username.placeholder',
                displayLabel: false,
            )
            addField(
                class: PasswordField,
                id: 'password',
                icon: '',
                placeholder: 'authentication.password.placeholder',
                displayLabel: false,
            )
            addField(
                class: Button,
                id: 'login',
                action: 'authenticate',
                submit: 'form',
                displayLabel: false,
                stretch: true,
                primary: true,
            )
            if (passwordRecoveryUrl) {
                addField(
                    class: Link,
                    id: 'passwordRecoveryLink',
                    url: passwordRecoveryUrl,
                    direct: false,
                    textAlign: TextAlign.CENTER,
                    displayLabel: false,
                    cssClass: 'w-100',
                )
            }
            if (registerUrl) {
                addField(
                    class: Button,
                    id: 'register',
                    url: registerUrl,
                    direct: false,
                    label: '',
                )
            }
            if (copy) {
                addField(
                    class: Label,
                    id: 'copy',
                    html: copy,
                    textAlign: TextAlign.CENTER,
                    displayLabel: false,
                )
            }
        }
    }
}
