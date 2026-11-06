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

import dueuno.elements.WebRequestAware
import dueuno.tenant.TenantPropertyService
import groovy.transform.CompileStatic
import jakarta.inject.Inject
import jakarta.servlet.ServletContext
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpSession
import org.apache.catalina.Context
import org.apache.catalina.Manager
import org.apache.catalina.Session
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Owns user session state and lifecycle for sessions managed by embedded Tomcat.
 */
@CompileStatic
class UserSessionService implements WebRequestAware {

    private static final String LOGIN_TIME_ATTRIBUTE = 'LOGIN_TIME'
    private static final String LOGIN_IP_ADDRESS_ATTRIBUTE = 'LOGIN_IP_ADDRESS'
    private static final String LOGIN_DENIED_MESSAGE_ATTRIBUTE = 'LOGIN_DENIED_MESSAGE'
    private static final String SESSION_KEY_ATTRIBUTE = 'USER_SESSION_KEY'
    private static final String TENANT_ID_ATTRIBUTE = 'USER_SESSION_TENANT_ID'
    private static final String CURRENT_USER_ATTRIBUTE = 'CURRENT_USER'
    private static final String USER_AGENT_ATTRIBUTE = 'USER_AGENT'
    private static final String MOBILE_DEVICE_ATTRIBUTE = 'MOBILE_DEVICE'
    private static final String REMEMBER_ME_EXPIRATION_ATTRIBUTE = 'REMEMBER_ME_EXPIRATION_TIME'
    private static final String REMEMBER_ME_TOKEN_ATTRIBUTE = 'REMEMBER_ME_TOKEN'

    private volatile Context tomcatContext
    private final ConcurrentMap<String, Long> revokedRememberMeTokens = new ConcurrentHashMap<>()

    TenantPropertyService tenantPropertyService

    @Inject
    SecurityContextLogoutHandler securityContextLogoutHandler

    @Inject
    TokenBasedRememberMeServices tokenBasedRememberMeServices

    void initializeTomcat(Context context) {
        tomcatContext = context
    }

    void configureServletContext(ServletContext servletContext) {
        servletContext.sessionCookieConfig.name = tenantPropertyService.getString('SESSION_COOKIE_NAME')
    }

    void initializeSession(TUser user) {
        if (!user) {
            return
        }

        session.maxInactiveInterval = user.sessionDuration * 60
        configureRememberMe(user.rememberMeDuration)
        register(user)
    }

    TUser getCurrentUser() {
        return session[CURRENT_USER_ATTRIBUTE] as TUser
    }

    String getTenantId() {
        TUser user = resolveUser(session)
        return user ? resolveTenantId(session, user) : null
    }

    void setCurrentUser(TUser user) {
        if (!user) {
            return
        }

        session[CURRENT_USER_ATTRIBUTE] =  user
        session[TENANT_ID_ATTRIBUTE] = user.tenant?.tenantId
    }

    void setLoginDeniedMessage(String message) {
        session[LOGIN_DENIED_MESSAGE_ATTRIBUTE] = message
    }

    String getLoginDeniedMessage() {
        return session[LOGIN_DENIED_MESSAGE_ATTRIBUTE] as String
    }

    Boolean isLoginDenied() {
        return getLoginDeniedMessage() != null
    }

    void configureRememberMe(Integer duration) {
        tokenBasedRememberMeServices.cookieName = tenantPropertyService.getString('REMEMBER_ME_COOKIE_NAME', true)
        tokenBasedRememberMeServices.alwaysRemember = false
        if (duration != null) {
            tokenBasedRememberMeServices.tokenValiditySeconds = duration * 60
        }
    }

    void logout() {
        if (!hasRequest()) {
            return
        }

        configureRememberMe(null)
        securityContextLogoutHandler.logout(request, response, null)
        tokenBasedRememberMeServices.logout(request, response, null)
        invalidateSession(session)
    }

    boolean handleRequest() {
        if (!hasRequest()) {
            return true
        }

        try {
            String cookieName = tenantPropertyService.getString('REMEMBER_ME_COOKIE_NAME')
            if (!cookieName) {
                return true
            }

            String cookieValue
            Cookie[] cookies = request.cookies
            if (cookies) {
                for (Cookie cookie in cookies) {
                    if (cookie.name == cookieName) {
                        cookieValue = cookie.value
                        break
                    }
                }
            }

            if (isRememberMeRevoked(cookieValue)) {
                logout()
                response.sendRedirect(request.contextPath + '/authentication/login')
                return false
            }

            TUser user = resolveUser(session)
            if (!user) {
                return true
            }

            updateRememberMeExpiration(cookieValue)

        } catch (IllegalStateException ignored) {
            // The session may be invalidated while the request is being processed.
        }

        return true
    }

