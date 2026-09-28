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
package dueuno.application

import grails.compiler.GrailsCompileStatic
import org.grails.datastore.gorm.GormEntity

/**
 * @author Gianluca Sartori
 */

@GrailsCompileStatic
class TConnectionSource implements GormEntity, Serializable {

    private static final long serialVersionUID = 1

    Long id

    Boolean tenant   // true if used by a tenant
    Boolean embedded // true if configured in application.yml (cannot be changed at runtime)
    String name
    String driverClassName
    String dialect
    String dbCreate
    Boolean readOnly
    String url
    String username
    String password

    static constraints = {
        name unique: true
        dialect nullable: true
        url unique: true
        password nullable: true
    }

    static mapping = {
        table 'sys_connection_source'
    }

}
