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
package dueuno.elements.core

import dueuno.elements.components.Button
import dueuno.elements.controls.TextField
import dueuno.elements.style.TextDefault
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class PageMessageBox extends Component {

    Button confirm
    Button cancel
    TextField verify

    PageMessageBox(Map args) {
        super(args)

        viewPath = '/dueuno/elements/'

        cancel = addComponent(Button, 'cancel')
        cancel.icon = 'fa-circle-xmark'
        cancel.text = TextDefault.CANCEL
        cancel.stretch = false

        verify = addComponent(TextField, 'verify')
        verify.placeholder = 'messagebox.confirm.verify'

        confirm = addComponent(Button, 'confirm')
        confirm.text = TextDefault.OK
        confirm.icon = 'fa-circle-check'
        confirm.primary = true
        confirm.stretch = false
    }

}
