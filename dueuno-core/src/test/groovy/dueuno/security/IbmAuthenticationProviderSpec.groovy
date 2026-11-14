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
package dueuno.security

import com.ibm.as400.access.AS400
import com.ibm.as400.access.AS400SecurityException
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.User
import spock.lang.Specification

class IbmAuthenticationProviderSpec extends Specification {

    AS400 system = Mock()
    AuthenticationProviderService configurations = Mock()
    AuthenticationUserProvisioningService provisioning = Mock()
    TAuthenticationProvider configuration = new TAuthenticationProvider(
        enabled: true, ibmSystemName: 'ibmi.example.org', ibmUseSsl: true,
        retrieveDatabaseRoles: true, defaultRole: 'operator',
    )
    IbmAuthenticationProvider provider = new IbmAuthenticationProvider() {
        @Override
        protected AS400 createSystem(TAuthenticationProvider configuration) {
            return IbmAuthenticationProviderSpec.this.system
        }
    }

    void setup() {
        provider.authenticationProviderService = configurations
        provider.authenticationUserProvisioningService = provisioning
        configurations.getByProviderTypeForCurrentTenant(AuthenticationProviderType.IBM) >> configuration
    }

    void 'authenticates and provisions the normalized profile with configured roles'() {
        given:
        def details = User.withUsername('ALICE').password('').roles('USER').build()
        configuration.retrieveDatabaseRoles = databaseRoles

        when:
        def result = provider.authenticate(new UsernamePasswordAuthenticationToken(' alice ', 'secret'))

        then:
        1 * system.setGuiAvailable(false)
        1 * system.setUseDefaultUser(false)
        1 * system.setUsePasswordCache(false)
        1 * system.setSystemName('ibmi.example.org')
        1 * system.setUserId('ALICE')
        1 * system.validateSignon({ char[] value -> new String(value) == 'secret' }) >> true
        1 * system.disconnectAllServices()
        1 * provisioning.ensureIbmUser('ALICE') >> details
        result.authenticated
        result.principal.is(details)
        result.authorities*.authority == expectedRoles

        where:
        databaseRoles | expectedRoles
        true          | ['ROLE_USER', 'ROLE_OPERATOR']
        false         | ['ROLE_OPERATOR']
    }

    void 'extracts the IBM hostname from the configured URL'() {
        given:
        configuration.ibmSystemName = 'https://pub400.com/'

        when:
        provider.authenticate(new UsernamePasswordAuthenticationToken('alice', 'wrong'))

        then:
        1 * system.setSystemName('pub400.com')
        1 * system.validateSignon(_ as char[]) >> false
        1 * system.disconnectAllServices()
        0 * provisioning._
        thrown(BadCredentialsException)
    }

    void 'rejects failed signon and always disconnects without provisioning'() {
        when:
        provider.authenticate(new UsernamePasswordAuthenticationToken('alice', 'wrong'))

        then:
        1 * system.validateSignon({ char[] value -> new String(value) == 'wrong' }) >> false
        1 * system.disconnectAllServices()
        0 * provisioning._
        thrown(BadCredentialsException)
    }

    void 'clears the password array after signon'() {
        given:
        char[] suppliedPassword

        when:
        provider.authenticate(new UsernamePasswordAuthenticationToken('alice', 'secret'))

        then:
        1 * system.validateSignon(_ as char[]) >> { char[] value ->
            suppliedPassword = value
            throw new IOException('unavailable')
        }
        thrown(AuthenticationServiceException)
        suppliedPassword != null
        suppliedPassword.every { it == (char) 0 }
    }

    void 'maps IBM i security failures without provisioning'() {
        given:
        AS400SecurityException failure = Stub() { getReturnCode() >> code }

        when:
        provider.authenticate(new UsernamePasswordAuthenticationToken('alice', 'secret'))

        then:
        1 * system.validateSignon({ char[] value -> new String(value) == 'secret' }) >> { throw failure }
        1 * system.disconnectAllServices()
        0 * provisioning._
        def exception = thrown(org.springframework.security.core.AuthenticationException)
        expectedType.isInstance(exception)

        where:
        code                                                     | expectedType
        AS400SecurityException.PASSWORD_INCORRECT                 | BadCredentialsException
        AS400SecurityException.PASSWORD_EXPIRED                   | org.springframework.security.authentication.CredentialsExpiredException
        AS400SecurityException.USERID_DISABLE                     | DisabledException
        AS400SecurityException.PASSWORD_INCORRECT_USERID_DISABLE  | DisabledException
    }

    void 'wraps connection failures and disconnects'() {
        when:
        provider.authenticate(new UsernamePasswordAuthenticationToken('alice', 'secret'))

        then:
        1 * system.validateSignon({ char[] value -> new String(value) == 'secret' }) >> { throw new IOException('unavailable') }
        1 * system.disconnectAllServices()
        0 * provisioning._
        thrown(AuthenticationServiceException)
    }

    void 'rejects disabled local accounts even without database roles'() {
        given:
        configuration.retrieveDatabaseRoles = false

        when:
        provider.authenticate(new UsernamePasswordAuthenticationToken('alice', 'secret'))

        then:
        1 * system.validateSignon({ char[] value -> new String(value) == 'secret' }) >> true
        1 * provisioning.ensureIbmUser('ALICE') >> User.withUsername('ALICE').password('').disabled(true).roles('USER').build()
        thrown(DisabledException)
    }

    void 'skips disabled providers'() {
        given:
        configuration.enabled = false

        expect:
        provider.authenticate(new UsernamePasswordAuthenticationToken('alice', 'secret')) == null
    }

    void 'rejects empty credentials before contacting IBM i'() {
        when:
        provider.authenticate(new UsernamePasswordAuthenticationToken('alice', ''))

        then:
        0 * system._
        0 * provisioning._
        thrown(BadCredentialsException)
    }
}
