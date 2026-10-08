package dueuno.security.physical

import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.security.authentication.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.SpringSecurityMessageSource
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.util.Assert

@CompileStatic
class PhysicalAuthenticationProvider implements AuthenticationProvider {

    protected final Log logger = LogFactory.getLog(getClass())

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor()

    protected boolean hideUserNotFoundExceptions = true

    private UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks()

    private UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks()

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper()

    PhysicalUserDetailsService physicalUserDetailsService

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(PhysicalAuthenticationToken.class, authentication,
            () -> this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.onlySupports",
                "Only PhysicalAuthenticationToken is supported"))
        String physicalId = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName()
        UserDetails user
        try {
            user = retrieveUser(physicalId)
        }
        catch (UsernameNotFoundException ex) {
            this.logger.debug("Failed to find user '" + physicalId + "'")
            if (!this.hideUserNotFoundExceptions) {
                throw ex
            }
            throw new BadCredentialsException(this.messages
                .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"))
        }
        Assert.notNull(user, "retrieveUser returned null - a violation of the interface contract")

        this.preAuthenticationChecks.check(user)
        this.postAuthenticationChecks.check(user)
        return createSuccessAuthentication(user, authentication)
    }

    @Override
    boolean supports(Class<?> authentication) {
        return PhysicalAuthenticationToken.isAssignableFrom(authentication)
    }

    protected Authentication createSuccessAuthentication(UserDetails user, Authentication authentication) {
        PhysicalAuthenticationToken result = PhysicalAuthenticationToken.authenticated(user,
            this.authoritiesMapper.mapAuthorities(user.getAuthorities()))
        result.setDetails(authentication.getDetails())
        this.logger.debug("Authenticated user")
        return result
    }

    protected final UserDetails retrieveUser(String physicalId)
        throws AuthenticationException {
        try {
            UserDetails loadedUser = this.getPhysicalUserDetailsService().loadUserByPhysicalId(physicalId)
            if (loadedUser == null) {
                throw new InternalAuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation")
            }
            return loadedUser
        }
        catch (UsernameNotFoundException ex) {
            throw ex
        }
        catch (InternalAuthenticationServiceException ex) {
            throw ex
        }
        catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex)
        }
    }

    public void setPhysicalUserDetailsService(PhysicalUserDetailsService physicalUserDetailsService) {
        this.physicalUserDetailsService = physicalUserDetailsService
    }

    protected PhysicalUserDetailsService getPhysicalUserDetailsService() {
        return this.physicalUserDetailsService
    }

    private class DefaultPreAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                PhysicalAuthenticationProvider.this.logger
                    .debug("Failed to authenticate since user account is locked")
                throw new LockedException(PhysicalAuthenticationProvider.this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"))
            }
            if (!user.isEnabled()) {
                PhysicalAuthenticationProvider.this.logger
                    .debug("Failed to authenticate since user account is disabled")
                throw new DisabledException(PhysicalAuthenticationProvider.this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"))
            }
            if (!user.isAccountNonExpired()) {
                PhysicalAuthenticationProvider.this.logger
                    .debug("Failed to authenticate since user account has expired")
                throw new AccountExpiredException(PhysicalAuthenticationProvider.this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"))
            }
        }

    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isCredentialsNonExpired()) {
                PhysicalAuthenticationProvider.this.logger
                    .debug("Failed to authenticate since user account credentials have expired")
                throw new CredentialsExpiredException(PhysicalAuthenticationProvider.this.messages
                    .getMessage("AbstractUserDetailsAuthenticationProvider.credentialsExpired",
                        "User credentials have expired"))
            }
        }

    }
}
