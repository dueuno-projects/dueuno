package dueuno.security.physical

import dueuno.elements.WebRequestAware
import dueuno.security.AuthenticationProviderType
import dueuno.security.SecurityService
import dueuno.security.TAuthenticationProvider
import dueuno.security.TUser
import dueuno.tenant.TTenant
import dueuno.tenant.TenantService
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GormUserDetailsService
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import groovy.util.logging.Slf4j
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Slf4j
class PhysicalUserDetailsService extends GormUserDetailsService implements WebRequestAware {

    SecurityService securityService
    TenantService tenantService

    @Transactional(readOnly = true, noRollbackFor = [IllegalArgumentException, UsernameNotFoundException])
    UserDetails loadUserByPhysicalId(String physicalId, boolean loadRoles) throws UsernameNotFoundException {

        String loginTenantId = tenantService.currentTenantId ?: tenantService.defaultTenantId
        if (hasRequest()) {
            loginTenantId = tenantService.getByHost(request.getHeader('host'))?.tenantId
                ?: tenantService.defaultTenantId
        }
        TTenant loginTenant = tenantService.getByTenantId(loginTenantId)
        if (!loginTenant) {
            throw new NoStackUsernameNotFoundException()
        }

        TAuthenticationProvider provider = TAuthenticationProvider.findByTenantAndProviderType(
            loginTenant, AuthenticationProviderType.PHYSICAL)
        if (!provider?.enabled) {
            throw new NoStackUsernameNotFoundException()
        }

        TUser user = securityService.getUserByPhysicalId(physicalId)
        if (!user || user.tenant?.id != loginTenant.id) {
            log.warn 'User not found: {}', physicalId
            throw new NoStackUsernameNotFoundException()
        }

        Collection<GrantedAuthority> authorities = loadAuthorities(user, physicalId, loadRoles)
        createUserDetails user, authorities
    }

    UserDetails loadUserByPhysicalId(String physicalId) throws UsernameNotFoundException {
        loadUserByPhysicalId physicalId, true
    }

    @Override
    protected UserDetails createUserDetails(user, Collection<GrantedAuthority> authorities) {

        def conf = SpringSecurityUtils.securityConfig

        String usernamePropertyName = conf.userLookup.usernamePropertyName
        String passwordPropertyName = conf.userLookup.passwordPropertyName
        String enabledPropertyName = conf.userLookup.enabledPropertyName
        String accountExpiredPropertyName = conf.userLookup.accountExpiredPropertyName
        String accountLockedPropertyName = conf.userLookup.accountLockedPropertyName
        String passwordExpiredPropertyName = conf.userLookup.passwordExpiredPropertyName

        String username = user."$usernamePropertyName"
        String password = user."$passwordPropertyName"
        boolean enabled = enabledPropertyName ? user."$enabledPropertyName" : true
        boolean accountExpired = accountExpiredPropertyName ? user."$accountExpiredPropertyName" : false
        boolean accountLocked = accountLockedPropertyName ? user."$accountLockedPropertyName" : false
        boolean passwordExpired = passwordExpiredPropertyName ? user."$passwordExpiredPropertyName" : false

        new PhysicalGrailsUser(username, password, enabled, !accountExpired, !passwordExpired,
            !accountLocked, authorities, user.id, username)
    }
}
