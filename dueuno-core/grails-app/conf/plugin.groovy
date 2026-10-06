// H2 Console enabled by default (protected by Spring Security, only "superadmin" can access it)
spring.h2.console.enabled = true

// Uses custom table naming
hibernate.naming_strategy = dueuno.database.TNamingStrategy

// Tenants are stored in separate database schemas
grails.gorm.multiTenancy.mode = 'DATABASE'

// Spring Security Core plugin setup example
grails.plugin.springsecurity.userLookup.userDomainClassName = 'dueuno.security.TUser'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'dueuno.security.TUserRoleGroup'
grails.plugin.springsecurity.authority.className = 'dueuno.security.TRole'
grails.plugin.springsecurity.authority.groupAuthorityNameField = 'authorities'
grails.plugin.springsecurity.useRoleGroups = true
grails.plugin.springsecurity.roleHierarchyEntryClassName = 'dueuno.security.TRoleHierarchyEntry'
grails.plugin.springsecurity.auth.loginFormUrl = '/authentication/login'
grails.plugin.springsecurity.apf.filterProcessesUrl = '/authentication/authenticate' // See Login.js
grails.plugin.springsecurity.successHandler.alwaysUseDefault = true
grails.plugin.springsecurity.successHandler.defaultTargetUrl = '/authentication/afterLogin'
grails.plugin.springsecurity.successHandler.ajaxSuccessUrl = '/authentication/afterLogin?ajax=true'
grails.plugin.springsecurity.failureHandler.defaultFailureUrl = '/authentication/login?login_error=1'
grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.logout.invalidateHttpSession = false
grails.plugin.springsecurity.logout.afterLogoutUrl = '/authentication/afterLogout'
grails.plugin.springsecurity.logout.filterProcessesUrl = '/springSecurityLogout'
grails.plugin.springsecurity.adh.errorPage = '/authentication/denied'
grails.plugin.springsecurity.physicalId.filterProcessesUrl = '/api/auth/physical'
grails.plugin.springsecurity.physicalId.propertyName = 'physicalId'

// Prevent Session Fixation attacks
grails.plugin.springsecurity.useSessionFixationPrevention = true

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: '/**/authentication/login', access: ['permitAll']],
        [pattern: '/**/login', access: ['permitAll']],
        [pattern: '/**/authentication/logout', access: ['permitAll']],
        [pattern: '/**/logout', access: ['permitAll']],
        [pattern: '/**', access: ['IS_AUTHENTICATED_REMEMBERED']],
        [pattern: '/**/h2-console/**', access: ['ROLE_DEVELOPER']],

        // Websocket
        [pattern: '/queue/**', access: ['permitAll']],
        [pattern: '/stomp/**', access: ['permitAll']],

        [pattern: '/error', access: ['permitAll']],
        [pattern: '/shutdown', access: ['permitAll']],
        [pattern: '/assets/**', access: ['permitAll']],
        [pattern: '/**/js/**', access: ['permitAll']],
        [pattern: '/**/css/**', access: ['permitAll']],
        [pattern: '/**/images/**', access: ['permitAll']],
        [pattern: '/**/favicon.png', access: ['permitAll']],
        [pattern: '/**/appicon.png', access: ['permitAll']],
        [pattern: '/**/pwa/register.js', access: ['permitAll']],
        [pattern: '/**/pwa/service-worker.js', access: ['permitAll']],
]

grails.plugin.springsecurity.filterChain.chainMap = [
        [pattern: '/assets/**', filters: 'none'],
        [pattern: '/**/js/**', filters: 'none'],
        [pattern: '/**/css/**', filters: 'none'],
        [pattern: '/**/images/**', filters: 'none'],
        [pattern: '/**/favicon.png', filters: 'none'],
        [pattern: '/**/appicon.png', filters: 'none'],
        [pattern: grails.plugin.springsecurity.physicalId.filterProcessesUrl, filters: 'physicalIdAuthenticationFilter'],
        [pattern: '/**', filters: 'JOINED_FILTERS,-physicalIdAuthenticationFilter']
]

grails.plugin.springsecurity.providerNames = [
        'runtimeAuthenticationProvider',
]
