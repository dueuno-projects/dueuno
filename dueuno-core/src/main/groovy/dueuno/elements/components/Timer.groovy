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
package dueuno.elements.components

import groovy.transform.CompileStatic

/**
 * A client-side interval timer component that fires a navigable action at a fixed rate.
 * <p>
 * {@code Timer} extends {@link Link} and uses the {@code interval} DOM event to trigger
 * the linked action repeatedly. The action target (controller, action, params) is configured
 * in the same way as any other {@link Link}. The interval callback is registered via the
 * {@code onInterval} argument at construction time.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class Timer extends Link {

    /** Whether the timer is currently active. Defaults to {@code true}. */
    Boolean enabled

    /** The interval between firings in milliseconds. Defaults to {@code 1000} (1 second). */
    Integer interval

    /**
     * Whether the action is executed immediately when the timer starts, before the first
     * interval elapses. Defaults to {@code true}.
     */
    Boolean executeImmediately

    /**
     * Creates a {@code Timer} instance configured from the supplied argument map.
     * Registers the interval event handler from the {@code onInterval} argument.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code enabled} ({@link Boolean}, default {@code true}),
     *             {@code interval} ({@link Integer}, default {@code 1000}),
     *             {@code executeImmediately} ({@link Boolean}, default {@code true}),
     *             {@code onInterval} ({@link String}) — the event-handler expression,
     *             plus all keys accepted by {@link Link#Link(Map)}
     */
    Timer(Map args) {
        super(args)

        enabled = args.enabled == null ? true : args.enabled
        interval = args.interval as Integer ?: 1000
        executeImmediately = args.executeImmediately == null ? true : args.executeImmediately

        eventName = 'interval'
        setOnEvent(args.onInterval as String)
    }

    /**
     * Serialises this timer's properties to JSON, adding {@link #enabled},
     * {@link #interval}, and {@link #executeImmediately}.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this timer's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                enabled: enabled,
                interval: interval,
                executeImmediately: executeImmediately,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }
}
