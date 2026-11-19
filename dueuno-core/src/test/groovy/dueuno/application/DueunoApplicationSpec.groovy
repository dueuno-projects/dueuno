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
package dueuno.application

import dueuno.elements.core.Feature
import dueuno.utils.LocaleUtils
import spock.lang.Specification

class DueunoApplicationSpec extends Specification {

    void 'explains the required bootstrap setup when onInit is missing'() {
        given:
        DueunoApplication service = new DueunoApplication()

        when:
        service.verifyInitialization()

        then:
        IllegalStateException error = thrown()
        error.message.contains('BootStrap.groovy')
        error.message.contains('import dueuno.application.DueunoApplication')
        error.message.contains('DueunoApplication dueunoApplication')
        error.message.contains('dueunoApplication.onInit()')
    }

    void 'registers localized language entries with existing flag assets'() {
        given:
        List<Map> entries = []
        DueunoApplication service = new DueunoApplication() {
            @Override
            List<String> getLanguages() { ['cs', 'cs_CZ', 'zh_TW'] }

            @Override
            Feature registerUserFeature(Map args) {
                entries.add(args)
                return null
            }
        }
        Properties messages = new Properties()
        new File('grails-app/i18n/dueuno.properties').withReader('UTF-8') {
            messages.load(it)
        }

        when:
        def registerLanguages = DueunoApplication.getDeclaredMethod('registerUserMenuLanguages')
        registerLanguages.accessible = true
        registerLanguages.invoke(service)

        then:
        entries.findAll { it.image }.size() == 3
        entries.findAll { it.image }.every {
            new File('grails-app/assets/dueuno/' + it.image).isFile() && messages[it.text]
        }
        entries.find { it.params?.id == 'cs' }.image == 'libs/flags/cs_cz.svg'
        entries.find { it.params?.id == 'zh_TW' }.image == 'libs/flags/tw.svg'
        LocaleUtils.getFlagCode('zh_TW') == 'tw'
    }

}
