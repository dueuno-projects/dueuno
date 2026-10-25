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
package dueuno.security.physical

import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import spock.lang.Specification

class PhysicalAuthenticationFilterSpec extends Specification {

    void 'authenticates by physical ID and returns the standard Grails user principal'() {
        given:
        GrailsUser user = new GrailsUser(
            'alice',
            '',
            true,
            true,
            true,
            true,
            [new SimpleGrantedAuthority('ROLE_USER')],
            1L,
        )
        AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> userDetailsService =
            Mock(AuthenticationUserDetailsService)
        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider(
            preAuthenticatedUserDetailsService: userDetailsService,
        )
        PhysicalAuthenticationFilter filter = new PhysicalAuthenticationFilter('/api/auth/physical')
        filter.authenticationManager = new ProviderManager([provider])
        MockHttpServletRequest request = new MockHttpServletRequest()
        request.method = 'POST'
        request.requestURI = '/api/auth/physical'
        request.addParameter('physicalId', ' badge-123 ')

        when:
        Authentication authentication = filter.attemptAuthentication(request, new MockHttpServletResponse())

        then:
        1 * userDetailsService.loadUserDetails({ PreAuthenticatedAuthenticationToken token ->
            token.principal == 'badge-123' && token.credentials == ''
        }) >> user
        authentication.authenticated
        authentication.principal.is(user)
        authentication.name == 'alice'
    }
}
