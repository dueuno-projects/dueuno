/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package dueuno.security

import dueuno.application.AuthenticationProviderService
import dueuno.tenant.TTenant
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@Transactional
@CompileStatic
class AuthenticationIdentityService {

    AuthenticationProviderService authenticationProviderService

    @CompileDynamic
    Boolean hasIdentity(TUser user) {
        return user != null && TUserAuthenticationIdentity.countByUser(user) > 0
    }

    @CompileDynamic
    TAuthenticationProvider getProvider(Serializable providerId, Boolean superAdmin) {
        TAuthenticationProvider provider
        if (superAdmin) {
            provider = authenticationProviderService.get(providerId)

        } else {
            provider = authenticationProviderService.getForCurrentTenant(providerId)
        }

        if (!provider) { return null }
        if (!provider.providerType.isOidcProvider()) { return null }

        return provider
    }

    @CompileDynamic
    private DetachedCriteria<TUserAuthenticationIdentity> buildQuery(TAuthenticationProvider provider, Map filters = [:]) {
        DetachedCriteria<TUserAuthenticationIdentity> query = TUserAuthenticationIdentity.where {
            providerType == provider.providerType && user.tenant == provider.tenant
        }

        if (filters.containsKey('id')) { query = query.where { id == filters.id } }
        if (filters.containsKey('user')) { query = query.where { user.id == filters.user } }
        if (filters.containsKey('providerType')) { query = query.where { providerType == filters.providerType } }
        if (filters.issuer) { query = query.where { issuer =~ "%${filters.issuer}%" } }
        if (filters.subject) { query = query.where { subject =~ "%${filters.subject}%" } }

        return query
    }

    private Map getFetch() {
        return [user: 'join']
    }

    private Map getFetchAll() {
        return fetch + [:]
    }

    @CompileDynamic
    List<TUserAuthenticationIdentity> list(Serializable providerId, Boolean superAdmin, Map filterParams = [:], Map fetchParams = [:]) {
        TAuthenticationProvider provider = getProvider(providerId, superAdmin)
        if (!provider) {
            return []
        }

        if (!fetchParams.sort) {
            fetchParams.sort = [issuer: 'asc', subject: 'asc']
        }

        fetchParams.fetch = fetch
        DetachedCriteria<TUserAuthenticationIdentity> query = buildQuery(provider, filterParams)
        return query.list(fetchParams)
    }

    @CompileDynamic
    Number count(Serializable providerId, Boolean superAdmin, Map filterParams = [:]) {
        TAuthenticationProvider provider = getProvider(providerId, superAdmin)
        if (!provider) {
            return 0
        }

        DetachedCriteria<TUserAuthenticationIdentity> query = buildQuery(provider, filterParams)
        return query.count()
    }

    @CompileDynamic
    TUserAuthenticationIdentity get(Serializable id, Serializable providerId, Boolean superAdmin) {
        TAuthenticationProvider provider = getProvider(providerId, superAdmin)
        if (!provider) {
            return null
        }

        DetachedCriteria<TUserAuthenticationIdentity> query = buildQuery(provider, [id: id])
        return query.get(fetch: fetchAll) as TUserAuthenticationIdentity
    }

    @CompileDynamic
    List<TUser> listUsers(Serializable providerId, Boolean superAdmin) {
        TAuthenticationProvider provider = getProvider(providerId, superAdmin)
        if (!provider) {
            return []
        }

        DetachedCriteria<TUser> query = TUser.where { tenant == provider.tenant }
        return query.list(sort: 'username', order: 'asc', fetch: [tenant: 'join'])
    }

    @CompileDynamic
    TUserAuthenticationIdentity create(Map args, Serializable providerId, Boolean superAdmin) {
        TAuthenticationProvider provider = getProvider(providerId, superAdmin)
        if (!provider) {
            return null
        }

        TUser user = getProviderUser(args.user as Serializable, provider.tenant)
        TUserAuthenticationIdentity identity = new TUserAuthenticationIdentity(
            user: user,
            providerType: provider.providerType,
            issuer: args.issuer,
            subject: args.subject,
        )

        identity.validate()
        if (!identity.hasErrors()) {
            identity.save(flush: true)
        }

        return identity
    }

    @CompileDynamic
    TUserAuthenticationIdentity update(Map args, Serializable providerId, Boolean superAdmin) {
        TUserAuthenticationIdentity identity = get(args.id as Serializable, providerId, superAdmin)
        if (!identity) {
            return null
        }

        identity.user = getProviderUser(args.user as Serializable, identity.user.tenant)
        identity.issuer = args.issuer
        identity.subject = args.subject

        identity.validate()
        if (!identity.hasErrors()) {
            identity.save(flush: true)
        }

        return identity
    }

    @CompileDynamic
    void delete(Serializable id, Serializable providerId, Boolean superAdmin) {
        TUserAuthenticationIdentity identity = get(id, providerId, superAdmin)
        if (identity) {
            identity.delete(flush: true)
        }
    }

    @CompileDynamic
    private TUser getProviderUser(Serializable userId, TTenant tenant) {
        if (!userId) {
            return null
        }

        return TUser.findByIdAndTenant(userId, tenant)
    }

}
