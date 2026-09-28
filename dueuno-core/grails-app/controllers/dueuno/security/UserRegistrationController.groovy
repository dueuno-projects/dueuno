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
package dueuno.security

import dueuno.elements.ElementsController
import dueuno.elements.components.Button
import dueuno.elements.components.Label
import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.EmailField
import dueuno.elements.pages.Login
import dueuno.types.Type
import grails.plugin.springsecurity.annotation.Secured

/**
 * @author Gianluca Sartori
 */
@Secured(['permitAll'])
class UserRegistrationController implements ElementsController {

    SecurityService securityService

    def reset() {
        def c = createContent(ContentForm)
        c.header.nextButton.action = 'onPasswordReset'
        c.form.with {
            validate = UserRegistrationPasswordResetValidator
            addField(
                class: Label,
                id: 'emailReset',
                html: """
                    Inserisci il tuo indirizzo email, se sei presente nei nostri database ti invieremo una email
                    con il link per reimpostare la password.
                    """
            )
            addField(
                class: EmailField,
                id: 'email',
            )
        }

        display content: c, modal: true
    }

    def onReset(UserRegistrationPasswordResetValidator val) {
        if (val.hasErrors()) {
            display errors: val
            return
        }

        securityService.resetPassword(params.email)

        def c = createContent(ContentForm)
        c.header.removeNextButton()
        c.form.with {
            addField(
                class: Label,
                id: 'confirmMessage',
                html: """
                        Ti abbiamo inviato una email per resettare la password, segui le istruzioni riportate
                        (se non la trovi potrebbe essere nella cartella di spam).
                    """,
            )
        }
        display content: c, modal: true
    }

    def createNew() {
        def c = createContent(ContentForm)
        c.header.nextButton.action = 'onCreateNewPassword'
        c.form.with {
            validate = UserRegistrationNewPasswordValidator
            addKeyField('username', Type.STRING)
            addField(
                class: Label,
                id: 'newPassword',
                html: """
                    Scegli la nuova password
                    """
            )
            addField(
                class: 'PasswordField',
                id: 'password',
            )
            addField(
                class: 'PasswordField',
                id: 'confermaPassword',
            )
        }

        display content: c, modal: true
    }

    def onCreateNew(UserRegistrationNewPasswordValidator val) {
        if (val.hasErrors()) {
            display errors: val
            return
        }

        TUser user = securityService.updateUser(
            username: params.username,
            password: params.password,
        )
        if (user.hasErrors()) {
            display errors: user
            return
        }

        def c = createContent(ContentForm)
        c.header.removeNextButton()
        c.form.with {
            addField(
                class: 'Label',
                id: 'confirmMessage',
                html: """
                        Password modificata, per accedere al sito eseguire nuovamente il login.
                    """,
            )
            addField(
                class: Button,
                id: 'login',
                controller: 'authentication',
                action: 'login',
                stretch: true,
            )
        }

        def p = createPage(Login)
        display page: p, content: c, modal: true
    }
}


