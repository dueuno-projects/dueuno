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
package dueuno

import dueuno.application.AuthenticationProviderService
import dueuno.application.SessionInitializer
import dueuno.security.*
import dueuno.security.physical.PhysicalAuthenticationFilter
import dueuno.security.physical.PhysicalUserDetailsService
import dueuno.tenant.TenantForCurrentUserResolver
import grails.core.GrailsApplication
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.BeanRegistrar
import org.springframework.beans.factory.BeanRegistry
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.security.web.context.SecurityContextRepository

/**
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
@EnableScheduling
class DueunoGrailsPlugin extends Plugin {

    static final String NAME = 'dueuno-core'

    def version = '4.x-SNAPSHOT'
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "8.0.0  > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        'grails-app/views/error.gsp'
    ]

    def title = 'Dueuno' // Headline display name of the plugin
    def author = 'Gianluca Sartori, Francesco Piceghello'
    def authorEmail = 'g.sartori@gmail.com, f.piceghello@gmail.com'
    def description = 'Dueuno Core'
    def profiles = ['web']
    def loadAfter = ['springSecurityCore']

    // URL to the plugin's documentation
    def documentation = 'https://www.dueuno.com/docs'

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = 'APACHE'

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: 'My Company', url: 'http://www.my-company.com/' ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: 'Joe Bloggs', email: 'joe@bloggs.net' ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: 'JIRA', url: 'http://jira.grails.org/browse/GPMYPLUGIN' ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: 'http://svn.codehaus.org/grails-plugins/' ]

    @Override
    BeanRegistrar beanRegistrar() {
        return { BeanRegistry registry, Environment environment ->
            registry.registerBean('tenantForCurrentUserResolver', TenantForCurrentUserResolver)
            registry.registerBean('sessionInitializer', SessionInitializer)

            registry.registerBean('userSessionTomcatContextListener', UserSessionTomcatContextListener) { spec ->
                spec.supplier { context ->
                    UserSessionTomcatContextListener bean = new UserSessionTomcatContextListener()
                    bean.userSessionService = context.bean('userSessionService', UserSessionService)
                    bean
                }
            }

            registry.registerBean('runtimeAuthenticationProvider', AuthenticationProviderManager) { spec ->
                spec.supplier { context ->
                    AuthenticationProviderManager bean = new AuthenticationProviderManager()
                    bean.authenticationProviderService = context.bean(
                        'authenticationProviderService', AuthenticationProviderService)
                    bean.grailsApplication = context.bean('grailsApplication', GrailsApplication)
                    bean.securityService = context.bean('securityService', SecurityService)
                    bean
                }
            }

            registry.registerBean('ldapAuthenticationProvider', LdapAuthenticationProvider) { spec ->
                spec.supplier { context ->
                    LdapAuthenticationProvider bean = new LdapAuthenticationProvider()
                    bean.authenticationProviderService = context.bean(
                        'authenticationProviderService', AuthenticationProviderService)
                    bean.authenticationUserProvisioningService = context.bean(
                        'authenticationUserProvisioningService', AuthenticationUserProvisioningService)
                    bean
                }
            }

            registry.registerBean('physicalUserDetailsService', PhysicalUserDetailsService)

            registry.registerBean('physicalAuthenticationProvider', PreAuthenticatedAuthenticationProvider) { spec ->
                spec.supplier { context ->
                    PreAuthenticatedAuthenticationProvider bean = new PreAuthenticatedAuthenticationProvider()
                    bean.preAuthenticatedUserDetailsService = context.bean('physicalUserDetailsService', PhysicalUserDetailsService)
                    bean
                }
            }

            registry.registerBean('physicalAuthenticationFilter', PhysicalAuthenticationFilter) { spec ->
                spec.supplier { context ->
                    ConfigObject securityConfig = SpringSecurityUtils.securityConfig as ConfigObject
                    ConfigObject physicalIdConfig = securityConfig.get('physicalId') as ConfigObject
                    String filterProcessesUrl = physicalIdConfig.get('filterProcessesUrl') as String
                    PhysicalAuthenticationFilter bean = new PhysicalAuthenticationFilter(filterProcessesUrl)
                    bean.authenticationManager = context.bean('authenticationManager', AuthenticationManager)
                    bean.authenticationSuccessHandler = context.bean(
                        'authenticationSuccessHandler', AuthenticationSuccessHandler)
                    bean.authenticationFailureHandler = context.bean(
                        'authenticationFailureHandler', AuthenticationFailureHandler)
                    bean.sessionAuthenticationStrategy = context.bean(
                        'sessionAuthenticationStrategy', SessionAuthenticationStrategy)
                    bean.securityContextRepository = context.bean(
                        'securityContextRepository', SecurityContextRepository)
                    bean
                }
            }
        } as BeanRegistrar
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
