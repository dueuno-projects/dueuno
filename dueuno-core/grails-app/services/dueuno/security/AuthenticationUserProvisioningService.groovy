/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
 * Loads or creates the local user associated with an authenticated identity.
 */
@CompileStatic
class AuthenticationUserProvisioningService implements WebRequestAware {

    TenantService tenantService
    SecurityService securityService
    UserDetailsService userDetailsService

    @Transactional
    @CompileDynamic
    UserDetails ensureUser(String username, Map profile = [:]) {
        if (!username) throw new UsernameNotFoundException('Authenticated identity has no username')

        String firstname = (profile.firstname as String)?.trim()
        String lastname = (profile.lastname as String)?.trim()
        if (!firstname) firstname = null
        if (!lastname) lastname = null

        String host = hasRequest() ? request.getHeader('host') : null
        String tenantId = tenantService.getByHost(host)?.tenantId ?: tenantService.defaultTenantId
        UserDetails details
        tenantService.withTenant(tenantId) {
            try {
                details = userDetailsService.loadUserByUsername(username)
            } catch (UsernameNotFoundException ignored) {
                securityService.createUser(
                    tenantId: tenantId,
                    username: username,
                    password: securityService.generatePassword(),
                    firstname: firstname,
                    lastname: lastname,
                    email: profile.email,
                    failOnError: true,
                )
                details = userDetailsService.loadUserByUsername(username)
            }
        }
        return details
    }

}
