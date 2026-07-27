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

import dueuno.commons.utils.SqlUtils
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentCreate
import dueuno.elements.contents.ContentEdit
import dueuno.elements.contents.ContentTable
import dueuno.elements.controls.*
import dueuno.core.ApplicationService
import dueuno.elements.ElementsController
import dueuno.types.Money
import dueuno.types.Quantity
import dueuno.types.QuantityUnit
import dueuno.types.Type
import jakarta.annotation.PostConstruct

import java.time.LocalDate

class CrudSqlController implements ElementsController {

    ApplicationService applicationService

    def dataSource

    String tblPerson = 'person'
    String tblCompany = 'company'

    @PostConstruct
    void init() {
//        applicationService.registerPrettyPrinter('COMPANY', '${it.name} (${it.date_created.toLocalDate().year})')
        applicationService.registerPrettyPrinter('COMPANY', '${it.name}')
        applicationService.registerTransformer('COMPANY') { value ->
            Map r = SqlUtils.get(dataSource, tblCompany, [id: value])
            return r.name
        }
        applicationService.registerTransformer('2MONEY') { value ->
            if (!value || !value[0]) return null
            return new Money(value[0], value[1])
        }
        applicationService.registerTransformer('2QUANTITY') { value ->
            if (!value || !value[0]) return null
            return new Quantity(value[0], value[1] as QuantityUnit)
        }
    }

    def index() {
        applicationService.registerPrettyPrinter('COMPANY', '${it.name}')
        def c = createContent(ContentTable)
        c.table.with {
            filters.with {
                addField(
                        class: Select,
                        id: 'company_id',
                        optionsFromRecordset: SqlUtils.list(dataSource, tblCompany),
                        keys: ['id'],
                        prettyPrinter: 'COMPANY',
                        renderTextPrefix: false,
                        cols: 4,
                )
                addField(
                        class: TextField,
                        id: 'name',
                        help: 'Hai bisogno di aiuto? Inserisci oil nome dell\'azienda! :)',
                        cols: 8,
                )
            }

            rowStriped = true
            rowHighlight = false
            stickyHeader = true
            sortable = [
                    name: 'asc',
                    address: 'desc',
            ]
            columns = [
                    'company_id',
                    'name',
                    'picture',
                    'address',
                    'postcode',
                    'salary_amount',
//                    'distance_km_amount',
//                    'date_start',
//                    'date_end',
//                    'active',
            ]
            transformers = [
                    company_id: 'COMPANY',
            ]
            prettyPrinters = [
                    company_id: 'COMPANY',
            ]

            body.eachRow { TableRow row, Map values ->
                values.salary_amount = transform('2MONEY', [values.salary_amount, values.salary_currency])
                values.distance_km_amount = transform('2QUANTITY', [values.distance_km_amount, values.distance_km_unit])
            }
        }

        c.table.body = SqlUtils.list(
                dataSource,
                tblPerson,
                ['name'],
                c.table.filterParams,
                c.table.fetchParams,
        )
        c.table.paginate = SqlUtils.count(dataSource, tblPerson, c.table.filterParams)
        display content: c
    }

    private buildForm(Map args = [:]) {
        applicationService.registerTransformer('MONEY') { value ->
            if (value == null) return null
            return new Money((value as String).replace(',', '.') as BigDecimal)
        }

        def c = args.create
                ? createContent(ContentCreate)
                : createContent(ContentEdit)

        c.form.with {
            validate = CrudRestValidator
            addKeyField('version', 0)
            addKeyField('salary_currency', Type.TEXT, 'EUR')
            addKeyField('distance_km_unit', Type.TEXT,'KM')
            addField(
                    class: Select,
                    id: 'company_id',
                    optionsFromRecordset: SqlUtils.list(dataSource, tblCompany),
                    prettyPrinter: 'COMPANY',
            )
            addField(
                    class: MoneyField,
                    id: 'prezzo',
                    transformer: 'MONEY',
                    defaultValue: '3,3'
            )
            addField(
                    class: TextField,
                    id: 'name',
            )
            addField(
                    class: TextField,
                    id: 'address',
            )
            addField(
                    class: NumberField,
                    id: 'postcode',
            )
            addField(
                    class: NumberField,
                    id: 'salary_amount',
            )
            addField(
                    class: NumberField,
                    id: 'distance_km_amount',
            )
            addField(
                    class: DateField,
                    id: 'date_start',
            )
            addField(
                    class: DateField,
                    id: 'date_end',
            )
            addField(
                    class: Checkbox,
                    id: 'active',
            )
            addField(
                    class: Upload,
                    id: 'picture',
            )
        }
        return c
    }

    private static Map cleanupValues(Map values) {
        // Excluding Grails params
        values.remove('controller')
        values.remove('action')
        values.remove('id')

        // Excluding Command properties
        values.remove('constraintsMap')
        values.remove('errors')
        values.remove('class')

        return values
    }

    def create() {
        def c = buildForm(create: true)
        display content: c, modal: true
    }

    def onCreate(CrudSqlValidator val) {
        if (val.hasErrors()) {
            display errors: val
            return
        }

        try {
            def results = SqlUtils.create(
                    dataSource,
                    tblPerson,
                    cleanupValues(val.properties + [date_created: LocalDate.now()])
            )
            display action: 'index'

        } catch (e) {
            e.printStackTrace()
            display exception: e
        }
    }

    def edit() {
        def c = buildForm(create: false)
        c.form.values = SqlUtils.get(dataSource, tblPerson, [id: params.id])
        c.form['prezzo'].value = new Money(5.1)
        display content: c, modal: true
    }

    def onEdit(CrudSqlValidator val) {
        if (val.hasErrors()) {
            display errors: val
            return
        }

        try {
            SqlUtils.update(
                    dataSource,
                    tblPerson,
                    [id: params.id],
                    cleanupValues(val.properties)
            )
            display action: 'index'

        } catch (e) {
            e.printStackTrace()
            display exception: e
        }
    }

    def onDelete() {
        try {
            SqlUtils.delete(dataSource, tblPerson, [id: params.id])
            display action: 'index'

        } catch (e) {
            e.printStackTrace()
            display exception: e
        }
    }
}
