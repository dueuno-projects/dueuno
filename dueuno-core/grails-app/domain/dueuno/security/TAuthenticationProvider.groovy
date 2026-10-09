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

import dueuno.tenant.TTenant
import grails.compiler.GrailsCompileStatic
import org.grails.datastore.gorm.GormEntity

/**
 * Authentication provider configuration for a tenant.
 */
@GrailsCompileStatic
class TAuthenticationProvider implements GormEntity, Serializable {

    private static final long serialVersionUID = 1

    Long id
    TTenant tenant
    AuthenticationProviderType providerType
    Integer sequence
    Boolean enabled

    String server
    String managerDn
    String managerPassword
    String searchBase
    String searchFilter
    Boolean searchSubtree
    Boolean retrieveGroupRoles
    String groupSearchBase
    String groupSearchFilter
    String groupRoleAttribute
    Boolean retrieveDatabaseRoles
    String defaultRole

    String clientId
    String clientSecret
    String clientAuthenticationMethod
    String discoveryUri
    String issuerUri
    String authorizationUri
    String tokenUri
    String jwksUri
    String userInfoUri
    String redirectUri
    String scopes

    Set<TUserAuthenticationIdentity> authenticationIdentities
    static hasMany = [
        authenticationIdentities: TUserAuthenticationIdentity,
    ]

    static constraints = {
        tenant nullable: false
        providerType nullable: false, unique: ['tenant']
        sequence nullable: false
        enabled nullable: false
        managerDn nullable: true, blank: true
        managerPassword nullable: true, blank: true
        server nullable: true, blank: true
        searchBase nullable: true, blank: true
        searchFilter nullable: true, blank: true
        searchSubtree nullable: true
        retrieveGroupRoles nullable: true
        groupSearchBase nullable: true, blank: true
        groupSearchFilter nullable: true, blank: true
        groupRoleAttribute nullable: true, blank: true
        retrieveDatabaseRoles nullable: true
        defaultRole nullable: true, blank: true
        clientId nullable: true, blank: true
        clientSecret nullable: true, blank: true
        clientAuthenticationMethod nullable: true, blank: true, inList: ['client_secret_basic', 'client_secret_post']
        discoveryUri nullable: true, blank: true
        issuerUri nullable: true, blank: true
        authorizationUri nullable: true, blank: true
        tokenUri nullable: true, blank: true
        jwksUri nullable: true, blank: true
        userInfoUri nullable: true, blank: true
        redirectUri nullable: true, blank: true
        scopes nullable: true, blank: true
    }

    static mapping = {
        table 'sys_authentication_provider'
    }

}
