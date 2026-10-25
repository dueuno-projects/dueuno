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

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Reads and invalidates authenticated sessions managed by the embedded Tomcat instance.
 */
@CompileStatic
class UserSessionService {

    private static final String SESSION_KEY_ATTRIBUTE = '_dueunoUserSessionKey'

    private volatile Context tomcatContext

    void initializeTomcat(Context context) {
        tomcatContext = context
    }

    void register(HttpSession httpSession, TUser user) {
        if (!httpSession || !user) return
        getOrCreateSessionKey(httpSession)
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

                result.add([
                    id              : getOrCreateSessionKey(httpSession),
                    tenant          : userTenantId,
                    username        : user.username,
                    firstname       : user.firstname,
                    lastname        : user.lastname,
                    lastAccessedTime: LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(lastAccessedTime),
                        ZoneId.systemDefault(),
                    ),
                ])
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

    private Manager findManager() {
        Context context = tomcatContext
        return context?.manager
    }
}
