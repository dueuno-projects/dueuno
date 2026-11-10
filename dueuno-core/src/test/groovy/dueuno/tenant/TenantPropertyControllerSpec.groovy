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
package dueuno.tenant

import dueuno.application.PropertyType
import spock.lang.Specification
import spock.lang.Unroll

class TenantPropertyControllerSpec extends Specification {

    @Unroll
    void 'does not look up an existing value when creating a #type property'() {
        given:
        def service = Mock(TenantPropertyService)
        def controller = new TenantPropertyController(tenantPropertyService: service)
        def form = new Expando(validate: null, controls: [value: [value: null]])
        form.addField = { Map args -> }
        controller.metaClass.createContent = { Class clazz -> [form: form] }
        controller.metaClass.messageOrBlank = { String code -> '' }
        def obj = new TTenantProperty(type: type)
        def buildForm = TenantPropertyController.getDeclaredMethod('buildForm', TTenantProperty)
        buildForm.accessible = true

        when:
        buildForm.invoke(controller, obj)

        then:
        0 * service._
        form.controls.value.value == null

        where:
        type << PropertyType.values()
    }
}
