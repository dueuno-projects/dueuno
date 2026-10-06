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

import dueuno.security.AuthenticationProviderType
import dueuno.security.TAuthenticationProvider
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
            [providerType: AuthenticationProviderType.EMBEDDED, sequence: 1, enabled: true],
            [providerType: AuthenticationProviderType.PHYSICAL, sequence: 2, enabled: true],
            [providerType      : AuthenticationProviderType.LDAP, sequence: 3, enabled: false,
             searchFilter     : '(sAMAccountName={0})', searchSubtree: true, retrieveGroupRoles: true,
             groupSearchFilter: '(member={0})', groupRoleAttribute: 'cn', retrieveDatabaseRoles: true,
             defaultRole      : 'ROLE_USER'],
            [providerType               : AuthenticationProviderType.GOOGLE, sequence: 4, enabled: false,
             discoveryUri              : 'https://accounts.google.com/.well-known/openid-configuration',
             clientAuthenticationMethod: 'client_secret_post',
             scopes                    : 'openid email profile'],
            [providerType               : AuthenticationProviderType.ENTRA, sequence: 5, enabled: false,
             discoveryUri              : 'https://login.microsoftonline.com/common/v2.0/.well-known/openid-configuration',
             clientAuthenticationMethod: 'client_secret_post',
             scopes                    : 'openid email profile'],
            [providerType               : AuthenticationProviderType.SAP, sequence: 6, enabled: false,
             clientAuthenticationMethod: 'client_secret_basic',
             scopes                    : 'openid email profile'],
            [providerType               : AuthenticationProviderType.OIDC, sequence: 7, enabled: false,
             clientAuthenticationMethod: 'client_secret_basic',
             scopes                    : 'openid email profile'],
            [providerType: AuthenticationProviderType.REMEMBERME, sequence: 8, enabled: true],
        ]

        for (Map values in defaults) {
            AuthenticationProviderType providerType = values.providerType as AuthenticationProviderType
            TAuthenticationProvider provider = TAuthenticationProvider.findByProviderType(providerType)
            if (!provider) {
                provider = new TAuthenticationProvider(values)
            } else {
                provider.sequence = values.sequence as Integer
                if (!provider.discoveryUri && values.discoveryUri) {
                    provider.discoveryUri = values.discoveryUri as String
                }
                if (!provider.clientAuthenticationMethod && values.clientAuthenticationMethod) {
                    provider.clientAuthenticationMethod = values.clientAuthenticationMethod as String
                }
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
    TAuthenticationProvider getByProviderType(AuthenticationProviderType providerType) {
        return TAuthenticationProvider.findByProviderType(providerType)
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

    @CompileDynamic
    TAuthenticationProvider update(Map args) {
        TAuthenticationProvider provider = get(args.id as Serializable)
        if (!provider) return null

        List<String> fields = [
            'enabled', 'server', 'managerDn', 'managerPassword', 'searchBase', 'searchFilter', 'searchSubtree',
            'retrieveGroupRoles', 'groupSearchBase', 'groupSearchFilter', 'groupRoleAttribute',
            'retrieveDatabaseRoles', 'defaultRole', 'clientId', 'clientSecret', 'clientAuthenticationMethod', 'discoveryUri', 'issuerUri',
            'authorizationUri', 'tokenUri', 'jwksUri', 'userInfoUri', 'redirectUri', 'scopes',
        ]
        for (String field in fields) {
            if (!args.containsKey(field)) continue
            if (field == 'managerPassword' || field == 'clientSecret') {
                if (args[field]) provider[field] = args[field]
            } else {
                provider[field] = args[field]
            }
        }

        if (provider.providerType == AuthenticationProviderType.EMBEDDED) provider.enabled = true
        provider.validate()
        provider.save(flush: true)
        return provider
    }

}
