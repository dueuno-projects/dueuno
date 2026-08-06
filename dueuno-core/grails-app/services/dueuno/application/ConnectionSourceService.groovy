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
package dueuno.application

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.contracts.Requires
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.orm.hibernate.connections.HibernateConnectionSourceSettings
import org.hibernate.SessionFactory

import javax.sql.DataSource
import java.sql.DriverManager

/**
 * @author Gianluca Sartori
 */

@Slf4j
@Transactional
@CompileStatic
class ConnectionSourceService {

    HibernateDatastore hibernateDatastore
    ApplicationService applicationService

    void installOrConnect() {
        applicationService.registerPrettyPrinter(TConnectionSource, '${it.name}${it.tenant ? " Tenant" : ""} (${it.driverClassName})')

        if (!connectionSourceAlreadyInstalled()) {
            for (connectionSource in listDatastoreConnectionSource()) {
                connectionSource.embedded = true
                connectionSource.tenant = false
                new TConnectionSource(connectionSource).save(flush: true, failOnError: true)
                log.info "Installed datasource connection '${connectionSource.name}: ${connectionSource.url}'"
            }

        } else {
            for (connectionSource in list(filters: [embedded: false])) {
                log.info "Initializing datasource connection '${connectionSource.name}: ${connectionSource.url}'"
                connect(connectionSource)
            }
        }
    }

    private Boolean connectionSourceAlreadyInstalled() {
        return TConnectionSource.count()
    }

    DataSource getDataSource(String connectionSourceName) {
        return hibernateDatastore.getDatastoreForConnection(connectionSourceName).dataSource
    }

    List listAvailableDataSources() {
        return list().collect { it.name }
    }

    List<String> listAvailableDrivers() {
        List<String> results = []
        for (driver in DriverManager.drivers) {
            results.add(driver.getClass().getName())
        }
        return results.sort()
    }

    List<String> listAvailableSchemaGenerators() {
        return [
            'none',
            'create-only',
            'drop',
            'create',
            'create-drop',
            'validate',
            'update',
        ]
    }

    private Map buildConnectionSourceObject(ConnectionSource<SessionFactory, HibernateConnectionSourceSettings> connectionSource) {
        return [
            name           : connectionSource.name,
            driverClassName: connectionSource.settings.dataSource.driverClassName,
            dialect        : connectionSource.settings.dataSource.dialect?.name,
            dbCreate       : connectionSource.settings.dataSource.dbCreate,
            readOnly       : connectionSource.settings.dataSource.readOnly,
            username       : connectionSource.settings.dataSource.username,
            password       : connectionSource.settings.dataSource.password,
            url            : connectionSource.settings.dataSource.url,
        ]
    }

    private List<Map> listDatastoreConnectionSource() {
        List<Map> results = []
        for (connectionSource in hibernateDatastore.connectionSources) {
            Map record = buildConnectionSourceObject(connectionSource)
            results.add(record)
        }
        return results
    }

    void connect(TConnectionSource connectionSource) {
        hibernateDatastore.connectionSources.addConnectionSource(
            connectionSource.name as String, [
            'driverClassName'       : connectionSource.driverClassName,
            'dialect'               : connectionSource.dialect,
            'hibernate.hbm2ddl.auto': connectionSource.dbCreate,
            'readOnly'              : connectionSource.readOnly,
            'url'                   : connectionSource.url,
            'username'              : connectionSource.username,
            'password'              : connectionSource.password,
        ] as Map)
    }

    @CompileDynamic
    private DetachedCriteria<TConnectionSource> buildQuery(Map filters) {
        def query = TConnectionSource.where {}

        if (filters.containsKey('id')) query = query.where { id == filters.id }
        if (filters.containsKey('name')) query = query.where { name == filters.name }
        if (filters.containsKey('tenantId')) query = query.where { tenant == true && name == filters.tenantId }
        if (filters.containsKey('embedded')) query = query.where { embedded == filters.embedded }

        return query
    }

    TConnectionSource get(Serializable id) {
        return buildQuery(id: id).get()
    }

    TConnectionSource getByTenantId(String tenantId) {
        return buildQuery(tenantId: tenantId).get()
    }

    TConnectionSource getDefault() {
        return buildQuery(name: ConnectionSource.DEFAULT).get()
    }

    List<TConnectionSource> list(Map filterParams = [:], Map fetchParams = [:]) {
        if (!fetchParams.sort) fetchParams.sort = [name: 'asc']
        def query = buildQuery(filterParams)
        return query.list(fetchParams)
    }

    Number count(Map filters = [:]) {
        def query = buildQuery(filters)
        return query.count()
    }

    TConnectionSource create(Map args) {
        if (args.tenant == null) args.tenant = false
        if (args.embedded == null) args.embedded = false
        if (args.readOnly == null) args.readOnly = false
        if (args.dbCreate == null) args.dbCreate = 'none'
        if (args.failOnError == null) args.failOnError = false

        TConnectionSource obj = new TConnectionSource(args)

        obj.validate()
        if (!obj.hasErrors()) {
            connect(obj)
        }

        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    @CompileDynamic
    @Requires({ args.id })
    TConnectionSource update(Map args) {
        if (args.failOnError == null) args.failOnError = false

        TConnectionSource obj = get(args.id)
        obj.properties = args

        obj.validate()
        if (!obj.hasErrors()) {
            connect(obj)
        }

        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    void delete(Serializable id) {
        TConnectionSource obj = get(id)
        obj.delete(flush: true)
    }
}
