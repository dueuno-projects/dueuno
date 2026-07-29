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
import org.grails.datastore.gorm.GormEntity

/**
 * Renamed to 'user_account' to avoid conflicting with database keywords (DB2, H2 have problems with 'user' or 'role')
 *
 * @author Gianluca Sartori
 */

@GrailsCompileStatic
@EqualsAndHashCode(includes='username')
class TUser implements GormEntity, Serializable {

    private static final long serialVersionUID = 1

    Long id

    TTenant tenant
    String apiKey

    String username
    String password
    String externalId
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

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
        defaultGroup nullable: true
        password blank: false, password: true
        username blank: false, unique: true
        apiKey nullable: true, unique: true
        externalId nullable: true, unique: true
        firstname nullable: true
        lastname nullable: true
        email nullable: true, email: true
        telephone nullable: true
        note nullable: true, maxSize: 2000
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
