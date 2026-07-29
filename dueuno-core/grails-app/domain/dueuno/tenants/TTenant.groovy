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

import dueuno.core.TConnectionSource
import grails.compiler.GrailsCompileStatic
import org.grails.datastore.gorm.GormEntity

import java.time.LocalDateTime

/**
 * @author Gianluca Sartori
 */

@GrailsCompileStatic
class TTenant implements GormEntity, Serializable {

    private static final long serialVersionUID = 1

    Long id

    LocalDateTime dateCreated
    String tenantId
    String description
    String host
    Boolean deletable

    TConnectionSource connectionSource

    static constraints = {
        tenantId unique: true
        description nullable: true
        host nullable: true, unique: true
    }

    static mapping = {
        table 'sys_tenant'
    }

}
