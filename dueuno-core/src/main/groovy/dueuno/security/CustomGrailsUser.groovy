package dueuno.security

import grails.plugin.springsecurity.userdetails.GrailsUser
import groovy.transform.CompileStatic
import org.springframework.security.core.GrantedAuthority

@CompileStatic
class CustomGrailsUser extends GrailsUser {

    final String physicalId

    CustomGrailsUser(String username, String password, boolean enabled, boolean accountNonExpired,
                     boolean credentialsNonExpired, boolean accountNonLocked,
                     Collection<? extends GrantedAuthority> authorities, Long id, String physicalId) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id)
        this.physicalId = physicalId
    }

    String getPhysicalId() {
        return this.physicalId
    }
}