    private void register(TUser user) {
        if (!user) {
            return
        }


        getOrCreateSessionKey(session)
        session.setAttribute(TENANT_ID_ATTRIBUTE, user.tenant?.tenantId)

        if (session[LOGIN_TIME_ATTRIBUTE] == null) {
            session[LOGIN_TIME_ATTRIBUTE] = System.currentTimeMillis()
            session[LOGIN_IP_ADDRESS_ATTRIBUTE] = request?.remoteAddr

            String userAgent = request?.getHeader('User-Agent')
            if (userAgent) session[USER_AGENT_ATTRIBUTE] = userAgent
        }
    }

    private void updateRememberMeExpiration(String cookieValue) {
        try {
            if (!cookieValue) {
                clearRememberMeAttributes(session)
                return
            }

            String paddedCookieValue = cookieValue
            while (paddedCookieValue.length() % 4 != 0) {
                paddedCookieValue += '='
            }

            String cookieContents = new String(
                Base64.getDecoder().decode(paddedCookieValue),
                StandardCharsets.UTF_8,
            )
            int firstDelimiter = cookieContents.indexOf(':')
            int secondDelimiter = cookieContents.indexOf(':', firstDelimiter + 1)
            if (firstDelimiter < 0 || secondDelimiter < 0) {
                clearRememberMeAttributes(session)
                return
            }

            String expirationValue = URLDecoder.decode(
                cookieContents.substring(firstDelimiter + 1, secondDelimiter),
                StandardCharsets.UTF_8,
            )
            session[REMEMBER_ME_EXPIRATION_ATTRIBUTE] = Long.parseLong(expirationValue)
            session[REMEMBER_ME_TOKEN_ATTRIBUTE] = fingerprint(cookieValue)

        } catch (IllegalArgumentException ignored) {
            clearRememberMeAttributes(session)

        } catch (IllegalStateException ignored) {
            // The session may be invalidated while the request is being processed.
        }
    }

    boolean isRememberMeRevoked(String cookieValue) {
        if (!cookieValue) {
            return false
        }

        String tokenFingerprint = fingerprint(cookieValue)
        Long expirationTime = revokedRememberMeTokens.get(tokenFingerprint)
        if (!expirationTime) return false
        if (expirationTime > System.currentTimeMillis()) return true

        revokedRememberMeTokens.remove(tokenFingerprint, expirationTime)
        return false
    }

