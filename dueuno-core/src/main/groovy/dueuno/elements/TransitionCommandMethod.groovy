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
 * Enumerates the client-side command methods that a {@link Transition} can dispatch
 * to the Elements frontend after a server-side action completes.
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
enum TransitionCommandMethod {

    /** Redirects the browser to a different URL or controller action. */
    REDIRECT,

    /** Replaces the main page content area with a new {@link PageContent} component. */
    CONTENT,

    /** Appends a new component as a child of an existing parent component. */
    APPEND,

    /** Replaces an existing component in the DOM with a new one. */
    REPLACE,

    /** Removes a component from the DOM. */
    REMOVE,

    /** Triggers a named event on a component. */
    TRIGGER,

    /** Shows or hides the global loading indicator. */
    LOADING,

    /** Invokes a named client-side method on a component. */
    CALL,

    /** Sets a property on a component, optionally triggering its change event. */
    SET,

}
