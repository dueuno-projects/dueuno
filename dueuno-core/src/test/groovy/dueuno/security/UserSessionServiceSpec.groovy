/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package dueuno.security

import spock.lang.Specification

import java.lang.reflect.Method

class UserSessionServiceSpec extends Specification {

    void 'matches username and name filters case insensitively by substring'() {
        given:
        Method matchesUser = UserSessionService.getDeclaredMethod('matchesUser', String, String, String, String)
        matchesUser.accessible = true

        expect:
        matchesUser.invoke(null, (Object[]) [username, firstname, lastname, filter]) == matches

        where:
        username        | firstname | lastname | filter  || matches
        'alice@example' | 'Alice'   | 'Smith'  | 'alice' || true
        'bob@example'   | 'Alice'   | 'Smith'  | 'ICE'   || true
        'bob@example'   | 'Alice'   | 'Smith'  | 'MIT'   || true
        'bob@example'   | 'Alice'   | 'Smith'  | 'bob'   || true
        'bob@example'   | 'Alice'   | 'Smith'  | 'Jones' || false
        'bob@example'   | 'Alice'   | 'Smith'  | null    || true
        'bob@example'   | 'Alice'   | 'Smith'  | ''      || true
    }
}
