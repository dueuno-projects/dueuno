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
package dueuno.elements

import dueuno.tenant.TenantPropertyService
import dueuno.utils.EnvUtils

/**
 * @author Gianluca Sartori
 */
class DevTagLib implements WebRequestAware {

    static namespace = "dev"

    TenantPropertyService tenantPropertyService

    def ifDevelopment(Map attrs, Closure body) {
        if (EnvUtils.isDevelopment()) {
            out << body()
        }
    }

    def ifDisplayHints(Map attrs, Closure body) {
        if (devDisplayHints) {
            out << body()
        }
    }

    def displayHints(Map attrs) {
        String result = devDisplayHints ? 'true' : 'false'
        out << result
    }

    def logError(Map attrs) {
        String result = tenantPropertyService.getBoolean('LOG_ERROR') ? 'true' : 'false'
        out << result
    }

    def logDebug(Map attrs) {
        String result = tenantPropertyService.getBoolean('LOG_DEBUG') ? 'true' : 'false'
        out << result
    }

    def logTrace(Map attrs) {
        String result = tenantPropertyService.getBoolean('LOG_TRACE') ? 'true' : 'false'
        out << result
    }

}
