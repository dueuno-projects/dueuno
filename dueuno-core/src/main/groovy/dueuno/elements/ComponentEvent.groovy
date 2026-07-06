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

import dueuno.core.LinkDefinition
import dueuno.types.Types
import groovy.transform.CompileStatic

/**
 * Describes a server-side action to invoke in response to a client-side component event.
 * <p>
 * A {@code ComponentEvent} extends {@link dueuno.core.LinkDefinition} with an {@code on} field
 * that specifies which DOM event (e.g. {@code "click"}, {@code "change"}) should trigger the
 * action. It is used to bind controller actions to UI interactions such as button clicks,
 * selection changes, or any other browser event exposed by a component.
 * </p>
 * <p>
 * The event is serialised to a map by {@link #asMap()} and passed to the Elements frontend,
 * which uses it to construct the AJAX call when the event fires.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class ComponentEvent extends LinkDefinition {

    /** The name of the DOM event that triggers the action (e.g. {@code "click"}, {@code "change"}). Defaults to {@code "click"}. */
    String on

    /**
     * Creates a {@code ComponentEvent} from a map of arguments.
     * All {@link dueuno.core.LinkDefinition} properties are supported, plus:
     * <ul>
     *     <li>{@code on} — the triggering DOM event name (defaults to {@code "click"})</li>
     * </ul>
     *
     * @param args map of event and link properties
     */
    ComponentEvent(Map args = [:]) {
        super(args)
        on = args.on ?: 'click'
    }

    /**
     * Creates a {@code ComponentEvent} that listens to the specified DOM event
     * without any associated controller action.
     *
     * @param on the DOM event name (e.g. {@code "change"})
     */
    ComponentEvent(String on) {
        super()
        this.on = on
    }

    /**
     * Serialises this event to a map suitable for JSON encoding and consumption
     * by the Elements frontend. Request parameters are serialised via
     * {@link dueuno.types.Types#serialize(Object)}.
     *
     * @return a map containing all link and render properties of this event
     */
    Map asMap() {
        return [
                namespace: namespace,
                controller: controller,
                action: action,
                url: url,
                params: Types.serialize(params),
                submit: submit,
                direct: direct,
                target: target,
                loading: loading,
                infoMessage: infoMessage,
                confirmMessage: confirmMessage,
                renderProperties: renderProperties.asMap(),
        ]
    }
}
