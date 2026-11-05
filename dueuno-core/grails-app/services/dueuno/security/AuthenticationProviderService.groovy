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
import dueuno.tenant.TTenant
import dueuno.tenant.TenantService
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.contracts.Requires
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.security.authentication.AuthenticationProvider

/**
 * Manages tenant-specific authentication providers and their runtime order.
 */
@Transactional
@CompileStatic
class AuthenticationProviderService implements WebRequestAware {

    TenantService tenantService

    void tenantInstall(String tenantId) {
        TTenant tenant = tenantService.getByTenantId(tenantId)
        if (!tenant) throw new IllegalArgumentException("Tenant '${tenantId}' does not exist")
        installDefaults(tenant)
    }

    @CompileDynamic
    private void installDefaults(TTenant tenant) {
        List<Map> defaults = [
            [
                providerType      : AuthenticationProviderType.LOCAL,
                sequence          : 1,
                enabled           : true,
                sessionDuration   : 5,
                canChangePassword : true,
            ],
            [
                providerType: AuthenticationProviderType.PHYSICAL,
                sequence    : 2,
                enabled     : false,
            ],
            [
                providerType         : AuthenticationProviderType.LDAP,
                sequence             : 3,
                enabled              : false,
                searchFilter         : '(sAMAccountName={0})',
                searchSubtree        : true,
                retrieveGroupRoles   : true,
                groupSearchFilter    : '(member={0})',
                groupRoleAttribute   : 'cn',
                retrieveDatabaseRoles: true,
                defaultRole          : 'ROLE_USER',
            ],
            [
                providerType              : AuthenticationProviderType.GOOGLE,
                sequence                  : 4,
                enabled                   : false,
                discoveryUri              : 'https://accounts.google.com/.well-known/openid-configuration',
                clientAuthenticationMethod: 'client_secret_post',
                scopes                    : 'openid email profile',
            ],
            [
                providerType              : AuthenticationProviderType.MS_ENTRA,
                sequence                  : 5,
                enabled                   : false,
                discoveryUri              : 'https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration',
                clientAuthenticationMethod: 'client_secret_post',
                scopes                    : 'openid email profile',
            ],
            [
                providerType              : AuthenticationProviderType.SAP_IAS,
                sequence                  : 6,
                enabled                   : false,
                clientAuthenticationMethod: 'client_secret_basic',
                scopes                    : 'openid email profile',
            ],
            [
                providerType              : AuthenticationProviderType.OIDC,
                sequence                  : 7,
                enabled                   : false,
                clientAuthenticationMethod: 'client_secret_basic',
                scopes                    : 'openid email profile',
            ],
            [
                providerType       : AuthenticationProviderType.REMEMBERME,
                sequence           : 99,
                enabled            : true,
                rememberMeDuration: 10080, // 7 days
            ],
        ]

        for (Map values in defaults) {
            AuthenticationProviderType providerType = values.providerType as AuthenticationProviderType
            TAuthenticationProvider provider = TAuthenticationProvider.findByTenantAndProviderType(tenant, providerType)
            if (!provider) {
                provider = new TAuthenticationProvider(values + [tenant: tenant])
            } else {
                provider.sequence = values.sequence as Integer
                if (!provider.discoveryUri && values.discoveryUri) {
                    provider.discoveryUri = values.discoveryUri as String
                }
                if (!provider.clientAuthenticationMethod && values.clientAuthenticationMethod) {
                    provider.clientAuthenticationMethod = values.clientAuthenticationMethod as String
                }
                if (provider.sessionDuration == null && values.sessionDuration != null) {
                    provider.sessionDuration = values.sessionDuration as Integer
                }
                if (provider.canChangePassword == null && values.canChangePassword != null) {
                    provider.canChangePassword = values.canChangePassword as Boolean
                }
                if (provider.rememberMeDuration == null && values.rememberMeDuration != null) {
                    provider.rememberMeDuration = values.rememberMeDuration as Integer
                }
            }
            provider.save(flush: true, failOnError: true)
        }
    }

    @CompileDynamic
    private DetachedCriteria<TAuthenticationProvider> buildQuery(Map filterParams = [:]) {
        DetachedCriteria<TAuthenticationProvider> query = TAuthenticationProvider.where {}

        if (filterParams.containsKey('id')) query = query.where { id == filterParams.id }
        if (filterParams.containsKey('tenant')) query = query.where { tenant.id == filterParams.tenant }

        return query
    }

