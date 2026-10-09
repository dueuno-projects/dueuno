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

/**
 * INTERNAL USE ONLY
 *
 * @author Gianluca Sartori
 */
class OidcAuthenticationUrlMappings {

    static mappings = {
        "/authentication/oidc/${providerType}/start"(controller: 'oidcAuthentication', action: 'start')
        "/authentication/oidc/${providerType}"(controller: 'oidcAuthentication', action: 'callback')
    }
}
