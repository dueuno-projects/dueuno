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

import groovy.transform.CompileStatic

/**
 * Represents a single command within a {@link Transition}.
 * <p>
 * Each command encodes one client-side operation that the Elements frontend
 * will execute in sequence after a server-side action completes. The set of
 * supported operations is defined by {@link TransitionCommandMethod}.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class TransitionCommand {

    /** The name of the client-side method to execute, as defined by {@link TransitionCommandMethod}. */
    String method

    /** The identifier of the target component on which the command operates, or {@code null} if not applicable. */
    String component

    /** The name of the property to read or write, or the event/method name for {@code TRIGGER}/{@code CALL} commands. */
    String property

    /** The value or arguments associated with the command (e.g. the new property value, redirect URL map, or method args). */
    Object value

    /** Whether the component's change event should be triggered after the command is applied (used by {@code SET} commands). */
    Boolean trigger
}
