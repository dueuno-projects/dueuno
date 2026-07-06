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

import groovy.transform.CompileStatic

/**
 * Enumeration of audit-trail operation types recorded for user activity tracking.
 *
 * @author Gianluca Sartori
 */
@CompileStatic
enum AuditOperation {
    /** A user successfully authenticated. */
    LOGIN,

    /** A user ended their session. */
    LOGOUT,

    /** A user viewed (read) a resource. */
    VIEW,

    /** A user modified an existing resource. */
    EDIT,

    /** A user created a new resource. */
    CREATE,

    /** A user deleted a resource. */
    DELETE
}
