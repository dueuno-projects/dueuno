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

import dueuno.tenant.TTenant
import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import org.grails.datastore.gorm.GormEntity

/**
 * @author Gianluca Sartori
 */

@GrailsCompileStatic
@EqualsAndHashCode(includes = 'username')
class TUser implements GormEntity, Serializable {

    private static final long serialVersionUID = 1

    Long id

    TTenant tenant
    String username
    String password

    Boolean enabled
    Boolean accountExpired
    Boolean accountLocked
    Boolean passwordExpired

    String apiKey
    String physicalId

    // System fields
    Boolean deletable
    TRoleGroup defaultGroup
    String note

    // User data
    String firstname
    String lastname
    String language
    String email
    String telephone

    // Session data
    Integer sessionDuration
    Integer rememberMeDuration

    // Localization preferences
    String decimalFormat // 'ISO_COM' (# ###,#) or 'ISO_DOT' (# ###.#)
    Boolean prefixedUnit
    Boolean symbolicCurrency
    Boolean symbolicQuantity
    Boolean invertedMonth
    Boolean twelveHours
    Boolean firstDaySunday

    // UI preferences
    Integer fontSize
    String guiStyle // 'ROUNDED' or 'SQUARED'
    Boolean animations

    static constraints = {
        tenant nullable: false
        username nullable: false, blank: false, unique: true
        password nullable: false, blank: false, password: true
        apiKey unique: true
        physicalId unique: true
        email email: true
        note maxSize: 1000

        sessionDuration nullable: false
        rememberMeDuration nullable: false

        fontSize nullable: false
        guiStyle nullable: false
    }

    static mapping = {
        table 'sys_user'
        password column: '`password`'
    }

    // Alias for the required getAuthorities() that better fits our naming structure
    List<TRoleGroup> getGroups() {
        getAuthorities()
    }

    List<TRoleGroup> getAuthorities() {
        TUserRoleGroup.findAllByUser(this)*.roleGroup
    }

    String getFullname() {
        if (firstname || lastname) {
            return "${firstname ?: ''}${lastname ? ' ' + lastname : ''}"
        }

        return username
    }
}
