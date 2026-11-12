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
package dueuno.elements

import dueuno.elements.core.Transition
import org.springframework.messaging.simp.SimpMessagingTemplate
import spock.lang.Specification

class TransitionServiceSpec extends Specification {

    void 'sends transition JSON to the tenant-specific channel and username destinations'() {
        given:
        SimpMessagingTemplate template = Mock()
        TransitionService service = new TransitionService(brokerMessagingTemplate: template)
        String json = '[{"method":"delay","value":{"value":100}}]'
        Transition transition = Stub(Transition) {
            getCommandsAsJSON() >> json
        }

        when:
        service.publish('tenant-a', 'updates', transition)
        service.publish('tenant-b', 'updates', transition)
        service.send('tenant-a', 'alice', transition)
        service.send('tenant-b', 'alice', transition)

        then:
        1 * template.convertAndSend('/queue/tenant/tenant-a/channel/updates', json)
        1 * template.convertAndSend('/queue/tenant/tenant-b/channel/updates', json)
        1 * template.convertAndSend('/queue/tenant/tenant-a/username/alice', json)
        1 * template.convertAndSend('/queue/tenant/tenant-b/username/alice', json)
        0 * template._
    }
}
