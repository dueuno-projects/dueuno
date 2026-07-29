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


import dueuno.tenants.TTenant
import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity

/**
 * @author Gianluca Sartori
 */

@GrailsCompileStatic
@EqualsAndHashCode(includes='name')
@ToString(includes='name', includeNames=true, includePackage=false)
class TRoleGroup implements GormEntity, Serializable {

    private static final long serialVersionUID = 1

    Long id

    TTenant tenant

    String name
    Boolean deletable = false
    String landingPage

    static constraints = {
        name blank: false, unique: ['tenant']
        landingPage nullable: true
    }

    static mapping = {
        table 'sys_role_group'
        cache true
    }

    List<TRole> getAuthorities() {
        TRoleGroupRole.findAllByRoleGroup(this)*.role
    }
}
