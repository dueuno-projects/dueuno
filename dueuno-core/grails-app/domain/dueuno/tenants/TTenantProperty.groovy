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
package dueuno.tenants

import dueuno.core.TApplicationProperty
import dueuno.properties.PropertyType
import grails.compiler.GrailsCompileStatic
import grails.gorm.MultiTenant
import org.grails.datastore.gorm.GormEntity

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * @author Gianluca Sartori
 */

// KEEP ALIGNED WITH TApplicationProperty
@GrailsCompileStatic
class TTenantProperty implements GormEntity, MultiTenant<TTenantProperty> {

    Long id

    String name
    PropertyType type

    String validation

    Boolean bool
    Boolean boolDefault
    BigDecimal number
    BigDecimal numberDefault

    LocalDateTime datetime
    LocalDateTime datetimeDefault
    LocalDate date
    LocalDate dateDefault
    LocalTime time
    LocalTime timeDefault

    String string
    String stringDefault
    String filename
    String filenameDefault
    String directory
    String directoryDefault
    String url
    String urlDefault

    String password

    static constraints = {
        importFrom TApplicationProperty
    }

    static mapping = {
        table 'sys_tenant_property'
    }

}
