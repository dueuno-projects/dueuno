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

import dueuno.types.Money
import dueuno.types.Quantity
import grails.gorm.MultiTenant
import org.grails.datastore.gorm.GormEntity

import java.time.LocalDate
import java.time.LocalTime

/**
 * Created by sartori on 01/08/16.
 */
class TDemo implements GormEntity, MultiTenant<TDemo> {

    Long id

    String textfield
    Integer numberfield
    Money moneyfield
    Quantity quantityfield
    LocalDate datefield
    LocalTime timefield
    Boolean checkbox

    String filename

    static embedded = [
        'moneyfield',
        'quantityfield',
    ]

    static constraints = {
        quantityfield nullable: true
        datefield nullable: true
        timefield nullable: true
        filename nullable: true
    }

}
