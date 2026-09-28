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
package dueuno.elements

import dueuno.elements.core.Transition
import grails.plugin.springwebsocket.WebSocket
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class TransitionService implements WebSocket {

    List<String> channels = []

    /**
     * Returns an instance of a Transition to store transition commands
     * @return an instance of a Transition
     */
    Transition createTransition() {
        return new Transition()
    }

    void subscribe(String channel) {
        channels.add(channel)
    }

    void publish(String channel, Transition t) {
        String destination = "/queue/channel/${channel}"
        convertAndSend(destination, t.commandsAsJSON)
    }

    void send(String username, Transition t) {
        String destination = "/queue/username/${username}"
        convertAndSend(destination, t.commandsAsJSON)
    }

}
