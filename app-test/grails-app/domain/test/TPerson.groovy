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
import java.time.LocalDateTime

class TPerson implements GormEntity, MultiTenant<TPerson> {

    Long id

    LocalDateTime dateCreated

    String name
    String address
    String postcode
    Money salary
    Quantity distanceKm
    LocalDate dateStart
    LocalDate dateEnd
    Boolean active
    String picture

    TCompany company
    static belongsTo = [
        company: TCompany,
    ]

    static embedded = [
        'salary',
        'distanceKm'
    ]

    static constraints = {
        picture nullable: true
        salary nullable: true
        distanceKm nullable: true
        dateStart nullable: true
        dateEnd nullable: true
    }

}
