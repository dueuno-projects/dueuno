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

import dueuno.elements.ElementsController
import dueuno.tenant.TenantPropertyService
import groovy.transform.CompileStatic
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpSession

/**
 * Captures the expiry embedded in each authenticated user's remember-me cookie.
 */
@CompileStatic
class UserSessionRememberMeInterceptor implements ElementsController {

    int order = HIGHEST_PRECEDENCE + 20

    TenantPropertyService tenantPropertyService
    UserSessionService userSessionService
    SecurityService securityService

    UserSessionRememberMeInterceptor() {
        matchAll()
    }

    boolean before() {
        HttpSession httpSession = request.getSession(false)

        try {
            String cookieName = tenantPropertyService.getString('REMEMBER_ME_COOKIE_NAME')
            if (!cookieName) return true

            String cookieValue
            Cookie[] cookies = request.cookies
            if (cookies) {
                for (Cookie cookie in cookies) {
                    if (cookie.name == cookieName) {
                        cookieValue = cookie.value
                        break
                    }
                }
            }

            if (userSessionService.isRememberMeRevoked(cookieValue)) {
                securityService.executeLogout()
                response.sendRedirect(request.contextPath + '/authentication/login')
                return false
            }

            if (!httpSession) return true

            TUser user = httpSession.getAttribute('_21CurrentUser') as TUser
            if (!user) return true

            userSessionService.updateRememberMeExpiration(httpSession, cookieValue)
        } catch (IllegalStateException ignored) {
            // The session may be invalidated while the request is being processed.
        }

        return true
    }

    boolean after() {
        return true
    }

    void afterView() {
        // no-op
    }
}
