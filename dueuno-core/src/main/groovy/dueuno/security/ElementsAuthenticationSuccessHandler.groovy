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

import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationSuccessHandler
import groovy.transform.CompileStatic
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication

/**
 * Preserves the client's device type across the authentication redirect.
 */
@CompileStatic
class ElementsAuthenticationSuccessHandler extends AjaxAwareAuthenticationSuccessHandler {

    private static final String MOBILE_DEVICE_ATTRIBUTE = 'MOBILE_DEVICE'

    @Override
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        String mobileDeviceFlag = request.getParameter('_21IsMobileDevice')
        if (mobileDeviceFlag != null) {
            request.getSession().setAttribute(MOBILE_DEVICE_ATTRIBUTE, Boolean.parseBoolean(mobileDeviceFlag))
        }

        super.onAuthenticationSuccess(request, response, authentication)
    }
}
