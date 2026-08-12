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
package test

import dueuno.application.ApplicationPropertyService
import dueuno.application.ApplicationService
import dueuno.application.ConnectionSourceService
import dueuno.commons.utils.FileUtils
import dueuno.elements.TransitionService
import dueuno.elements.pages.ShellService
import dueuno.security.SecurityService
import dueuno.tenant.TenantPropertyService
import dueuno.tenant.TenantService
import dueuno.types.QuantityService
import grails.web.servlet.mvc.GrailsHttpSession
import jakarta.servlet.ServletContext

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService
    TestService testService
    QuantityService quantityService
    ApplicationPropertyService applicationPropertyService
    TenantPropertyService tenantPropertyService
    TenantService tenantService
    SecurityService securityService
    ConnectionSourceService connectionSourceService
    ShellService shellService
    TransitionService transitionService

    def init = {

        applicationService.onUpdate('2021-10-03') { String tenantId ->
            println "${tenantId} Tenant - UPDATE N.2"
        }

        applicationService.onUpdate('2021-10-02') { String tenantId ->
            println "${tenantId} Tenant - UPDATE N.1"
        }

        applicationService.onUpdate('2021-10-05') { String tenantId ->
            println "${tenantId} Tenant - UPDATE N.4"
        }

        applicationService.onUpdate('2021-10-04') { String tenantId ->
            println "${tenantId} Tenant - UPDATE N.3"
        }

        applicationService.onInstall {
            applicationPropertyService.setBoolean('DISPLAY_MENU', true)
            applicationPropertyService.setString('DEFAULT_LANGUAGE', 'it')
            applicationPropertyService.setString('EXCLUDED_LANGUAGES', 'de,pi')
            applicationPropertyService.setString('TEST_JOB_PROPERTY', 'CHANGE ME')

            connectionSourceService.create(
                name: 'dynamicDatasource',
                driverClassName: 'org.h2.Driver',
                dbCreate: 'update',
                username: 'sa',
                password: '',
                url: 'jdbc:h2:mem:DYNAMIC_CONNECTION;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=TRUE',
            )

            tenantService.create(
                tenantId: 'TEST',
                description: 'Test tenant',
                host: 'localhost:9443',
                failOnError: true,
                connectionSource: [
                    driverClassName: 'com.mysql.cj.jdbc.Driver',
                    url            : 'jdbc:mysql://localhost:3306/dueuno_test?useSSL=false&createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Europe/Rome',
                    username       : 'root',
                    password       : 'root',
                ]
            )
        }

        applicationService.onTenantInstall { String tenantId ->
            tenantPropertyService.setNumber('TEST_NUMBER', 5 * 60)
            tenantPropertyService.setString('TEST_STRING', 'This is a string', 'This is its default value')
            tenantPropertyService.setDateTime('TEST_DATE_TIME', LocalDateTime.now())
            tenantPropertyService.setDate('TEST_DATE', LocalDate.now())
            tenantPropertyService.setTime('TEST_TIME', LocalTime.now())
            tenantPropertyService.setBoolean('TEST_BOOLEAN', true)
            tenantPropertyService.setDirectory('TEST_DIRECTORY', '', '/pippo/pluto')
            tenantPropertyService.setFilename('TEST_FILENAME', '', '\\pippo\\pluto\\config.txt')
            tenantPropertyService.setUrl('TEST_URL', 'http://www.dueuno.com', 'http://www.google.com/search')
            tenantPropertyService.setDirectory('TEST_WRONG_DIRECTORY', '\\this\\directory\\doesnt\\exist', '')
            tenantPropertyService.setFilename('TEST_WRONG_FILENAME', '\\this\\file\\doesnt\\exist.txt', '')
            tenantPropertyService.setUrl('TEST_WRONG_URL', 'htp://www.wrong.url', '')
            tenantPropertyService.setString('TEST_ON_CHANGE', 'Change me and check the console!', '')
            tenantPropertyService.setPassword('TEST_PASSWORD', "${tenantId}Password")

            String appLink = servletContext.contextPath
            tenantPropertyService.setString('SHELL_URL_MAPPING', '/admin')
            tenantPropertyService.setString('LOGIN_LANDING_URL', '/')
            tenantPropertyService.setString('LOGIN_REGISTRATION_URL', appLink + '/crud/register')
            tenantPropertyService.setString('LOGIN_PASSWORD_RECOVERY_URL', appLink + '/crud/passwordRecovery')
            tenantPropertyService.setString(
                'LOGIN_COPY',
                'Copyright &copy; Dueuno.<br/>All rights reserved.<br/><br/><a href="' + appLink + '">&lt;&lt; Back to website</a>'
            )

            securityService.updateGroup(
                failOnError: true,
                tenantId: tenantId,
                name: 'USERS',
                landingPage: 'table',
            )
            securityService.createGroup(
                failOnError: true,
                tenantId: tenantId,
                name: 'PROJECT MANAGER',
                authorities: ['ROLE_PROJECT_MANAGER'],
            )
            securityService.createUser(
                failOnError: true,
                tenantId: tenantId,
                firstname: "Developer",
                username: tenantId == tenantService.defaultTenantId ? 'dev' : "${tenantId}/dev",
                password: 'dev',
                groups: ['DEVELOPERS', 'PROJECT MANAGER'],
            )

            FileUtils.createDirectory(tenantService.publicDir + 'upload')

            quantityService.enableUnitLength(['KM', 'M'])

            // Creating a new tenant from the GUI gives an error here, to solve it you need to
            // remove the 'spring-dev-tools' dependency in build.gradle
            // See: https://github.com/spring-projects/spring-data-jpa/issues/2552
            testService.installDemo()
        }

        applicationService.onDevInstall { String tenantId ->
            println "${tenantId} Tenant - INSTALLING STUFF ONLY WHEN IN DEVELOPMENT ENVIRONMENT"

            applicationPropertyService.setBoolean('TEST_DENY_LOGIN', false)

            applicationPropertyService.setNumber('TEST_NUMBER', 5 * 60)
            applicationPropertyService.setString('TEST_STRING', 'This is a string', 'This is its default value')
            applicationPropertyService.setDateTime('TEST_DATE_TIME', LocalDateTime.now())
            applicationPropertyService.setDate('TEST_DATE', LocalDate.now())
            applicationPropertyService.setTime('TEST_TIME', LocalTime.now())
            applicationPropertyService.setBoolean('TEST_BOOLEAN', true)
            applicationPropertyService.setDirectory('TEST_DIRECTORY', '', '/pippo/pluto')
            applicationPropertyService.setFilename('TEST_FILENAME', '', '\\pippo\\pluto\\config.txt')
            applicationPropertyService.setUrl('TEST_URL', 'http://www.dueuno.com', 'http://www.google.com/search')
            applicationPropertyService.setDirectory('TEST_WRONG_DIRECTORY', '\\this\\directory\\doesnt\\exist', '')
            applicationPropertyService.setFilename('TEST_WRONG_FILENAME', '\\this\\file\\doesnt\\exist.txt', '')
            applicationPropertyService.setUrl('TEST_WRONG_URL', 'htp://www.wrong.url', '')
            applicationPropertyService.setString('TEST_ON_CHANGE', 'Change me and check the console!', '')

            securityService.createAuthority('ROLE_1')
            securityService.createAuthority('ROLE_2')
            securityService.createAuthority('ROLE_3')

            securityService.createGroup(
                tenantId: tenantId,
                name: "GROUP_1",
                authorities: ['ROLE_1'],
            )
            securityService.createGroup(
                tenantId: tenantId,
                name: "GROUP_2",
                authorities: ['ROLE_2'],
            )
            securityService.createGroup(
                tenantId: tenantId,
                name: "GROUP_3",
                authorities: ['ROLE_3'],
            )

            securityService.createUser(
                tenantId: tenantId,
                username: tenantId == tenantService.defaultTenantId ? 'user1' : "${tenantId}/user1",
                password: 'user1',
                groups: ['GROUP_1'],
                email: 'user@company.it',
                firstname: 'User',
                lastname: 'ONE',
                physicalId: tenantId == tenantService.defaultTenantId ? 'FB78E50B' : null,
            )
            securityService.createUser(
                tenantId: tenantId,
                username: tenantId == tenantService.defaultTenantId ? 'user2' : "${tenantId}/user2",
                password: 'user2',
                groups: ['GROUP_2', 'GROUP_1'],
                email: 'user@company.it',
                firstname: 'User',
                lastname: 'ONE TWO',
            )
            securityService.createUser(
                tenantId: tenantId,
                username: tenantId == tenantService.defaultTenantId ? 'user3' : "${tenantId}/user3",
                password: 'user3',
                groups: ['GROUP_3'],
                email: 'user@company.it',
                firstname: 'User',
                lastname: 'THREE',
            )

            securityService.createSystemUser(
                username: '*',
                firstname: 'Nessun',
                lastname: 'Utente',
            )
        }

        securityService.afterLogin { String tenantId, GrailsHttpSession session ->
            println "${tenantId}: Benvenuto in ${shellService.shell.id} caro ${securityService.currentUsername}"
            if (applicationPropertyService.getBoolean('TEST_DENY_LOGIN', true)) {
                securityService.denyLogin('Cannot execute login because of X reason')
            }
        }

        securityService.afterLogout { String tenantId ->
            println "${tenantId}: Arrivederci ${securityService.currentUsername}!"
        }

        applicationService.onTenantInit { String tenantId ->
            println "INITIALIZING ${tenantId} Tenant"
        }

        applicationService.onInit {
            applicationService.registerPrettyPrinter(TPerson, '${it.name}')
            applicationService.registerPrettyPrinter(TCompany, '${it.name}')
            applicationService.registerPrettyPrinter('customCompanyPrinter', '${it.name} (${it.dateCreated})')

            transitionService.subscribe('macchina-1')
            transitionService.subscribe('macchina-2')

            applicationPropertyService.onChange('TEST_ON_CHANGE') { Object oldValue, Object value, Object defaultValue ->
                println "*** TEST_ON_CHANGE *************************************************************"
                println "OLD VALUE: $oldValue"
                println "VALUE: $value"
                println "DEFAULT VALUE: $defaultValue"
                println "********************************************************************************"
            }

            // APPLICATION CONFIGURATION
            //
            applicationService.registerCredits('Created by', 'Gianluca Sartori', 'Francesco Piceghello')
            applicationService.registerCredits('Tested by', 'Gianluca Sartori', 'Francesco Piceghello')
            applicationService.registerCredits('Sponsored by', 'Gianluca Sartori', 'Francesco Piceghello')
            applicationService.registerCredits('Dreamed by', 'Gianluca Sartori', 'Francesco Piceghello')

            //
            // FEATURES
            //

            applicationService.registerUserFeature(
                controller: 'sandbox',
                icon: 'fa-book',
                text: 'Manual...',
            )

            applicationService.registerFeature(
                controller: 'crud',
                icon: 'fa-file',
                favourite: true,
            )
            applicationService.registerFeature(
                controller: 'crudCustom',
                icon: 'fa-regular fa-file',
            )
            applicationService.registerFeature(
                controller: 'crudDataServices',
                icon: 'fa-file-circle-check',
            )
            applicationService.registerFeature(
                controller: 'crudSql',
                icon: 'fa-database',
            )
//            applicationService.registerFeature(
//                    controller: 'crudRest',
//                    icon: 'fa-cloud',
//                    authorities: ['ROLE_USER']
//            )

            applicationService.registerFeature(
                controller: 'elements',
                favourite: true,
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'sandbox',
                icon: 'fa-umbrella-beach',
                favourite: true,
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'table',
                icon: 'fa-table',
                favourite: true,
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'tableStressTest',
                icon: 'fa-table',
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'form',
                icon: 'fa-check-square',
                favourite: true,
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'select',
                icon: 'fa-table-list',
                favourite: true,
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'grid',
                icon: 'fa-border-none',
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'tabs',
                icon: 'fa-ellipsis-h',
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'timer',
                icon: 'fa-hourglass',
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'transitions',
                icon: 'fa-wand-sparkles',
                modal: true,
                favourite: true,
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'websocket',
                icon: 'fa-envelope',
                favourite: true,
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'charts',
                icon: 'fa-chart-pie',
                favourite: true,
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'upload',
                icon: 'fa-upload',
                favourite: true,
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'staticResources',
                icon: 'fa-images',
            )
            applicationService.registerFeature(
                parent: 'elements',
                controller: 'customPage',
                icon: 'fa-palette',
                direct: true,
            )

            applicationService.registerFeature(
                controller: 'errorMessages',
            )
            applicationService.registerFeature(
                parent: 'errorMessages',
                controller: 'handledException',
                icon: 'fa-regular fa-comment',
            )
            applicationService.registerFeature(
                parent: 'errorMessages',
                controller: 'denied',
                icon: 'fa-user-lock',
            )
            applicationService.registerFeature(
                parent: 'errorMessages',
                controller: 'notFound',
                icon: 'fa-search-location',
            )
            applicationService.registerFeature(
                parent: 'errorMessages',
                controller: 'exception',
                icon: 'fa-solid fa-bug',
            )

            applicationService.registerFeature(
                controller: 'authDemo',
                icon: 'fa-group',
                favourite: true,
                authorities: ['ROLE_1', 'ROLE_2', 'ROLE_3']
            )
            applicationService.registerFeature(
                parent: 'authDemo',
                controller: 'user1',
                icon: 'fa-users',
                favourite: true,
                authorities: ['ROLE_1']
            )
            applicationService.registerFeature(
                parent: 'authDemo',
                controller: 'user2',
                icon: 'fa-users',
                favourite: true,
                authorities: ['ROLE_2']
            )
            applicationService.registerFeature(
                parent: 'authDemo',
                controller: 'user3',
                icon: 'fa-users',
                favourite: true,
                authorities: ['ROLE_3']
            )
        }
    }

    def destroy = {
        //no-op
    }

}
