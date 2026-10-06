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
package dueuno.application

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.security.authentication.AuthenticationProvider

/**
 * Manages application-wide authentication providers and their runtime order.
 */
@Transactional
@CompileStatic
class AuthenticationProviderService {

    @CompileDynamic
    void install() {
        List<Map> defaults = [
            [providerKey: 'embedded', providerName: 'daoAuthenticationProvider', name: 'Embedded', sequence: 1, enabled: true],
            [providerKey: 'physical', providerName: 'physicalIdAuthenticationProvider', name: 'Physical', sequence: 2, enabled: true],
            [providerKey: 'ldap', providerName: 'ldapAuthenticationProvider', name: 'LDAP', sequence: 3, enabled: false,
                searchFilter: '(sAMAccountName={0})', searchSubtree: true, retrieveGroupRoles: true,
                groupSearchFilter: '(member={0})', groupRoleAttribute: 'cn', retrieveDatabaseRoles: true,
                defaultRole: 'ROLE_USER'],
            [providerKey: 'google', providerName: 'google', name: 'Google', sequence: 4, enabled: false,
                authorizationUri: 'https://accounts.google.com/o/oauth2/v2/auth',
                tokenUri: 'https://oauth2.googleapis.com/token', issuerUri: 'https://accounts.google.com',
                scopes: 'openid email profile'],
            [providerKey: 'rememberMe', providerName: 'rememberMeAuthenticationProvider', name: 'Remember Me', sequence: 5, enabled: true],
        ]

        for (Map values in defaults) {
            TAuthenticationProvider provider = TAuthenticationProvider.findByProviderKey(values.providerKey as String)
            if (!provider) {
                provider = new TAuthenticationProvider(values)
            } else {
                provider.providerName = values.providerName as String
                provider.name = values.name as String
                provider.sequence = values.sequence as Integer
            }
            provider.save(flush: true, failOnError: true)
        }
    }

    List<TAuthenticationProvider> list() {
        return TAuthenticationProvider.list(sort: 'sequence', order: 'asc')
    }

    TAuthenticationProvider get(Serializable id) {
        return TAuthenticationProvider.get(id) as TAuthenticationProvider
    }

    @CompileDynamic
    TAuthenticationProvider getByProviderKey(String providerKey) {
        return TAuthenticationProvider.findByProviderKey(providerKey)
    }

    List<AuthenticationProvider> getEnabledProviders(Map<String, ?> beans) {
        List<AuthenticationProvider> providers = []
        for (TAuthenticationProvider provider in list()) {
            if (!provider.enabled) continue

            Object bean = beans[provider.providerName]
            if (bean instanceof AuthenticationProvider) {
                providers.add((AuthenticationProvider) bean)
            }
        }
        return providers
    }

    @CompileDynamic
    TAuthenticationProvider update(Map args) {
        TAuthenticationProvider provider = get(args.id as Serializable)
        if (!provider) return null

        List<String> fields = [
            'enabled', 'server', 'managerDn', 'managerPassword', 'searchBase', 'searchFilter', 'searchSubtree',
            'retrieveGroupRoles', 'groupSearchBase', 'groupSearchFilter', 'groupRoleAttribute',
            'retrieveDatabaseRoles', 'defaultRole', 'clientId', 'clientSecret', 'issuerUri',
            'authorizationUri', 'tokenUri', 'userInfoUri', 'redirectUri', 'scopes',
        ]
        for (String field in fields) {
            if (!args.containsKey(field)) continue
            if (field == 'managerPassword' || field == 'clientSecret') {
                if (args[field]) provider[field] = args[field]
            } else {
                provider[field] = args[field]
            }
        }

        if (provider.providerKey == 'embedded') provider.enabled = true
        provider.validate()
        provider.save(flush: true)
        return provider
    }

}
