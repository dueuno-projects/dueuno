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
import groovy.transform.CompileStatic
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import grails.core.GrailsApplication

import java.util.concurrent.atomic.AtomicReference

/**
 * @author Gianluca Sartori
 */

@CompileStatic
class AuthenticationProviderManager implements AuthenticationManager, AuthenticationProvider {

    private static final Set<String> EXCLUDED_PROVIDER_IDENTIFIERS = [
        'daoAuthorizationProvider', 'daoAuthenticationProvider', 'EMBEDDED',
        'physicalIdAuthorizationProvider', 'physicalIdAuthenticationProvider', 'PHYSICAL',
        'rememberMeProvider', 'rememberMeAuthenticationProvider', 'REMEMBERME',
    ] as Set<String>

    AuthenticationProviderService authenticationProviderService
    AuthenticationUserProvisioningService authenticationUserProvisioningService
    GrailsApplication grailsApplication

    @Override
    Authentication authenticate(Authentication authentication) {
        AtomicReference<TAuthenticationProvider> successfulProvider = new AtomicReference<>()
        Map<String, AuthenticationProvider> beans = [:]
        for (TAuthenticationProvider provider in authenticationProviderService.list()) {
            String beanName = provider.providerType.authenticationProviderBeanName
            if (!provider.enabled || !grailsApplication.mainContext.containsBean(beanName)) continue
            Object bean = grailsApplication.mainContext.getBean(beanName)
            if (bean instanceof AuthenticationProvider) {
                beans[beanName] = new TrackingAuthenticationProvider(
                    (AuthenticationProvider) bean,
                    provider,
                    successfulProvider,
                )
            }
        }
        List<AuthenticationProvider> providers = authenticationProviderService.getEnabledProviders(beans)
        Authentication result = new ProviderManager(providers).authenticate(authentication)
        TAuthenticationProvider provider = successfulProvider.get()
        if (provider != null && !isExcluded(provider)) {
            authenticationUserProvisioningService.ensureUser(result.name)
        }
        return result
    }

    private boolean isExcluded(TAuthenticationProvider provider) {
        return EXCLUDED_PROVIDER_IDENTIFIERS.contains(provider.providerType.authenticationProviderBeanName) ||
            EXCLUDED_PROVIDER_IDENTIFIERS.contains(provider.providerType.name())
    }

    @Override
    boolean supports(Class<?> authentication) {
        return true
    }

    private static class TrackingAuthenticationProvider implements AuthenticationProvider {

        private final AuthenticationProvider delegate
        private final TAuthenticationProvider provider
        private final AtomicReference<TAuthenticationProvider> successfulProvider

        TrackingAuthenticationProvider(
            AuthenticationProvider delegate,
            TAuthenticationProvider provider,
            AtomicReference<TAuthenticationProvider> successfulProvider
        ) {
            this.delegate = delegate
            this.provider = provider
            this.successfulProvider = successfulProvider
        }

        @Override
        Authentication authenticate(Authentication authentication) {
            Authentication result = delegate.authenticate(authentication)
            if (result != null && result.authenticated) successfulProvider.set(provider)
            return result
        }

        @Override
        boolean supports(Class<?> authentication) {
            return delegate.supports(authentication)
        }
    }
}
