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

import dueuno.commons.utils.FileUtils
import dueuno.elements.ElementsException
import dueuno.elements.LinkGeneratorAware
import dueuno.elements.core.Elements
import dueuno.elements.core.Feature
import dueuno.elements.core.PrettyPrinter
import dueuno.elements.core.Transformer
import dueuno.tenant.TenantService
import dueuno.utils.EnvUtils
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.servlet.mvc.GrailsHttpSession
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import jakarta.servlet.ServletContext
import org.grails.io.support.PathMatchingResourcePatternResolver
import org.grails.io.support.Resource

/**
 * Application API
 *
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class ApplicationService implements LinkGeneratorAware {

    GrailsApplication grailsApplication
    ServletContext servletContext
    ConnectionSourceService connectionSourceService
    TenantService tenantService
    ApplicationPropertyService applicationPropertyService

    private static Map<String, List> credits = [:]

    /**
     * Returns an application scoped variable
     *
     * @param name The name of the attribute
     *
     * @return The application variable
     */
    Object getAttribute(String name) {
        return servletContext.getAttribute(name)
    }

    /**
     * Sets an application scoped variable
     *
     * @param name The name of the variable to set
     *
     * @param value The value of the variable to set
     */
    void setAttribute(String name, Object value) {
        servletContext.setAttribute(name, value)
    }

    /**
     * Returns true if the specified plugin is installed
     * @return true if the specified plugin is installed
     */
    Boolean hasPlugin(String name) {
        return Holders.pluginManager.hasGrailsPlugin(name)
    }

    /**
     * Enables third party components implementations.
     * See BootStrap.groovy in the 'elements-extra' project
     *
     * @param componentsImplementation The path to the components implementation assets (.css and .js main files),
     * Eg: /thirdparty/my-components (see "Elements Extra" implementation)
     */
    void registerComponents(String componentsImplementation) {
        Elements.registerComponents(componentsImplementation)
    }

    /**
     * Starts the application. There can be only one init() call in the Bootstrap.groovy
     * of the application.
     *
     * @param closure a closure containing specific application initialisation directives
     */
    void onInit(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        performInstallation()
        registerLanguages()
        registerOnInit(closure)
        startApplication()
    }

    void performInstallation() {
        connectionSourceService.installOrConnect()

        if (!applicationInstalled) {
            executeOnPluginInstall()
            executeOnInstall()
        }

        tenantService.provisionAllTenants()
        tenantService.updateAllTenants()
    }

    /**
     * Starts the application
     */
    void startApplication() {
        log.info ""
        log.info "-" * 78
        log.info "STARTING UP"
        log.info "-" * 78

        performInitialization()

        executeBeforeInit()
        executeOnInit()
        executeAfterInit()

        executeBeforeTenantInit()
        executeOnTenantInit()
        executeAfterTenantInit()

        performFinalization()
    }

    /**
     * Registers one or more closures to a given list. Registered closures can then be executed calling
     * executeBootEvents(String listName)
     *
     * @param listName List name
     * @param revisionName Name of the revision
     * @param closure Closure to add to the script list
     */
    void registerBootEvent(String listName, String revisionName, Closure closure) {
        String revision = closure.getClass().getPackage().name + '.' + listName
        if (revisionName) revision += '(' + revisionName + ')'

        log.trace "Registering event '${revision}'"
        closure.delegate = this

        Map<String, Closure> listEvents = getBootEvents(listName)
        listEvents.put(revision, closure)
        setAttribute(listName, listEvents)
    }

    void registerBootEvent(String listName, Closure closure) {
        registerBootEvent(listName, null, closure)
    }

    /**
     * Returns if a list has registered events or not
     *
     * @param listName Name of the list to verify
     */
    Boolean hasBootEvents(String listName) {
        return getBootEvents(listName)
    }

    /**
     * Retrives the initialization closures list corresponding to list name
     *
     * @param listName
     *
     * @return the closure list; an empty list if not found
     */
    private Map<String, Closure> getBootEvents(String listName) {
        return (Map<String, Closure>) (getAttribute(listName) ?: [:])
    }

    /**
     * Used by plugins to register a System setup closure. The closure will be executed only once.
     * @param closure a closure containing specific setup code
     */
    void onInstall(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('onInstall', closure)
    }

    /**
     * Used by plugins to register a setup closure. The closure will be executed only once.
     * @param closure a closure containing specific setup code
     */
    void onPluginInstall(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('onPluginInstall', closure)
    }

    /**
     * Used by the application to register a setup closure. The closure will be executed only once.
     * @param closure a closure containing specific setup code
     */
    void onPluginTenantInstall(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('onPluginTenantInstall', closure)
    }

    /**
     * Used by the application to register a setup closure. The closure will be executed only once.
     * @param closure a closure containing specific setup code
     */
    void onTenantInstall(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('onTenantInstall', closure)
    }

    /**
     * Same as onTenantInstall() but only executed when in development environment
     * @param closure a closure containing specific setup code
     */
    void onDevInstall(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('onDevInstall', closure)
    }

    /**
     * Used by applications to register an update closure. The closure will be executed only once.
     * @param closure a closure containing specific update code
     */
    void onUpdate(String version, @DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('onUpdate', version, closure)
    }

    /**
     * Used by plugins to register a closure containing tenant pre-initialisation
     * @param closure a closure containing tenant initialisation
     */
    void beforeTenantInit(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('beforeTenantInit', closure)
    }

    /**
     * Used by plugins to register a closure containing tenant post-initialisation
     * @param closure a closure containing tenant initialisation
     */
    void afterTenantInit(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('afterTenantInit', closure)
    }

