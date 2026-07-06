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
 * Carries the display options that control how a {@link PageContent} is rendered by the
 * Elements frontend after a server-side action completes.
 * <p>
 * An instance of this class is attached to every {@link dueuno.core.LinkDefinition} and
 * {@link PageContent}, and is serialised into the {@link Transition} command stream so the
 * browser can apply the requested visual behaviour when the response arrives.
 * </p>
 * <p>
 * All properties are optional; {@code null} means "use the default" for that option.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class PageRenderProperties implements Serializable {

    /** When {@code true}, the content is displayed in a modal dialog instead of the main content area. */
    Boolean modal

    /** When {@code true}, the modal dialog uses a small (narrow) layout. */
    Boolean small

    /** When {@code true}, the modal dialog uses a large (wide) layout. */
    Boolean large

    /** When {@code true}, the modal dialog occupies the full viewport. */
    Boolean fullscreen

    /** When {@code true}, a close button is shown on the modal dialog. */
    Boolean closeButton

    /** When {@code true}, the browser address bar is updated to reflect the navigated URL. */
    Boolean updateUrl

    /**
     * The name of the CSS animation to apply when the content enters the view
     * (e.g. {@code "fade"}, {@code "slide"}).
     */
    String animate

    /**
     * Scroll behaviour to apply after the content is rendered
     * (e.g. {@code "reset"} to scroll back to the top).
     */
    String scroll

    /**
     * Creates a {@code PageRenderProperties} from the given argument map.
     * All keys are optional and map directly to the fields of this class.
     *
     * @param args map of render property values
     */
    PageRenderProperties(Map args = [:]) {
        modal = args.modal
        small = args.small
        large = args.large
        fullscreen = args.fullscreen
        closeButton = args.closeButton
        updateUrl = args.updateUrl
        animate = args.animate
        scroll = args.scroll
    }

    /**
     * Serialises this instance to a plain map suitable for JSON encoding and
     * consumption by the Elements frontend.
     *
     * @return a map containing all render property keys and their current values
     */
    Map asMap() {
        return [
                modal: modal,
                small: small,
                large: large,
                fullscreen: fullscreen,
                closeButton: closeButton,
                updateUrl: updateUrl,
                animate: animate,
                scroll: scroll,
        ]
    }
}
