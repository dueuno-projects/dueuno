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
grails.plugin.springsecurity.externalId.filterProcessesUrl = '/api/auth/external'
grails.plugin.springsecurity.externalId.propertyName = 'externalId'

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
        [pattern: grails.plugin.springsecurity.externalId.filterProcessesUrl, filters: 'externalIdAuthenticationFilter'],
        [pattern: '/**', filters: 'JOINED_FILTERS,-externalIdAuthenticationFilter']
]

///////////////////////////////////////////////////////////////////////////////
//                                                                           //
// LDAP CONFIGURATION                                                        //
//                                                                           //
///////////////////////////////////////////////////////////////////////////////

grails.plugin.springsecurity.providerNames = [
        // Must be the first one
        'daoAuthenticationProvider',

        // All other providers
        'externalIdAuthenticationProvider',

        // Must be the last one
        'rememberMeAuthenticationProvider',
]

// Configure the server connection using the variables you find below
// Test the conneciton logging in with the managerDn credentials

// CONFIGURATION
//
//grails.plugin.springsecurity.ldap.context.managerDn = 'testuser'
//grails.plugin.springsecurity.ldap.context.managerPassword = 'testpass'
//grails.plugin.springsecurity.ldap.context.server = 'ldap://0.0.0.0:389/'
//grails.plugin.springsecurity.ldap.search.base = 'DC=COMPANY,DC=LOCAL'

//
// Do not modify the lines below unless you know what you are doing
//
grails.plugin.springsecurity.ldap.auth.hideUserNotFoundExceptions = false

// You need this for Active Directory
grails.plugin.springsecurity.ldap.search.filter = 'sAMAccountName={0}'
grails.plugin.springsecurity.ldap.search.searchSubtree = true
grails.plugin.springsecurity.ldap.authorities.ignorePartialResultException = true

// extra attributes you want returned
grails.plugin.springsecurity.ldap.search.attributesToReturn = ['mail', 'displayName']

// role-specific LDAP config
grails.plugin.springsecurity.ldap.authorities.retrieveGroupRoles = true
grails.plugin.springsecurity.ldap.authorities.defaultRole = 'ROLE_USER'
//grails.plugin.springsecurity.ldap.authorities.groupSearchBase = 'dc=company,dc=group'
//grails.plugin.springsecurity.ldap.authorities.groupSearchFilter = 'member={0}'
grails.plugin.springsecurity.ldap.authorities.retrieveDatabaseRoles = true


grails.plugin.springsecurity.ldap.useRememberMe = false
//grails.plugin.springsecurity.ldap.rememberMe.detailsManager.groupMemberAttributeName = 'member'
//grails.plugin.springsecurity.ldap.rememberMe.detailsManager.groupRoleAttribute = 'CN'
//grails.plugin.springsecurity.ldap.rememberMe.detailsManager.groupSearchBase = 'DC=COMPANY,DC=NET'
//grails.plugin.springsecurity.ldap.rememberMe.detailsManager.passwordAttributeName = 'userPassword'
//grails.plugin.springsecurity.ldap.rememberMe.usernameMapper.userDnBase = 'DC=COMPANY,DC=NET'
//grails.plugin.springsecurity.ldap.rememberMe.usernameMapper.usernameAttribute = 'CN'
//grails.plugin.springsecurity.ldap.rememberMe.detailsManager.attributesToRetrieve = null
