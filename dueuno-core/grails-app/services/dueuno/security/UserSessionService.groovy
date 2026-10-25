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

import groovy.transform.CompileStatic
import jakarta.servlet.http.HttpSession
import org.apache.catalina.Context
import org.apache.catalina.Manager
import org.apache.catalina.Session

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Reads and invalidates authenticated sessions managed by the embedded Tomcat instance.
 */
@CompileStatic
class UserSessionService {

    private static final String SESSION_KEY_ATTRIBUTE = '_dueunoUserSessionKey'
    private static final String REMEMBER_ME_EXPIRATION_ATTRIBUTE = '_dueunoRememberMeExpirationTime'
    private static final String REMEMBER_ME_TOKEN_ATTRIBUTE = '_dueunoRememberMeToken'

    private volatile Context tomcatContext
    private final ConcurrentMap<String, Long> revokedRememberMeTokens = new ConcurrentHashMap<>()

    void initializeTomcat(Context context) {
        tomcatContext = context
    }

    void register(HttpSession httpSession, TUser user) {
        if (!httpSession || !user) return
        getOrCreateSessionKey(httpSession)
    }

    void updateRememberMeExpiration(HttpSession httpSession, String cookieValue) {
        if (!httpSession) return

        try {
            if (!cookieValue) {
                clearRememberMeAttributes(httpSession)
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
                clearRememberMeAttributes(httpSession)
                return
            }

            String expirationValue = URLDecoder.decode(
                cookieContents.substring(firstDelimiter + 1, secondDelimiter),
                StandardCharsets.UTF_8,
            )
            httpSession.setAttribute(REMEMBER_ME_EXPIRATION_ATTRIBUTE, Long.parseLong(expirationValue))
            httpSession.setAttribute(REMEMBER_ME_TOKEN_ATTRIBUTE, fingerprint(cookieValue))
        } catch (IllegalArgumentException ignored) {
            clearRememberMeAttributes(httpSession)
        } catch (IllegalStateException ignored) {
            // The session may be invalidated while the request is being processed.
        }
    }

    boolean isRememberMeRevoked(String cookieValue) {
        if (!cookieValue) return false

        String tokenFingerprint = fingerprint(cookieValue)
        Long expirationTime = revokedRememberMeTokens.get(tokenFingerprint)
        if (!expirationTime) return false
        if (expirationTime > System.currentTimeMillis()) return true

        revokedRememberMeTokens.remove(tokenFingerprint, expirationTime)
        return false
    }

    List<Map<String, Object>> list(String tenantId, Boolean includeAllTenants) {
        Manager currentManager = findManager()
        if (!currentManager) return []

        long now = System.currentTimeMillis()
        List<Map<String, Object>> result = []

        for (Session tomcatSession in currentManager.findSessions()) {
            try {
                boolean valid = tomcatSession.valid

                HttpSession httpSession = tomcatSession.session
                TUser user = httpSession.getAttribute('_21CurrentUser') as TUser
                if (!valid) continue
                if (!user) continue

                int timeout = httpSession.maxInactiveInterval
                long lastAccessedTime = httpSession.lastAccessedTime
                boolean hasNotTimedOut = timeout <= 0 || now - lastAccessedTime < timeout * 1000L
                if (!hasNotTimedOut) continue

                String userTenantId = user.tenant?.tenantId
                if (tenantId && userTenantId != tenantId) continue
                if (!includeAllTenants && userTenantId != tenantId) continue

                Long rememberMeExpirationTime = httpSession.getAttribute(REMEMBER_ME_EXPIRATION_ATTRIBUTE) as Long

                result.add([
                    id                       : getOrCreateSessionKey(httpSession),
                    tenant                   : userTenantId,
                    username                 : user.username,
                    firstname                : user.firstname,
                    lastname                 : user.lastname,
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
        if (!sessionKey) return

        Manager currentManager = findManager()
        if (!currentManager) return

        for (Session tomcatSession in currentManager.findSessions()) {
            try {
                HttpSession httpSession = tomcatSession.session
                if (httpSession.getAttribute(SESSION_KEY_ATTRIBUTE) != sessionKey) continue

                TUser user = httpSession.getAttribute('_21CurrentUser') as TUser
                String userTenantId = user?.tenant?.tenantId
                if (!user || (!includeAllTenants && userTenantId != tenantId)) return

                String rememberMeToken = httpSession.getAttribute(REMEMBER_ME_TOKEN_ATTRIBUTE) as String
                Long rememberMeExpirationTime = httpSession.getAttribute(REMEMBER_ME_EXPIRATION_ATTRIBUTE) as Long
                if (rememberMeToken && rememberMeExpirationTime && rememberMeExpirationTime > System.currentTimeMillis()) {
                    removeExpiredRevocations()
                    revokedRememberMeTokens.put(rememberMeToken, rememberMeExpirationTime)
                }

                httpSession.invalidate()
                return
            } catch (IllegalStateException ignored) {
                // The session may already have been invalidated or expired.
            }
        }
    }

    private static String getOrCreateSessionKey(HttpSession httpSession) {
        synchronized (httpSession) {
            String sessionKey = httpSession.getAttribute(SESSION_KEY_ATTRIBUTE) as String
            if (!sessionKey) {
                sessionKey = UUID.randomUUID().toString()
                httpSession.setAttribute(SESSION_KEY_ATTRIBUTE, sessionKey)
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
