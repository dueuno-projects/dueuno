/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.elements.components

import dueuno.elements.core.Component
import dueuno.elements.core.Control
import grails.util.GrailsWebMockUtil
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

class FormSpec extends Specification {

    static class TestControl extends Control {

        String placeholder

        TestControl(Map args) {
            super(args)
            placeholder = args.placeholder ?: ''
        }

    }

    static class TestComponent extends Component {

        String title

        TestComponent(Map args) {
            super(args)
            title = args.title ?: ''
        }

    }

    def setup() {
        GrailsWebMockUtil.bindMockWebRequest()
    }

    def cleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    void configuresControlAndField() {
        given:
        Form form = new Form(id: 'form')
        TestControl configuredControl
        FormField configuredField

        when:
        FormField field = form.addField(TestControl, 'name') { TestControl control, FormField wrapper ->
            control.placeholder = 'Full name'
            control.defaultValue = 'Jane Doe'
            wrapper.label = 'Name'
            wrapper.help = 'Enter first and last name'
            wrapper.cols = 6

            configuredControl = control
            configuredField = wrapper
        }

        then:
        field.is(configuredField)
        field.component.is(configuredControl)
        form.getControl('name').is(configuredControl)
        configuredControl.placeholder == 'Full name'
        configuredControl.value == 'Jane Doe'
        field.label == 'Name'
        field.help == 'Enter first and last name'
        field.cols == ' col-sm-6 col-6'
    }

    void supportsComponents() {
        given:
        Form form = new Form(id: 'form')

        when:
        FormField field = form.addField(TestComponent, 'summary') { TestComponent component, FormField wrapper ->
            component.title = 'Summary'
            wrapper.displayLabel = false
        }

        then:
        field.component instanceof TestComponent
        form.getComponent('summary').is(field.component)
        (field.component as TestComponent).title == 'Summary'
        !field.displayLabel
    }

}
