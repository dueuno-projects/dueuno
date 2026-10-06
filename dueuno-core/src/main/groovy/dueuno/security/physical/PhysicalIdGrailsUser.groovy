package dueuno.security.physical

import grails.plugin.springsecurity.userdetails.GrailsUser
import groovy.transform.CompileStatic
import org.springframework.security.core.GrantedAuthority

@CompileStatic
class PhysicalIdGrailsUser extends GrailsUser {

    final String physicalId

    PhysicalIdGrailsUser(String username, String password, boolean enabled, boolean accountNonExpired,
                       boolean credentialsNonExpired, boolean accountNonLocked,
                       Collection<? extends GrantedAuthority> authorities, Long id, String physicalId) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id)
        this.physicalId = physicalId
    }

    String getPhysicalId() {
        return this.physicalId
    }
}
