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
package dueuno.tenants

import dueuno.DueunoGrailsPlugin
import dueuno.commons.utils.FileUtils
import dueuno.core.ApplicationService
import dueuno.core.ConnectionSourceService
import dueuno.core.TSystemInstall
import dueuno.properties.SystemPropertyService
import dueuno.security.TRoleGroup
import dueuno.security.TRoleGroupRole
import dueuno.security.TUserAccount
import dueuno.security.TUserRoleGroup
import dueuno.utils.ResourceUtils
import grails.gorm.DetachedCriteria
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import groovy.contracts.Requires
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.core.connections.ConnectionSource

/**
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class TenantService {

    ApplicationService applicationService
    ConnectionSourceService connectionSourceService
    SystemPropertyService systemPropertyService

    void install() {
        create(
                tenantId: defaultTenantId,
                description: 'Default Tenant',
                deletable: false,
                failOnError: true,
                connectionSource: connectionSourceService.default,
        )
    }

    String getPrivateDir() {
        String tenantIdUpper = currentTenantId.toUpperCase()
        return systemPropertyService.getDirectory(tenantIdUpper + '_TENANT_PRIVATE_DIR')
    }

    String getPublicDir() {
        String tenantIdUpper = currentTenantId.toUpperCase()
        return systemPropertyService.getDirectory(tenantIdUpper + '_TENANT_PUBLIC_DIR')
    }

    String getPrivateDir(String tenantId) {
        String tenantIdUpper = tenantId.toUpperCase()
        return systemPropertyService.getDirectory(tenantIdUpper + '_TENANT_PRIVATE_DIR')
    }

    String getPublicDir(String tenantId) {
        String tenantIdUpper = tenantId.toUpperCase()
        return systemPropertyService.getDirectory(tenantIdUpper + '_TENANT_PUBLIC_DIR')
    }

    void eachTenant(Closure closure) {
        List<TTenant> tenantList = list()
        for (tenant in tenantList) {
            Tenants.withId(tenant.tenantId) {
                closure.call(tenant.tenantId)
            }
        }
    }

    void withTenant(String tenantId, Closure closure) {
        Tenants.withId(tenantId) {
            closure.call(tenantId)
        }
    }

    /**
     * Returns the name of the DEFAULT tenant
     * @return the name of the DEFAULT tenant
     */
    static String getDefaultTenantId() {
        return ConnectionSource.DEFAULT
    }

    /**
     * Returns the DEFAULT tenant
     */
    TTenant getDefaultTenant() {
        return getByTenantId(ConnectionSource.DEFAULT)
    }

    /**
     * INTERNAL USE ONLY. Use SecurityService.getCurrentUserTenantId()
     *
     * Returns the name of the current tenantId
     * @return the name of the current tenantId
     */
    String getCurrentTenantId() {
        return Tenants.currentId()
    }

    /**
     * INTERNAL USE ONLY. Use SecurityService.getCurrentUserTenant()
     *
     * Returns the name of the current tenant
     * @return the name of the current tenant
     */
    TTenant getCurrentTenant() {
        return getByTenantId(currentTenantId)
    }

    @Transactional
    TTenant get(Serializable id) {
        return TTenant.get(id) as TTenant
    }

    @CompileDynamic
    TTenant getByTenantId(String tenantId) {
        return TTenant.findByTenantId(tenantId)
    }

    @CompileDynamic
    TTenant getByHost(String host) {
        return TTenant.findByHost(host)
    }

    @CompileDynamic
    private DetachedCriteria<TTenant> buildQuery(Map filterParams) {
        def query = TTenant.where {}

        if (filterParams.containsKey('tenantId')) query = query.where { tenantId == filterParams.tenantId }
        if (filterParams.containsKey('host')) query = query.where { host == filterParams.host }

        return query
    }

    @Transactional
    List<TTenant> list(Map filterParams = [:], Map fetchParams = [:]) {
        if (!fetchParams.sort) fetchParams.sort = [dateCreated: 'asc']
        def query = buildQuery(filterParams)
        return query.list(fetchParams)
    }

    @Transactional
    Number count(Map filters = [:]) {
        def query = buildQuery(filters)
        return query.count()
    }

    @Transactional
    TTenant create(Map args) {
        if (args.deletable == null) args.deletable = true

        TTenant obj = new TTenant(args)
        if (obj.connectionSource.embedded == null) obj.connectionSource.embedded = false
        obj.connectionSource.tenant = true
        obj.connectionSource.readOnly = false
        if (obj.connectionSource.dbCreate == null) obj.connectionSource.dbCreate = 'update'
        obj.connectionSource.name = obj.tenantId
        obj.save(flush: true, failOnError: args.failOnError)

        if (!obj.hasErrors()) {
            log.info "Creating new tenant '${obj.tenantId}'..."

            String tenantIdUpper = obj.tenantId.toUpperCase()
            String root = systemPropertyService.getDirectory('NEW_TENANT_DIR')

            String privateDir = "${root}${obj.tenantId}/private"
            systemPropertyService.setDirectory(tenantIdUpper + '_TENANT_PRIVATE_DIR', privateDir)
            FileUtils.createDirectory(privateDir)
            ResourceUtils.extractDirectory('/deploy/private', privateDir)

            String publicDir = "${root}${obj.tenantId}/public"
            systemPropertyService.setDirectory(tenantIdUpper + '_TENANT_PUBLIC_DIR', publicDir)
            FileUtils.createDirectory(publicDir)
            ResourceUtils.extractDirectory('/deploy/public', publicDir)

            systemPropertyService.validateAll()

            ResourceUtils.extractDirectoryFromPlugin(
                    DueunoGrailsPlugin,
                    DueunoGrailsPlugin.NAME,
                    '/brand',
                    publicDir
            )

            if (obj.tenantId != defaultTenantId) { // Default tenant gets its 'dataSource' from application.yml
                log.info "${obj.tenantId} Tenant - Connecting to database..."
                connectionSourceService.connect(obj.connectionSource)
            }
        }

        return obj
    }

    void provision(String tenantId) {
        withTenant(tenantId) {
            provisionTenant()
        }
    }

    void provisionAllTenants() {
        eachTenant { String tenantId ->
            provisionTenant()
        }
    }

    private void provisionTenant() {
        applicationService.executeOnPluginTenantInstall()
        applicationService.executeOnTenantInstall()
    }

    void updateAllTenants() {
        eachTenant { String tenantId ->
            applicationService.executeOnUpdate()
        }
    }

    @Transactional
    @CompileDynamic
    @Requires({ args.id })
    TTenant update(Map args) {
        if (args.failOnError == null) args.failOnError = false

        TTenant obj = get(args.id)
        obj.properties = args
        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    @Transactional
    @CompileDynamic
    void delete(Serializable id) {
        TTenant tenant = TTenant.get(id)
        deleteTenantUsersAndGroups(tenant)
        tenant.delete(flush: true)
        tenant.connectionSource.delete(flush: true)

        DetachedCriteria<TSystemInstall> systemInstall = TSystemInstall.where { tenantId == tenant.tenantId }
        systemInstall.deleteAll()
    }

    @Transactional
    @CompileDynamic
    private void deleteTenantUsersAndGroups(TTenant tenant) {
        List<TUserRoleGroup> userRoleGroups = TUserRoleGroup.where { user.tenant == tenant }.list()
        for (userRoleGroup in userRoleGroups) {
            userRoleGroup.delete(flush: true)
        }

        List<TUserAccount> users = TUserAccount.where { tenant == tenant }.list()
        for (user in users) {
            user.delete(flush: true)
        }

        List<TRoleGroupRole> roleGroupRoles = TRoleGroupRole.where { roleGroup.tenant == tenant }.list()
        for (roleGroupRole in roleGroupRoles) {
            roleGroupRole.delete(flush: true)
        }

        List<TRoleGroup> roleGroups = TRoleGroup.where { tenant == tenant }.list()
        for (roleGroup in roleGroups) {
            roleGroup.delete(flush: true)
        }
    }
}