/**
 * Used by the application to register a closure containing tenant initialisation
 * @param closure a closure containing tenant initialisation
 */
    void onTenantInit(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('onTenantInit', closure)
    }

/**
 * Used by plugins to register a closure containing initialisation scripts.
 * @param closure a closure containing specific application pre-initialisation directives
 */
    void beforeInit(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('beforeInit', closure)
    }

    /**
     * Used by plugins to register a closure containing initialisation scripts.
     * @param closure a closure containing specific application post-initialisation directives
     */
    void afterInit(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('afterInit', closure)
    }

    /**
     * Used by plugins to register a closure containing their initialisation scripts.
     * @param closure a closure containing specific application initialisation directives
     */
    private void registerOnInit(@DelegatesTo(ApplicationService) Closure closure = { /* No setup */ }) {
        registerBootEvent('onInit', closure)
    }

    void executeOnInstall() {
        if (hasBootEvents('onInstall')) {
            log.info "-" * 78
            log.info "INSTALLING APPLICATION"
            log.info "-" * 78

            executeInstall('onInstall')
        }
    }

    void executeOnPluginInstall() {
        if (hasBootEvents('onPluginInstall')) {
            log.info "-" * 78
            log.info "INSTALLING PLUGINS"
            log.info "-" * 78

            executeInstall('onPluginInstall')
        }
    }

    void executeOnPluginTenantInstall() {
        String tenantId = tenantService.currentTenantId
        if (hasBootEvents('onPluginTenantInstall')) {
            log.info "-" * 78
            log.info "${tenantId} Tenant - SETTING UP PLUGINS"
            log.info "-" * 78

            executeInstall('onPluginTenantInstall', false, true)
        }
    }

    void executeOnTenantInstall() {
        String tenantId = tenantService.currentTenantId
        if (hasBootEvents('onTenantInstall') || hasBootEvents('onDevInstall')) {
            log.info "-" * 78
            log.info "${tenantId} Tenant - SETTING UP APPLICATION"
            log.info "-" * 78

            executeInstall('onTenantInstall')
            if (EnvUtils.isDevelopment()) {
                executeInstall('onDevInstall', true)
            }
        }
    }

    void executeOnUpdate() {
        String tenantId = tenantService.currentTenantId
        if (hasBootEvents('onUpdate')) {
            log.info "-" * 78
            log.info "${tenantId} Tenant - UPDATING APPLICATION"
            log.info "-" * 78

            executeInstall('onUpdate', false, true)
        }
    }

    @Transactional
    @CompileDynamic
    Boolean getApplicationInstalled() {
        return TApplicationInstall.count() > 0
    }

    @Transactional
    @CompileDynamic
    Boolean isPluginInstalled(String pluginName) {
        return TApplicationInstall.where { plugin == pluginName }.count() > 0
    }

    @Transactional
    @CompileDynamic
    private void executeInstall(String listName, Boolean isDev = false, Boolean sort = false) {
        String tenantId = tenantService.currentTenantId
        Map<String, Closure> eventList = getBootEvents(listName)
        Map revisionList = sort ? eventList.sort() : eventList

        for (revision in revisionList) {
            String revisionName = revision.key
            Closure closure = revision.value

            String pluginName = closure.getClass().getPackage().getName()
            Integer isInstalled = TApplicationInstall.countByPluginAndRevisionAndTenantIdAndDev(pluginName, revisionName, tenantId, isDev)

            if (isInstalled) {
                continue
            }

            log.info "${tenantId} Tenant - Executing '${revisionName}'..."

            if (closure.maximumNumberOfParameters == 1) {
                closure.call(tenantId)
            } else {
                closure.call(tenantId, pluginName)
            }

            new TApplicationInstall(
                plugin: pluginName,
                revision: revisionName,
                tenantId: tenantId,
                dev: isDev,
            ).save(flush: true, failOnError: true)

            log.info "...done."
            log.info ""
        }
    }

    /**
     * Executes the scripts registered by registerBootEvent()
     *
     * @param listName Name of the list to execute
     */
    void executeBootEvents(String listName, GrailsHttpSession session = null) {
        String tenantId = tenantService.currentTenantId
        Map<String, Closure> eventList = getBootEvents(listName)
        for (revision in eventList) {
            String revisionName = revision.key
            Closure closure = revision.value

            log.info "${tenantId} Tenant - Executing '${revisionName}'..."
            if (closure.maximumNumberOfParameters == 1) {
                closure.call(tenantId)
            } else {
                closure.call(tenantId, session)
            }
        }
    }

    private void executeBeforeInit() {
        executeBootEvents('beforeInit')
    }

    private void executeOnInit() {
        executeBootEvents('onInit')
    }

    private void executeAfterInit() {
        executeBootEvents('afterInit')
    }

    private void executeBeforeTenantInit() {
        tenantService.eachTenant { String tenantId ->
            executeBootEvents('beforeTenantInit')
        }
    }

    private void executeOnTenantInit() {
        tenantService.eachTenant { String tenantId ->
            executeBootEvents('onTenantInit')
        }
    }

    private void executeAfterTenantInit() {
        tenantService.eachTenant { String tenantId ->
            executeBootEvents('afterTenantInit')
        }
    }

    void registerCredits(String task, String... people) {
        credits[task] = people as List<String>
    }

    /**
     * Returns the credits configured for the application
     * @return the credits configured for the application
     */
    Map getCredits() {
        return credits
    }

    String getApplicationName() {
        return grailsApplication.config.getProperty('info.app.name', String)
    }

    String getApplicationDir() {
        return "${FileUtils.workingDirectory}${applicationName}/"
    }

    private void performInitialization() {
        setAttribute('mainFeatures', new Feature(controller: 'mainFeaturesRoot'))
        setAttribute('userFeatures', new Feature(controller: 'userFeaturesRoot'))

        registerUserMenuLanguages()
    }

    private void performFinalization() {
        registerUserMenuCredits()
    }

    void registerPrettyPrinter(String name, String template) {
        PrettyPrinter.register(name, template)
    }

    void registerPrettyPrinter(Class clazz, String template) {
        PrettyPrinter.register(clazz, template)
    }

    void registerTransformer(String name, Closure transformer) {
        Transformer.register(name, transformer)
    }

    private void registerUserMenuLanguages() {
        for (language in languages) {
            registerUserFeature(
                text: 'default.language.' + language,
                image: 'libs/flags/' + getFlagCode(language) + '.svg',
                controller: 'shell',
                action: 'switchLanguage',
                params: [id: language],
            )
        }

        registerUserFeature()
    }

    private void registerUserMenuCredits() {
        if (!credits) {
            return
        }

        registerUserFeature(
            text: 'shell.user.menu.credits',
            icon: 'fa-circle-info',
            controller: 'shell',
            action: 'credits',
        )

        registerUserFeature()
    }

    Feature getMainFeatures() {
        return getAttribute('mainFeatures') as Feature
    }

    Feature getUserFeatures() {
        return getAttribute('userFeatures') as Feature
    }

    Feature getFeature(String controller) {
        return mainFeatures.byController(controller)
    }

    Feature getAdminFeature() {
        return mainFeatures.byController('admin')
    }

    Feature getSuperadminFeature() {
        return mainFeatures.byController('superadmin')
    }

    Feature getUserFeature(String controller) {
        return userFeatures.byController(controller)
    }

    /**
     * Registers an application feature.
     *
     * @param controller the controller name this menu item points to
     * @param action the action name this menu item points to
     * @param icon an icon to display, please chose one from <a target="_blank" href="http://fontawesome.io">fontawesome.io</a> (eg. <code>'calendar'</code>)
     * @param order display order, each new item is created with a step of 10 (eg. 10, 20, 30, etc.)
     * @param authorities a list of authorities the menu item will be accessible for (eg: <code>['ROLE_CAN_EDIT_ORDERS', 'ROLE_CAN_EDIT_PRODUCTS']</code>).
     * @param parent the name of the controller of the parent menu item (used to create sub items)
     *
     */
    Feature registerFeature(Map args = [:]) {
        args.authorities = (List) args.authorities ?: []

        Feature parent = mainFeatures.byController(args.parent as String)
        if (parent) {
            args.parent = parent
            return parent.add(args)

        } else {
            return mainFeatures.add(args)
        }
    }

    /**
     * Registers a feature into the User Menu.
     */
    Feature registerUserFeature(Map args = [:]) {
        args.authorities = args.authorities ?: []
        args.name = args.controller ?: args.order ?: (Math.random() * 1000).toInteger().toString()
        return userFeatures.add(args)
    }

    /**
     * Registers a feature into the "Development" user menu area.
     * Registers a feature accessible only by the ROLE_DEVELOPER authority.
     */
    void registerDeveloperUserFeature(Map args = [:]) {
        args.authorities = ['ROLE_DEVELOPER']
        registerUserFeature(args)
    }

    /**
     * Registers a feature into the "Application Management" area.
     * Registers a feature accessible only by the ROLE_SUPERADMIN authority.
     */
    void registerSuperadminFeature(Map args = [:]) {
        Feature parent = getSuperadminFeature()
        if (!parent) {
            throw new ElementsException("Cannot register Superadmin Feature '${args.controller}': applications must register features in the 'onInit' event. Plugins in the 'afterInit' event.")
        }

        Feature newFeature = getFeature(args.controller as String)
        if (!newFeature) {
            args.parent = parent.controller
            args.authorities = ['ROLE_SUPERADMIN']
            registerFeature(args)
        }
    }

    /**
     * Registers a feature into the "Tenant Management" area.
     * To be used when creating features accessible only by users with the ROLE_ADMIN authority.
     */
    void registerAdminFeature(Map args = [:]) {
        Feature parent = getAdminFeature()
        if (!parent) {
            throw new ElementsException("Cannot register Admin Feature '${args.controller}': applications must register features in the 'onInit' event. Plugins in the 'afterInit' event.")
        }

        Feature newFeature = getFeature(args.controller as String)
        if (!newFeature) {
            args.parent = parent.controller
            args.authorities = ['ROLE_ADMIN']
            registerFeature(args)
        }
    }

    private void registerLanguages() {
        List<String> available = getLanguagesFromResources()
        applicationPropertyService.setString('AVAILABLE_LANGUAGES', available.join(','))

        // Exclusions
        List<String> excluded = applicationPropertyService.getString('EXCLUDED_LANGUAGES').split(',') as List<String>

        // Main check
        String main = applicationPropertyService.getString('DEFAULT_LANGUAGE')
        if (main !in available) {
            throw new ElementsException("""
                The language '${main}' does not have a corresponding '/i18n/messages_${main}.properties' file,
                please provide one or choose a default from ${available} then try again.
            """)
        }

        log.info "Available languages ${available}, excluded ${excluded}, default '${main}'"
    }

    List<String> getLanguages() {
        String[] availableLanguages = applicationPropertyService.getString('AVAILABLE_LANGUAGES').split(',')
        String[] excludedLanguages = applicationPropertyService.getString('EXCLUDED_LANGUAGES').split(',')
        return (availableLanguages - excludedLanguages) as List<String>
    }

    /**
     * Retrieves the list of languages available to the application
     */
    private List<String> getLanguagesFromResources() {
        def rpr = new PathMatchingResourcePatternResolver()
        Resource[] resources = rpr.getResources('classpath*:*.properties')
        Collection<Resource> languageFiles = resources.findAll { resource ->
            if (isValidMessageFile(resource)) {
                log.trace "Found language: '${resource.URL}'"
                return true
            } else {
                return false
            }
        }

        List<String> languages = languageFiles.collect { resource ->
            def filename = resource.filename.toLowerCase()
            if (filename == 'messages.properties') {
                filename = 'en'
            } else {
                filename = filename - 'messages_' - '.properties'
            }
            return filename
        }

        return languages.unique().sort()
    }

    private Boolean isValidMessageFile(Resource resource) {
//        log.trace "Analyzing resource: ${resource.URL.toString()}"
        String path = resource.URL.toString()
        String filename = resource.filename

        // When deployed as executable .jar or .war
        if (path.startsWith('jar:file:')) {
            if (path.contains('/BOOT-INF/classes!') || path.contains('/WEB-INF/classes!')) {
                return filename.startsWith('messages')
            } else {
                return false
            }

        } else {
            return filename.startsWith('messages')
        }
    }

    private String getFlagCode(String lang) {
        // Per riferimento:
        // - Java Locale docs:                     https://docs.oracle.com/javase/7/docs/api/java/util/Locale.html
        // - Icon library:                         https://www.flaticon.com/packs/countrys-flags
        // - Locale uses ISO 639 Alpha 2 codes:    https://www.loc.gov/standards/iso639-2/php/English_list.php
        // - Flags uses ISO 3166-1 Alpha 2 codes:  https://www.iso.org/obp/ui/
        Map localeToFlag = [
            en   : 'gb',    // defaults to UK (cause we're european ;-)
            en_gb: 'gb',
            en_us: 'us',
            pt_pt: 'pt',
            pt_br: 'br',
            zh_cn: 'cn',
            cs   : 'cs_cz',
            da   : 'dk',
            ja   : 'jp',
            nb   : 'no',
        ]
        String langLowerCase = lang.toLowerCase()
        String flagLang = localeToFlag[langLowerCase] ?: langLowerCase
        // println "Locale: '${lang}' -> Flag: '${flagLang}'"
        return flagLang
    }
}
