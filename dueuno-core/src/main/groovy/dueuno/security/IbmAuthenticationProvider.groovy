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

import com.ibm.as400.access.AS400
import com.ibm.as400.access.AS400SecurityException
import com.ibm.as400.access.SecureAS400
import groovy.transform.CompileStatic
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

import java.beans.PropertyVetoException

/**
 * Authenticates IBM i user profiles using the tenant's JT400 sign-on service.
 */
@CompileStatic
class IbmAuthenticationProvider implements AuthenticationProvider {

    AuthenticationProviderService authenticationProviderService
    AuthenticationUserProvisioningService authenticationUserProvisioningService

    @Override
    Authentication authenticate(Authentication authentication) {
        TAuthenticationProvider provider = authenticationProviderService.getByProviderTypeForCurrentTenant(AuthenticationProviderType.IBM)
        if (!provider?.enabled) return null
        if (!provider.ibmSystemName?.trim()) {
            throw new AuthenticationServiceException('IBM i host is required')
        }
        String systemName = provider.ibmSystemName.trim()
        if (systemName.contains('://')) {
            try {
                systemName = new URI(systemName).host
            } catch (URISyntaxException e) {
                throw new AuthenticationServiceException('Invalid IBM i host', e)
            }
            if (!systemName) throw new AuthenticationServiceException('Invalid IBM i host')
        }
        String username = authentication.name?.trim()?.toUpperCase(Locale.ROOT)
        String password = authentication.credentials?.toString()
        if (!username || !password) throw new BadCredentialsException('Invalid credentials')

        AS400 system = createSystem(provider)
        char[] passwordChars = password.toCharArray()
        try {
            system.setGuiAvailable(false)
            system.setUseDefaultUser(false)
            system.setUsePasswordCache(false)
            system.setSystemName(systemName)
            system.setUserId(username)
            if (!system.validateSignon(passwordChars)) throw new BadCredentialsException('Invalid credentials')
        } catch (AS400SecurityException e) {
            if (e.returnCode == AS400SecurityException.PASSWORD_EXPIRED) {
                throw new CredentialsExpiredException('IBM i password has expired', e)
            }
            if (e.returnCode == AS400SecurityException.USERID_DISABLE ||
                e.returnCode == AS400SecurityException.PASSWORD_INCORRECT_USERID_DISABLE) {
                throw new DisabledException('IBM i user profile is disabled', e)
            }
            throw new BadCredentialsException('Invalid credentials', e)
        } catch (IOException | PropertyVetoException e) {
            throw new AuthenticationServiceException('Unable to authenticate against the IBM i system', e)
        } finally {
            Arrays.fill(passwordChars, (char) 0)
            system.disconnectAllServices()
        }

        UserDetails details = authenticationUserProvisioningService.ensureIbmUser(username)
        new AccountStatusUserDetailsChecker().check(details)
        Set<GrantedAuthority> authorities = new LinkedHashSet<>()
        if (provider.retrieveDatabaseRoles) authorities.addAll(details.authorities)
        if (provider.defaultRole?.trim()) {
            String role = provider.defaultRole.trim().replace(' ', '_').toUpperCase(Locale.ROOT)
            if (!role.startsWith('ROLE_')) role = 'ROLE_' + role
            authorities.add(new SimpleGrantedAuthority(role))
        }
        return new UsernamePasswordAuthenticationToken(details, password, authorities)
    }

    protected AS400 createSystem(TAuthenticationProvider provider) {
        return provider.ibmUseSsl ? new SecureAS400() : new AS400()
    }

    @Override
    boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.isAssignableFrom(authentication)
    }
}
