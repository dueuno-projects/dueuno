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

import dueuno.elements.WebRequestAware
import dueuno.tenant.TenantService
import dueuno.tenant.TTenant
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GormUserDetailsService
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import jakarta.inject.Inject
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
 * Loads the local user identified by the request tenant and username.
 */
@CompileStatic
class TenantUserDetailsService extends GormUserDetailsService implements WebRequestAware {

    @Inject
    TenantService tenantService

    @Override
    @CompileDynamic
    @Transactional(readOnly = true, noRollbackFor = [IllegalArgumentException, UsernameNotFoundException])
    UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {
        TTenant tenant = SecurityService.USERNAME_SUPERADMIN.equalsIgnoreCase(username)
            ? tenantService.getByTenantId(TenantService.defaultTenantId)
            : resolveTenant()
        if (!tenant) {
            throw new NoStackUsernameNotFoundException()
        }

        def conf = SpringSecurityUtils.securityConfig
        TUser user = TUser.createCriteria().get {
            eq('tenant', tenant)
            if (conf.userLookup.usernameIgnoreCase) {
                eq('username', username, [ignoreCase: true])
            } else {
                eq('username', username)
            }
        } as TUser

        if (!user) {
            throw new NoStackUsernameNotFoundException()
        }

        Collection<GrantedAuthority> authorities = loadAuthorities(user, username, loadRoles)
        return createUserDetails(user, authorities)
    }

    @CompileDynamic
    private TTenant resolveTenant() {
        TTenant tenant
        if (hasRequest()) {
            String host = request.getHeader('host')
            if (host) {
                tenant = TTenant.findByHost(host)
            }
        }

        if (!tenant) {
            String tenantId = tenantService.currentTenantId ?: TenantService.defaultTenantId
            tenant = tenantService.getByTenantId(tenantId)
        }

        return tenant
    }
}
