package dueuno.security.physical

import dueuno.elements.WebRequestAware
import dueuno.security.SecurityService
import dueuno.security.TUser
import dueuno.tenant.TTenant
import dueuno.tenant.TenantService
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.userdetails.GormUserDetailsService
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import groovy.util.logging.Slf4j
import jakarta.inject.Inject
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

@Slf4j
class PhysicalUserDetailsService extends GormUserDetailsService implements WebRequestAware,
    AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    @Inject
    SecurityService securityService

    @Inject
    TenantService tenantService

    @Override
    @Transactional(readOnly = true, noRollbackFor = [IllegalArgumentException, UsernameNotFoundException])
    UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authentication)
        throws UsernameNotFoundException {
        String physicalId = authentication.getPrincipal()?.toString()
        if (!physicalId) {
            throw new NoStackUsernameNotFoundException()
        }

        String loginTenantId = tenantService.currentTenantId ?: tenantService.defaultTenantId
        if (hasRequest()) {
            loginTenantId = tenantService.getByRequest(request)?.tenantId
                ?: tenantService.defaultTenantId
        }
        TTenant loginTenant = tenantService.getByTenantId(loginTenantId)
        if (!loginTenant) {
            throw new NoStackUsernameNotFoundException()
        }

        TUser user = securityService.getUserByPhysicalId(physicalId)
        if (!user || user.tenant?.id != loginTenant.id) {
            log.warn 'User not found: {}', physicalId
            throw new NoStackUsernameNotFoundException()
        }

        Collection<GrantedAuthority> authorities = loadAuthorities(user, user.username, true)
        return createUserDetails(user, authorities)
    }
}
