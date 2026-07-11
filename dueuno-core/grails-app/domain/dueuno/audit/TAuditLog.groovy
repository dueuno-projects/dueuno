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

import grails.compiler.GrailsCompileStatic
import grails.gorm.MultiTenant
import org.grails.datastore.gorm.GormEntity

import java.time.LocalDateTime

/**
 * @author Gianluca Sartori
 */

@GrailsCompileStatic
class TAuditLog implements GormEntity, MultiTenant<TAuditLog> {

    static final Integer LOG_MESSAGE_MAX_SIZE = 4000

    Long id

    LocalDateTime dateCreated

    String ip
    String port
    String requestInfo
    String userAgent
    String username
    AuditOperation operation
    String message

    String objectName
    String stateBefore
    String stateAfter

    String digest

    static constraints = {
        ip nullable: true
        port nullable: true
        requestInfo nullable: true
        userAgent nullable: true
        message nullable: true, maxSize: LOG_MESSAGE_MAX_SIZE
        objectName nullable: true
        stateBefore nullable: true, maxSize: LOG_MESSAGE_MAX_SIZE
        stateAfter nullable: true, maxSize: LOG_MESSAGE_MAX_SIZE
        digest nullable: true
    }
}