    private Map getFetchAll() {
        return [
            tenant                  : 'join',
            authenticationIdentities: 'join',
        ]
    }

    private Map getFetch() {
        return [tenant: 'join']
    }

    List<TAuthenticationProvider> list(Map filterParams = [:], Map fetchParams = [:]) {
        TTenant tenant = getRequestTenant()
        if (!tenant) return []

        if (!fetchParams.sort) fetchParams.sort = [sequence: 'asc']
        if (!fetchParams.fetch) fetchParams.fetch = fetch

        DetachedCriteria<TAuthenticationProvider> query = buildQuery(filterParams + [tenant: tenant.id])
        return query.list(fetchParams)
    }

    List<TAuthenticationProvider> listAll(Map filterParams = [:], Map fetchParams = [:]) {
        if (filterParams.tenant && !tenantService.get(filterParams.tenant as Serializable)) return []

        if (!fetchParams.sort) fetchParams.sort = [id: 'asc']
        if (!fetchParams.fetch) fetchParams.fetch = fetch

        DetachedCriteria<TAuthenticationProvider> query = buildQuery(filterParams)
        return query.list(fetchParams)
    }

    TAuthenticationProvider get(Serializable id) {
        return buildQuery(id: id).get(fetch: fetchAll) as TAuthenticationProvider
    }

    TAuthenticationProvider getForCurrentTenant(Serializable id) {
        TTenant tenant = getRequestTenant()
        if (!tenant) return null
        return buildQuery(id: id, tenant: tenant.id).get(fetch: fetchAll) as TAuthenticationProvider
    }

    TAuthenticationProvider getByProviderType(AuthenticationProviderType providerType) {
        TTenant tenant = getRequestTenant()
        if (!tenant) return null
        return getByTenantAndProviderType(tenant, providerType)
    }

    @CompileDynamic
    TAuthenticationProvider getByTenantAndProviderType(TTenant tenant, AuthenticationProviderType providerType) {
        if (!tenant) return null
        return TAuthenticationProvider.findByTenantAndProviderType(tenant, providerType)
    }

    List<AuthenticationProvider> getEnabledProviders(Map<String, ?> beans) {
        List<AuthenticationProvider> providers = []
        for (TAuthenticationProvider provider in list()) {
            if (!provider.enabled) continue

            Object bean = beans[provider.providerType.authenticationProviderBeanName]
            if (bean instanceof AuthenticationProvider) {
                providers.add((AuthenticationProvider) bean)
            }
        }
        return providers
    }

    TAuthenticationProvider create(Map args = [:]) {
        if (args.failOnError == null) args.failOnError = false

        TAuthenticationProvider obj = new TAuthenticationProvider(args)
        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    @CompileDynamic
    @Requires({ args.id })
    TAuthenticationProvider update(Map args = [:]) {
        if (args.failOnError == null) args.failOnError = false

        TAuthenticationProvider obj = get(args.id)
        if (!obj) {
            return null
        }

        Map updateArgs = args.findAll { String name, Object value ->
            obj.hasProperty(name) && name !in [
                'tenant', 'providerType', 'sequence', 'authenticationIdentities',
            ]
        }
        if (obj.providerType != AuthenticationProviderType.LOCAL) updateArgs.remove('canChangePassword')
        if (!args.managerPassword) updateArgs.remove('managerPassword')
        if (!args.clientSecret) updateArgs.remove('clientSecret')

        obj.properties = updateArgs

        if (obj.providerType == AuthenticationProviderType.LOCAL) obj.enabled = true
        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    void delete(Serializable id) {
        TAuthenticationProvider obj = getForCurrentTenant(id)
        if (obj) {
            obj.delete(flush: true)
        }
    }

    private TTenant getRequestTenant() {
        if (hasRequest()) {
            String host = request.getHeader('host')
            TTenant tenant = tenantService.getByHost(host)
            if (tenant) return tenant
        }
        String tenantId = tenantService.currentTenantId ?: tenantService.defaultTenantId
        return tenantService.getByTenantId(tenantId)
    }

}