    List<Map<String, Object>> list(String tenantId, Boolean includeAllTenants) {
        Manager currentManager = findManager()
        if (!currentManager) {
            return []
        }

        long now = System.currentTimeMillis()
        List<Map<String, Object>> result = []

        for (Session tomcatSession in currentManager.findSessions()) {
            try {
                boolean valid = tomcatSession.valid

                HttpSession httpSession = tomcatSession.session
                TUser user = resolveUser(httpSession)
                if (!valid) continue
                if (!user) continue

                int timeout = httpSession.maxInactiveInterval
                long lastAccessedTime = httpSession.lastAccessedTime
                boolean hasNotTimedOut = timeout <= 0 || now - lastAccessedTime < timeout * 1000L
                if (!hasNotTimedOut) continue

                String userTenantId = resolveTenantId(httpSession, user)
                if (tenantId && userTenantId != tenantId) continue
                if (!includeAllTenants && userTenantId != tenantId) continue

                Long rememberMeExpirationTime = httpSession.getAttribute(REMEMBER_ME_EXPIRATION_ATTRIBUTE) as Long
                Long loginTime = httpSession.getAttribute(LOGIN_TIME_ATTRIBUTE) as Long

                result.add([
                    id                       : getOrCreateSessionKey(httpSession),
                    tenant                   : userTenantId,
                    username                 : user.username,
                    firstname                : user.firstname,
                    lastname                 : user.lastname,
                    loginTime                : loginTime ? LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(loginTime),
                        ZoneId.systemDefault(),
                    ) : null,
                    loginIpAddress           : httpSession.getAttribute(LOGIN_IP_ADDRESS_ATTRIBUTE) as String,
                    userAgent                : httpSession.getAttribute(USER_AGENT_ATTRIBUTE) as String,
                    isMobile                 : httpSession.getAttribute(MOBILE_DEVICE_ATTRIBUTE) as Boolean,
                    lastAccessedTime         : LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(lastAccessedTime),
                        ZoneId.systemDefault(),
                    ),
                    expirationTime           : timeout > 0 ? LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(lastAccessedTime + timeout * 1000L),
                        ZoneId.systemDefault(),
                    ) : null,
                    rememberMeExpirationTime: rememberMeExpirationTime ? LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(rememberMeExpirationTime),
                        ZoneId.systemDefault(),
                    ) : null,
                ] as Map)
            } catch (IllegalStateException ignored) {
                // The session may be invalidated while the list is being built.
            }
        }

        result.sort { Map<String, Object> left, Map<String, Object> right ->
            int tenantComparison = ((String) left.tenant).compareToIgnoreCase((String) right.tenant)
            if (tenantComparison != 0) return tenantComparison
            return ((String) left.username).compareToIgnoreCase((String) right.username)
        }

        return result
    }

    void disconnect(String sessionKey, String tenantId, Boolean includeAllTenants) {
        if (!sessionKey) {
            return
        }

        Manager currentManager = findManager()
        if (!currentManager) {
            return
        }

        for (Session tomcatSession in currentManager.findSessions()) {
            try {
                HttpSession httpSession = tomcatSession.session
                if (httpSession.getAttribute(SESSION_KEY_ATTRIBUTE) != sessionKey) continue

                TUser user = resolveUser(httpSession)
                String userTenantId = user ? resolveTenantId(httpSession, user) : null
                if (!user || (!includeAllTenants && userTenantId != tenantId)) return

                invalidateSession(httpSession)
                return
            } catch (IllegalStateException ignored) {
                // The session may already have been invalidated or expired.
            }
        }
    }

    void disconnectOtherSessions(TUser user) {
        if (!user) {
            return
        }

        Manager currentManager = findManager()
        if (!currentManager) {
            return
        }

        for (Session tomcatSession in currentManager.findSessions()) {
            try {
                HttpSession httpSession = tomcatSession.session
                if (httpSession.id == session.id) continue

                TUser sessionUser = resolveUser(httpSession)
                if (!isSameUser(httpSession, sessionUser, user)) {
                    continue
                }

                invalidateSession(httpSession)

            } catch (IllegalStateException ignored) {
                // The session may already have been invalidated or expired.
            }
        }
    }

    private TUser resolveUser(HttpSession httpSession) {
        return httpSession[CURRENT_USER_ATTRIBUTE] as TUser
    }

    private boolean isSameUser(HttpSession httpSession, TUser sessionUser, TUser currentUser) {
        return sessionUser && currentUser &&
            sessionUser.id == currentUser.id &&
            resolveTenantId(httpSession, sessionUser) == currentUser.tenant?.tenantId
    }

    private String resolveTenantId(HttpSession httpSession, TUser user) {
        String tenantId = httpSession[TENANT_ID_ATTRIBUTE] as String
        if (!tenantId) {
            tenantId = user.tenant?.tenantId
            if (tenantId) httpSession[TENANT_ID_ATTRIBUTE] = tenantId
        }
        return tenantId
    }

    private void invalidateSession(HttpSession httpSession) {
        if (!httpSession) {
            return
        }

        String rememberMeToken = httpSession[REMEMBER_ME_TOKEN_ATTRIBUTE] as String
        Long rememberMeExpirationTime = httpSession[REMEMBER_ME_EXPIRATION_ATTRIBUTE] as Long
        if (rememberMeToken && rememberMeExpirationTime && rememberMeExpirationTime > System.currentTimeMillis()) {
            removeExpiredRevocations()
            revokedRememberMeTokens.put(rememberMeToken, rememberMeExpirationTime)
        }

        httpSession.invalidate()
    }

    private static String getOrCreateSessionKey(HttpSession httpSession) {
        synchronized (httpSession) {
            String sessionKey = httpSession[SESSION_KEY_ATTRIBUTE] as String
            if (!sessionKey) {
                sessionKey = UUID.randomUUID().toString()
                httpSession[SESSION_KEY_ATTRIBUTE] = sessionKey
            }
            return sessionKey
        }
    }

    private static void clearRememberMeAttributes(HttpSession httpSession) {
        httpSession.removeAttribute(REMEMBER_ME_EXPIRATION_ATTRIBUTE)
        httpSession.removeAttribute(REMEMBER_ME_TOKEN_ATTRIBUTE)
    }

    private static String fingerprint(String cookieValue) {
        byte[] digest = MessageDigest.getInstance('SHA-256').digest(cookieValue.getBytes(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(digest)
    }

    private void removeExpiredRevocations() {
        long now = System.currentTimeMillis()
        for (Map.Entry<String, Long> entry in revokedRememberMeTokens.entrySet()) {
            if (entry.value <= now) {
                revokedRememberMeTokens.remove(entry.key, entry.value)
            }
        }
    }

    private Manager findManager() {
        Context context = tomcatContext
        return context?.manager
    }

}
