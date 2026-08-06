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
import grails.gorm.DetachedCriteria
import groovy.transform.ToString
import org.codehaus.groovy.util.HashCodeHelper
import org.grails.datastore.gorm.GormEntity

/**
 * @author Gianluca Sartori
 */

@GrailsCompileStatic
@ToString(cache = true, includeNames = true, includePackage = false)
class TUserRoleGroup implements GormEntity, Serializable {

    private static final long serialVersionUID = 1

    TUser user
    TRoleGroup roleGroup

    static mapping = {
        table 'sys_user_role_group'
        id composite: ['roleGroup', 'user']
        version false
    }

    @Override
    boolean equals(other) {
        if (other instanceof TUserRoleGroup) {
            other.userId == user?.id && other.roleGroupId == roleGroup?.id
        }
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (user) {
            hashCode = HashCodeHelper.updateHash(hashCode, user.id)
        }
        if (roleGroup) {
            hashCode = HashCodeHelper.updateHash(hashCode, roleGroup.id)
        }
        hashCode
    }

    static TUserRoleGroup get(long userId, long roleGroupId) {
        criteriaFor(userId, roleGroupId).get() as TUserRoleGroup
    }

    static boolean exists(long userId, long roleGroupId) {
        criteriaFor(userId, roleGroupId).count()
    }

    private static DetachedCriteria criteriaFor(long userId, long roleGroupId) {
        TUserRoleGroup.where {
            user == TUser.load(userId) &&
                roleGroup == TRoleGroup.load(roleGroupId)
        }
    }

    static TUserRoleGroup create(TUser user, TRoleGroup roleGroup) {
        def instance = new TUserRoleGroup(user: user, roleGroup: roleGroup)
        instance.save(flush: true)
        instance
    }

    static boolean remove(TUser u, TRoleGroup rg) {
        if (u != null && rg != null) {
            TUserRoleGroup.where { user == u && roleGroup == rg }.deleteAll()
        }
    }

    static Number removeAll(TUser u) {
        u == null ? 0 : TUserRoleGroup.where { user == u }.deleteAll()
    }

    static Number removeAll(TRoleGroup rg) {
        rg == null ? 0 : TUserRoleGroup.where { roleGroup == rg }.deleteAll()
    }
}
