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
import grails.gorm.multitenancy.CurrentTenant
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

    @CurrentTenant
    void tenantInstall() {
        String tenantId = tenantService.currentTenantId
        byte[] password = CryptoUtils.generateAESKey()
        CryptoUtils.saveAESKey(password, tenantAESKeyPathname)
        applicationService.setAttribute(getTenantAESKeyAttributeName(tenantId), password)
    }

    @CurrentTenant
    void tenantInit() {
        String tenantId = tenantService.currentTenantId
        byte[] password = CryptoUtils.loadAESKey(tenantAESKeyPathname)
        applicationService.setAttribute(getTenantAESKeyAttributeName(tenantId), password)
    }

    @CurrentTenant
    String encrypt(String value) {
        return CryptoUtils.encrypt(value, getTenantAESKey())
    }

    @CurrentTenant
    String decrypt(String encryptedValue) {
        return CryptoUtils.decrypt(encryptedValue, getTenantAESKey())
    }

    @CurrentTenant
    byte[] getTenantAESKey() {
        return applicationService.getAttribute(getTenantAESKeyAttributeName(tenantService.currentTenantId)) as byte[]
    }

    private String getTenantAESKeyPathname() {
        return tenantService.privateDir + applicationService.applicationName + '.key'
    }

    private String getTenantAESKeyAttributeName(String tenantId) {
        return tenantId.toUpperCase() + '_TENANT_AES_KEY'
    }
}
