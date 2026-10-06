package dueuno.security.physical

import groovy.transform.CompileStatic
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.util.Assert

@CompileStatic
class PhysicalAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal

    PhysicalAuthenticationToken(Object principal) {
        super([])
        this.principal = principal
        setAuthenticated(false)
    }

    PhysicalAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities)
        this.principal = principal
        super.setAuthenticated(true) // must use super, as we override
    }

    @Override
    public String getName() {
        if (this.getPrincipal() instanceof PhysicalGrailsUser) {
            return ((PhysicalGrailsUser) this.getPrincipal()).getPhysicalId()
        }
        return super.getName()
    }

    public static PhysicalAuthenticationToken unauthenticated(Object principal) {
        return new PhysicalAuthenticationToken(principal)
    }

    public static PhysicalAuthenticationToken authenticated(Object principal, Collection<? extends GrantedAuthority> authorities) {
        return new PhysicalAuthenticationToken(principal, authorities)
    }

    @Override
    Object getCredentials() {
        return null
    }

    @Override
    Object getPrincipal() {
        return this.principal
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated,
            "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead")
        super.setAuthenticated(false)
    }
}
