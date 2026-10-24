/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.elements.core

import spock.lang.Specification

class ComponentActionSpec extends Specification {

    void defaultsMissingActionToIndex() {
        expect:
        new ComponentAction([controller: 'shell']).action == 'index'
    }

    void preservesExplicitAction() {
        expect:
        new ComponentAction([controller: 'shell', action: 'home']).action == 'home'
    }

    void includesDefaultActionInFeatureId() {
        expect:
        new Feature([namespace: 'security', controller: 'user']).id == 'security.user.index'
    }

}
