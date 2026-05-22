package dueuno.security

import groovy.transform.CompileStatic
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.util.Assert

@CompileStatic
class ExternalIdAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal

    ExternalIdAuthenticationToken(Object principal) {
        super([])
        this.principal = principal
        setAuthenticated(false)
    }

    ExternalIdAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities)
        this.principal = principal
        super.setAuthenticated(true) // must use super, as we override
    }

    @Override
    public String getName() {
        if (this.getPrincipal() instanceof CustomGrailsUser) {
            return ((CustomGrailsUser) this.getPrincipal()).getExternalId()
        }
        return super.getName()
    }

    public static ExternalIdAuthenticationToken unauthenticated(Object principal) {
        return new ExternalIdAuthenticationToken(principal)
    }

    public static ExternalIdAuthenticationToken authenticated(Object principal, Collection<? extends GrantedAuthority> authorities) {
        return new ExternalIdAuthenticationToken(principal, authorities)
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
