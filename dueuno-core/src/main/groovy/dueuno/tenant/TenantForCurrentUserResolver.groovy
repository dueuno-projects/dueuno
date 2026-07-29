/*
 * Copyright 2021 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License")
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
package dueuno.tenant

import dueuno.application.WebRequestAware
import dueuno.security.TUser
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.multitenancy.TenantResolver
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.springframework.stereotype.Component

/**
 * @author Gianluca Sartori
 */

@Slf4j
@Component
@CompileStatic
class TenantForCurrentUserResolver implements TenantResolver, WebRequestAware {

    @Override
    Serializable resolveTenantIdentifier() throws TenantNotFoundException {
        if (!hasRequest()) {
            return TenantService.defaultTenantId
        }

        try {
            TUser user = session['_21CurrentUser'] as TUser
            if (user) {
                return user.tenant.tenantId
            } else {
                return TenantService.defaultTenantId
            }

        } catch (Exception ignore) {
            return TenantService.defaultTenantId
        }
    }

}
