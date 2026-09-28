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

import dueuno.application.ApplicationService
import dueuno.commons.utils.CryptoUtils
import dueuno.tenant.TenantService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class CryptoService {

    ApplicationService applicationService
    TenantService tenantService

    void tenantInstall() {
        byte[] password = CryptoUtils.generateAESKey()
        CryptoUtils.saveAESKey(password, tenantAESKeyPathname)
        applicationService.setAttribute(tenantAESKeyAttributeName, password)
    }

    void tenantInit() {
        byte[] password = CryptoUtils.loadAESKey(tenantAESKeyPathname)
        applicationService.setAttribute(tenantAESKeyAttributeName, password)
    }

    String encrypt(String value) {
        return CryptoUtils.encrypt(value, tenantAESKey)
    }

    String decrypt(String encryptedValue) {
        return CryptoUtils.decrypt(encryptedValue, tenantAESKey)
    }

    byte[] getTenantAESKey() {
        return applicationService.getAttribute(tenantAESKeyAttributeName) as byte[]
    }

    private String getTenantAESKeyPathname() {
        return tenantService.privateDir + applicationService.applicationName + '.key'
    }

    private String getTenantAESKeyAttributeName() {
        return tenantService.currentTenantId + '_TENANT_AES_KEY'
    }
}
