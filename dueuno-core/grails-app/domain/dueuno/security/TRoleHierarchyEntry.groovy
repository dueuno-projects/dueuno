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
 * @author Gianluca Sartori
 */

@GrailsCompileStatic
@EqualsAndHashCode(includes='entry')
@ToString(includes='entry', includeNames=true, includePackage=false)
class TRoleHierarchyEntry implements GormEntity, Serializable {

    private static final long serialVersionUID = 1

    Long id

    String entry

    static constraints = {
        entry blank: false, unique: true
    }

    static mapping = {
        table 'sys_role_hierarchy_entry'
        cache true
    }
}
