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

import dueuno.application.TAuthenticationProvider
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException

import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.Attribute
import javax.naming.directory.Attributes
import javax.naming.directory.DirContext
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult

/**
 * LDAP authentication whose settings are loaded from the system database for each login.
 */
@CompileStatic
class LdapAuthenticationProvider implements AuthenticationProvider {

    AuthenticationUserProvisioningService authenticationUserProvisioningService

    @Override
    Authentication authenticate(Authentication authentication) {
        UsernamePasswordAuthenticationToken request = (UsernamePasswordAuthenticationToken) authentication
        String username = request.name
        String password = request.credentials?.toString()
        TAuthenticationProvider provider = findLdapProvider()

        if (!provider?.enabled) return null
        if (!provider.server || !provider.searchBase) {
            throw new AuthenticationServiceException('LDAP server URL and user search base are required')
        }
        if (!password) throw new BadCredentialsException('Invalid credentials')

        DirContext directoryContext = null
        try {
            directoryContext = new InitialDirContext(buildEnvironment(provider, provider.managerDn, provider.managerPassword))
            SearchResult userResult = findUser(directoryContext, provider, username)
            if (userResult == null) throw new BadCredentialsException('Invalid credentials')
            String userDn = userResult.nameInNamespace
            String firstname = attributeValue(userResult.attributes, 'givenName')
            String lastname = attributeValue(userResult.attributes, 'sn')

            try {
                DirContext userContext = new InitialDirContext(buildEnvironment(provider, userDn, password))
                userContext.close()
            } catch (NamingException ignored) {
                throw new BadCredentialsException('Invalid credentials')
            }

            UserDetails provisionedDetails = loadUserDetails(username, firstname, lastname)
            UserDetails details = provider.retrieveDatabaseRoles
                ? provisionedDetails
                : new User(username, '', true, true, true, true, [] as Collection<GrantedAuthority>)
            if (!details.enabled) throw new DisabledException('User is disabled')
            if (!details.accountNonLocked) throw new LockedException('User account is locked')
            if (!details.accountNonExpired) throw new AccountExpiredException('User account has expired')
            if (!details.credentialsNonExpired) throw new CredentialsExpiredException('User credentials have expired')

            Collection<GrantedAuthority> authorities = resolveAuthorities(directoryContext, provider, userDn, details)
            return new UsernamePasswordAuthenticationToken(details, password, authorities)
        } catch (BadCredentialsException | AuthenticationServiceException e) {
            throw e
        } catch (NamingException e) {
            throw new AuthenticationServiceException('Unable to authenticate against the LDAP server', e)
        } finally {
            if (directoryContext != null) {
                try {
                    directoryContext.close()
                } catch (NamingException ignored) {
                    // Closing the connection must not replace the authentication result.
                }
            }
        }
    }

    @Transactional(readOnly = true)
    @CompileDynamic
    private TAuthenticationProvider findLdapProvider() {
        return TAuthenticationProvider.findByProviderKey('ldap')
    }

    @Transactional
    @CompileDynamic
    private UserDetails loadUserDetails(String username) {
        return loadUserDetails(username, null, null)
    }

    @Transactional
    @CompileDynamic
    private UserDetails loadUserDetails(String username, String firstname, String lastname) {
        return authenticationUserProvisioningService.ensureUser(username, [firstname: firstname, lastname: lastname])
    }

    private Hashtable<String, Object> buildEnvironment(TAuthenticationProvider provider, String principal, String password) {
        Hashtable<String, Object> environment = new Hashtable<>()
        environment.put(Context.INITIAL_CONTEXT_FACTORY, 'com.sun.jndi.ldap.LdapCtxFactory')
        environment.put(Context.PROVIDER_URL, provider.server)
        environment.put(Context.REFERRAL, 'follow')
        if (principal && password) {
            environment.put(Context.SECURITY_AUTHENTICATION, 'simple')
            environment.put(Context.SECURITY_PRINCIPAL, principal)
            environment.put(Context.SECURITY_CREDENTIALS, password)
        } else {
            environment.put(Context.SECURITY_AUTHENTICATION, 'none')
        }
        return environment
    }

    private SearchResult findUser(DirContext context, TAuthenticationProvider provider, String username) {
        SearchControls controls = new SearchControls()
        controls.searchScope = provider.searchSubtree ? SearchControls.SUBTREE_SCOPE : SearchControls.ONELEVEL_SCOPE
        controls.returningAttributes = ['givenName', 'sn'] as String[]
        NamingEnumeration<SearchResult> results = context.search(
            provider.searchBase,
            normalizeFilter(provider.searchFilter ?: '(uid={0})').replace('{0}', escapeFilter(username)),
            controls,
        )
        try {
            if (!results.hasMore()) return null
            return results.next()
        } finally {
            results.close()
        }
    }

    private String attributeValue(Attributes attributes, String name) {
        Attribute attribute = attributes?.get(name)
        if (attribute == null) return null
        Object rawValue = attribute.get()
        if (rawValue == null) return null
        String value = rawValue.toString().trim()
        return value ? value : null
    }

    private Collection<GrantedAuthority> resolveAuthorities(DirContext context, TAuthenticationProvider provider, String userDn, UserDetails details) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>()
        if (provider.retrieveDatabaseRoles) authorities.addAll(details.authorities)
        if (provider.retrieveGroupRoles && provider.groupSearchBase && provider.groupSearchFilter && provider.groupRoleAttribute) {
            SearchControls controls = new SearchControls()
            controls.searchScope = SearchControls.SUBTREE_SCOPE
            controls.returningAttributes = [provider.groupRoleAttribute] as String[]
            NamingEnumeration<SearchResult> results = context.search(
                provider.groupSearchBase,
                normalizeFilter(provider.groupSearchFilter).replace('{0}', escapeFilter(userDn)),
                controls,
            )
            try {
                while (results.hasMore()) {
                    Attributes attributes = results.next().attributes
                    Attribute roles = attributes?.get(provider.groupRoleAttribute)
                    if (roles == null) continue
                    NamingEnumeration<?> values = roles.all
                    try {
                        while (values.hasMore()) authorities.add(toAuthority(values.next().toString()))
                    } finally {
                        values.close()
                    }
                }
            } finally {
                results.close()
            }
        }
        if (provider.defaultRole) authorities.add(toAuthority(provider.defaultRole))
        return authorities
    }

    private GrantedAuthority toAuthority(String role) {
        String normalizedRole = role.trim().replace(' ', '_').toUpperCase(Locale.ROOT)
        if (!normalizedRole.startsWith('ROLE_')) normalizedRole = 'ROLE_' + normalizedRole
        return new SimpleGrantedAuthority(normalizedRole)
    }

    private String escapeFilter(String value) {
        return value.replace('\\', '\\5c')
            .replace('*', '\\2a')
            .replace('(', '\\28')
            .replace(')', '\\29')
            .replace('\u0000', '\\00')
    }

    private String normalizeFilter(String filter) {
        String value = filter.trim()
        return value.startsWith('(') ? value : '(' + value + ')'
    }

    @Override
    boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.isAssignableFrom(authentication)
    }

}
