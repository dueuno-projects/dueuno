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
package dueuno.audit

import dueuno.commons.utils.DateUtils
import dueuno.core.WebRequestAware
import dueuno.elements.Elements
import dueuno.security.CryptoService
import dueuno.security.SecurityService
import grails.gorm.DetachedCriteria
import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import jakarta.servlet.http.HttpServletRequest

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * @author Gianluca Sartori
 */

@CurrentTenant
@Transactional
@CompileStatic
class AuditService implements WebRequestAware {

    SecurityService securityService
    CryptoService cryptoService

    void log(AuditOperation operation, Object domainObject) {
        Map properties = Elements.toMap(domainObject)
        String message = prettyProperties(properties)
        log(operation, message, domainObject.toString(), null, null)
    }

    void log(AuditOperation operation, Class domainObject, Map stateBefore, Map stateAfter = null) {
        String prettyStateBefore = prettyProperties(stateBefore)
        String prettyStateAfter = prettyProperties(stateAfter)
        log(operation, domainObject.toString(), prettyStateBefore, prettyStateAfter)
    }

    void log(AuditOperation operation, Class domainObject, String stateBefore, String stateAfter = null) {
        log(operation, domainObject.toString(), stateBefore, stateAfter)
    }

    void log(AuditOperation operation, String objectName, String stateBefore, String stateAfter = null) {
        log(operation, null, objectName, stateBefore, stateAfter)
    }

    void log(AuditOperation operation, String message) {
        log(operation, message, null, null, null)
    }

    void log(AuditOperation operation, String message, String objectName, String stateBefore, String stateAfter) {
        String username = securityService.currentUsername ?: 'super'
        LocalDateTime dateCreated = LocalDateTime.now()

        Map log = buildLog(dateCreated, username, operation, message, objectName, stateBefore, stateAfter)
        Map integrity = buildLogIntegrity(log)

        // We can not use the following to compute HMAC since they could be different on each request
        log.port = getClientPort(request)
        log.requestInfo = getClientRequestInfo(request)

        TAuditLog obj = new TAuditLog(log + integrity)
        obj.save(flush: true, failOnError: true)
    }

    Boolean verifyLogIntegrity(TAuditLog obj) {
        Map log = buildLog(obj.id)
        byte[] AESKey = cryptoService.getTenantAESKey()
        String actualDigest = computeHmac(log, AESKey)

        return MessageDigest.isEqual(actualDigest.bytes, obj.digest.bytes)
    }

    private Map buildLog(Serializable id) {
        TAuditLog obj = get(id)
        return buildLog(obj.dateCreated, obj.username, obj.operation, obj.message, obj.objectName, obj.stateBefore, obj.stateAfter)
    }

    private Map buildLog(LocalDateTime dateCreated, String username, AuditOperation operation, String message, String objectName, String stateBefore, String stateAfter) {
        String sDateCreated = DateUtils.format(dateCreated, 'yyyy-MM-dd @ HH:mm:ss')
        String sOperation = operation.toString()
        String userAgent = request.getHeader('User-Agent')
        String ip = getClientIp(request)

        Map log = [
                ip         : ip,
                userAgent  : userAgent,
                operation  : sOperation,
                message    : message?.take(TAuditLog.LOG_MESSAGE_MAX_SIZE),
                objectName : objectName,
                stateBefore: stateBefore?.take(TAuditLog.LOG_MESSAGE_MAX_SIZE),
                stateAfter : stateAfter?.take(TAuditLog.LOG_MESSAGE_MAX_SIZE),
                username   : username,
                dateCreated: sDateCreated,
                seed       : generateLogSeed(username, sOperation, sDateCreated),
        ]

        return log
    }

    private Map buildLogIntegrity(Map log) {
        byte[] AESKey = cryptoService.getTenantAESKey()

        Map integrity = [
                digest: computeHmac(log, AESKey),
        ]

        return integrity
    }

