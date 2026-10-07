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
import dueuno.security.AuthenticationProviderType
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
    String googleLoginUrl
    List<Map> oidcProviders = []

    String backgroundImage
    String logoImage

    KeyPress loginKeyPress
    Form form

    Login(Map args) {
        super(args)

        keyPress.enabled = false
        Boolean physicalAuthenticationEnabled = args.physicalAuthenticationEnabled != null
            ? args.physicalAuthenticationEnabled
            : false
        loginKeyPress = createComponent(KeyPress, 'loginKeyPress', [enabled: physicalAuthenticationEnabled])

        autocomplete = (args.autocomplete == null) ? false : args.autocomplete

        copy = args.copy
        registerUrl = args.registerUrl
        passwordRecoveryUrl = args.passwordRecoveryUrl
        googleLoginUrl = args.googleLoginUrl
        oidcProviders = (args.oidcProviders ?: []) as List<Map>

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
            if (googleLoginUrl) {
                addField(
                    class: Button,
                    id: 'googleLogin',
                    url: googleLoginUrl,
                    direct: true,
                    label: 'authentication.google.login',
                    displayLabel: false,
                    stretch: true,
                )
            }
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
            for (Map provider in oidcProviders) {
                AuthenticationProviderType providerType = provider.providerType as AuthenticationProviderType
                addField(
                    class: Button,
                    id: "oidcLogin${providerType.name().capitalize()}",
                    url: provider.url as String,
                    direct: true,
                    text: provider.name as String,
                    icon: oidcProviderIcon(providerType),
                    backgroundColor: oidcProviderBackgroundColor(providerType),
                    textColor: '#ffffff',
                    displayLabel: false,
                    stretch: true,
                )
            }
        }
    }

    private static String oidcProviderIcon(AuthenticationProviderType providerType) {
        switch (providerType) {
            case AuthenticationProviderType.GOOGLE: return 'fa-brands fa-google'
            case AuthenticationProviderType.MS_ENTRA: return 'fa-brands fa-microsoft'
            case AuthenticationProviderType.SAP_IAS: return ''
            default: return 'fa-brands fa-openid'
        }
    }

    private static String oidcProviderBackgroundColor(AuthenticationProviderType providerType) {
        switch (providerType) {
            case AuthenticationProviderType.GOOGLE: return '#1a73e8'
            case AuthenticationProviderType.MS_ENTRA: return '#6f42c1'
            case AuthenticationProviderType.SAP_IAS: return '#c2410c'
            default: return '#334155'
        }
    }
}
