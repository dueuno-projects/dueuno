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

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.transaction.TransactionStatus
import spock.lang.Specification

import java.lang.reflect.Method

class LdapAuthenticationProviderSpec extends Specification {

    void 'delegates LDAP user provisioning to the shared service'() {
        given:
        LdapAuthenticationProvider provider = new LdapAuthenticationProvider()
        AuthenticationUserProvisioningService authenticationUserProvisioningService = Mock(AuthenticationUserProvisioningService)
        provider.authenticationUserProvisioningService = authenticationUserProvisioningService
        UserDetails createdUserDetails = new User(
            'sartogia',
            '',
            true,
            true,
            true,
            true,
            [new SimpleGrantedAuthority('ROLE_USER')],
        )

        when:
        Method method = LdapAuthenticationProvider.getDeclaredMethod(
            '$tt__loadUserDetails', String, String, String, String, String, String, TransactionStatus,
        )
        method.accessible = true
        UserDetails details = (UserDetails) method.invoke(provider, 'sartogia', null, null, 'sartogia@example.org', null, null, null)

        then:
        1 * authenticationUserProvisioningService.ensureUser('sartogia', [
            firstname: null,
            lastname : null,
            email     : 'sartogia@example.org',
            telephone : null,
            note      : null,
        ]) >> createdUserDetails
        details.username == 'sartogia'
        details.authorities*.authority == ['ROLE_USER']
    }
}
