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
package dueuno.security

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity

/**
 * Renamed to 'user_role' to avoid conflicting with database keywords (DB2, H2 have problems with 'user' or 'role')
 *
 * @author Gianluca Sartori
 */

@GrailsCompileStatic
@EqualsAndHashCode(includes='authority')
@ToString(includes='authority', includeNames=true, includePackage=false)
class TRole implements GormEntity, Serializable {

    private static final long serialVersionUID = 1

    Long id

    String authority

    static constraints = {
        authority blank: false, unique: true
    }

    static mapping = {
        table 'sys_role'
        cache true
    }
}