    private String generateLogSeed(String username, String operation, String dateCreated) {
        return username.take(1) + operation.take(2) + dateCreated.take(3)
    }

    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr()
    }

    private String getClientPort(HttpServletRequest request) {
        return request.getRemotePort()
    }

    private String getClientRequestInfo(HttpServletRequest request) {
        List relevantHeaders = [
                'CF-Connecting-IP',        // Cloudflare: IP reale del client dietro il loro CDN
                'True-Client-IP',          // Akamai / altri CDN: IP reale del client
                'X-Forwarded-For',         // De facto standard proxy: lista IP client + proxy
                'X-Real-IP',               // Nginx: IP reale del client
                'X-Cluster-Client-IP',     // Alcuni load balancer / proxy cluster: IP client
                'Client-IP',               // Proxy meno comuni: IP originale del client
                'X-Originating-IP',        // Alcuni webmail / proxy: IP originale del client
                'X-Remote-IP',             // Alcuni proxy privati: IP originale del client
                'X-Client-IP',             // Proxy custom: IP reale del client
                'Proxy-Client-IP',         // Proxy / servlet container legacy: IP client
                'WL-Proxy-Client-IP',      // WebLogic: IP client dietro il proxy
                'X-Forwarded',             // Variante generica di X-Forwarded-For
                'X-Forwarded-Host',        // Non contiene IP, ma può aiutare a ricostruire la connessione
                'Via'                      // RFC 7230: lista dei proxy attraversati, informativo
        ]

        Map results = [:]
        for (header in relevantHeaders) {
            String ip = request.getHeader(header)
            if (ip != null && ip.length() != 0 && !'unknown'.equalsIgnoreCase(ip)) {
                results.put(header, ip)
            }
        }

        return results.collect { k, v -> "${k} = ${v}" }.join(', ')
    }

    private String prettyProperties(Map properties) {
        Map stringifiedProperties = properties.collectEntries { [(it.key): it.value.toString()] } as Map
        String json = new JsonBuilder(stringifiedProperties).toPrettyString()
        return json
    }

    private String computeHmac(Map log, byte[] secret) {
        // Sort keys to ensure deterministic order
        def sortedKeys = log.keySet().sort()

        // Build canonical string representation
        String canonical = sortedKeys.collect { k ->
            "${k}=${log[k]}"
        }.join('|')

        // Create HMAC-SHA256
        Mac mac = Mac.getInstance("HmacSHA256")
        SecretKeySpec keySpec = new SecretKeySpec(secret, "HmacSHA256")
        mac.init(keySpec)

        byte[] hmacBytes = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8))

        // Return as lowercase hex string
        return hmacBytes.encodeHex().toString()
    }

    @CompileDynamic
    private DetachedCriteria<TAuditLog> buildQuery(Map filterParams) {
        def query = TAuditLog.where {}

        if (filterParams.containsKey('id')) query = query.where { id == filterParams.id }
        if (filterParams.dateFrom) query = query.where { dateCreated >= LocalDateTime.of(filterParams.dateFrom, LocalTime.MIN) }
        if (filterParams.dateTo) query = query.where { dateCreated <= LocalDateTime.of(filterParams.dateTo, LocalTime.MAX) }
        if (filterParams.operation) query = query.where { operation == filterParams.operation }
        if (filterParams.username) query = query.where { username == filterParams.username }
        if (filterParams.find) {
            String search = filterParams.find.replaceAll('\\*', '%')
            query = query.where {
                true
                        || ip =~ "%${search}%"
                        || userAgent =~ "%${search}%"
                        || username =~ "%${search}%"
                        || message =~ "%${search}%"
                        || objectName =~ "%${search}%"
                        || stateBefore =~ "%${search}%"
                        || stateAfter =~ "%${search}%"
            }
        }

        return query
    }

    TAuditLog get(Serializable id) {
        return buildQuery(id: id).get()
    }

    List<TAuditLog> list(Map filterParams = [:], Map fetchParams = [:]) {
        if (!fetchParams.sort) fetchParams.sort = [dateCreated: 'desc']
        def query = buildQuery(filterParams)
        return query.list(fetchParams)
    }

    Number count(Map filterParams = [:]) {
        def query = buildQuery(filterParams)
        return query.count()
    }
}
