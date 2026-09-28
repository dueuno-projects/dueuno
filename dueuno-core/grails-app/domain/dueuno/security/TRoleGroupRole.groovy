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
class TRoleGroupRole implements GormEntity, Serializable {

    private static final long serialVersionUID = 1

    TRoleGroup roleGroup
    TRole role

    static mapping = {
        table 'sys_role_group_role'
        id composite: ['roleGroup', 'role']
        version false
    }

    @Override
    boolean equals(other) {
        if (other instanceof TRoleGroupRole) {
            other.roleId == role?.id && other.roleGroupId == roleGroup?.id
        }
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (roleGroup) {
            hashCode = HashCodeHelper.updateHash(hashCode, roleGroup.id)
        }
        if (role) {
            hashCode = HashCodeHelper.updateHash(hashCode, role.id)
        }
        hashCode
    }

    static TRoleGroupRole get(long roleGroupId, long roleId) {
        criteriaFor(roleGroupId, roleId).get() as TRoleGroupRole
    }

    static boolean exists(long roleGroupId, long roleId) {
        criteriaFor(roleGroupId, roleId).count()
    }

    private static DetachedCriteria criteriaFor(long roleGroupId, long roleId) {
        TRoleGroupRole.where {
            roleGroup == TRoleGroup.load(roleGroupId) &&
                role == TRole.load(roleId)
        }
    }

    static TRoleGroupRole create(TRoleGroup roleGroup, TRole role) {
        def instance = new TRoleGroupRole(roleGroup: roleGroup, role: role)
        instance.save(flush: true)
        instance
    }

    static boolean remove(TRoleGroup rg, TRole r) {
        if (rg != null && r != null) {
            TRoleGroupRole.where { roleGroup == rg && role == r }.deleteAll()
        }
    }

    static Number removeAll(TRole r) {
        r == null ? 0 : TRoleGroupRole.where { role == r }.deleteAll()
    }

    static Number removeAll(TRoleGroup rg) {
        rg == null ? 0 : TRoleGroupRole.where { roleGroup == rg }.deleteAll()
    }
}
