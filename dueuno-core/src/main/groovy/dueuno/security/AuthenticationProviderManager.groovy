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

import dueuno.application.AuthenticationProviderService
import grails.core.GrailsApplication
import groovy.transform.CompileStatic
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.ProviderNotFoundException
import org.springframework.security.core.Authentication

/**
 * @author Gianluca Sartori
 */

@CompileStatic
class AuthenticationProviderManager implements AuthenticationManager, AuthenticationProvider {

    private static final List<AuthenticationProviderType> BUILT_IN_PROVIDER_TYPES = [
        AuthenticationProviderType.LOCAL,
        AuthenticationProviderType.PHYSICAL,
        AuthenticationProviderType.LDAP,
        AuthenticationProviderType.REMEMBERME,
    ]

    AuthenticationProviderService authenticationProviderService
    GrailsApplication grailsApplication

    @Override
    Authentication authenticate(Authentication authentication) {
        // Add each built-in provider only when it is enabled for the current tenant.
        List<AuthenticationProvider> providers = []
        for (AuthenticationProviderType providerType in BUILT_IN_PROVIDER_TYPES) {
            TAuthenticationProvider providerConfiguration = authenticationProviderService.getByProviderType(providerType)
            if (!providerConfiguration?.enabled) continue

            String beanName = providerType.authenticationProviderBeanName
            if (!grailsApplication.mainContext.containsBean(beanName)) continue
            Object bean = grailsApplication.mainContext.getBean(beanName)
            if (bean instanceof AuthenticationProvider) {
                providers.add((AuthenticationProvider) bean)
            }
        }

        if (!providers) {
            throw new ProviderNotFoundException('No authentication providers are enabled for the current tenant')
        }

        return new ProviderManager(providers).authenticate(authentication)
    }

    @Override
    boolean supports(Class<?> authentication) {
        return true
    }
}
