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
package dueuno.elements.controls

import dueuno.elements.components.Form
import dueuno.elements.components.FormField
import dueuno.types.QuantityUnit
import dueuno.types.Type
import grails.core.DefaultGrailsApplication
import grails.util.GrailsWebMockUtil
import grails.util.Holders
import org.springframework.context.support.GenericApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import org.grails.core.support.GrailsApplicationDiscoveryStrategy
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalTime

class ControlConfigurationSpec extends Specification {

    GenericApplicationContext applicationContext

    def setup() {
        GrailsWebMockUtil.bindMockWebRequest()

        applicationContext = new GenericApplicationContext()
        applicationContext.refresh()
        DefaultGrailsApplication application = new DefaultGrailsApplication()
        application.mainContext = applicationContext
        Holders.addApplicationDiscoveryStrategy([
            findGrailsApplication: { application },
            findApplicationContext: { applicationContext },
        ] as GrailsApplicationDiscoveryStrategy)
    }

    def cleanup() {
        RequestContextHolder.resetRequestAttributes()
        Holders.clear()
        applicationContext.close()
    }

    void configuresDerivedPropertiesAfterConstruction() {
        given:
        DateTimeField dateTimeField = new DateTimeField(id: 'dateTime')
        NumberField numberField = new NumberField(id: 'number')
        MoneyField moneyField = new MoneyField(id: 'money')
        QuantityField quantityField = new QuantityField(id: 'quantity')
        HiddenField hiddenField = new HiddenField(id: 'hidden')

        when:
        dateTimeField.min = LocalDate.of(2026, 1, 2)
        dateTimeField.max = LocalTime.of(18, 30)
        numberField.decimals = 3
        moneyField.currency = 'USD'
        quantityField.availableUnits = [QuantityUnit.KG, QuantityUnit.G]
        hiddenField.value = LocalDate.of(2026, 1, 2)

        then:
        dateTimeField.min.toLocalDate() == LocalDate.of(2026, 1, 2)
        dateTimeField.max.toLocalTime() == LocalTime.of(18, 30)
        numberField.inputMode == 'decimal'
        moneyField.currency == 'USD'
        quantityField.availableUnits == [QuantityUnit.KG, QuantityUnit.G]
        quantityField.defaultUnit == QuantityUnit.KG
        hiddenField.valueType == Type.DATE.toString()
    }

    void configuresPrettyPrintingAndOptionsAfterConstruction() {
        given:
        NumberField numberField = new NumberField(id: 'number')
        Select select = new Select(id: 'select')
        MultipleCheckbox multipleCheckbox = new MultipleCheckbox(id: 'multipleCheckbox')

        when:
        numberField.renderTextPrefix = true
        numberField.textPrefix = 'invoice.amount'
        numberField.textArgs = ['2026']
        select.options = [
            [draft: 'Draft'],
            [final: 'Final'],
        ]
        select.multiple = true
        multipleCheckbox.optionsFromList = ['read', 'write']
        multipleCheckbox.readonly = true

        then:
        numberField.renderTextPrefix
        numberField.textPrefix == 'invoice.amount'
        numberField.textArgs == ['2026']
        select.options == [
            [draft: 'Draft'],
            [final: 'Final'],
        ]
        select.multiple
        multipleCheckbox.checkboxes.keySet() == ['read', 'write'] as Set
        multipleCheckbox.readonly
        multipleCheckbox.checkboxes.values().every { it.readonly }
    }

    void configuresOptionControlThroughFormApi() {
        given:
        Form form = new Form(id: 'form')

        when:
        FormField field = form.addField(Select, 'status') { Select control, FormField wrapper ->
            control.options = [
                [draft: 'Draft'],
                [final: 'Final'],
            ]
            control.multiple = true
            wrapper.cols = 6
        }

        then:
        field.component instanceof Select
        (field.component as Select).options == [
            [draft: 'Draft'],
            [final: 'Final'],
        ]
        (field.component as Select).multiple
        field.cols == ' col-sm-6 col-6'
    }

}
