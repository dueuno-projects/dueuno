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

import dueuno.elements.core.Component
import groovy.transform.CompileStatic

/**
 * Displays an image from a browser-readable URL or path.
 * <p>
 * When added to a form, the field's {@code rows} setting controls its height. The
 * {@code keepAspectRatio} property defaults to {@code true}; set it to {@code false}
 * to stretch the image to fill the field.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class Image extends Component {

    /** Browser-readable URL or path of the image to display. */
    String image

    /** Whether the image preserves its aspect ratio. Defaults to {@code true}. */
    Boolean keepAspectRatio

    /**
     * Creates an image component from the given arguments.
     *
     * @param args component arguments, including {@code image} and optional {@code keepAspectRatio}
     */
    Image(Map args) {
        super(args)

        image = args.image
        keepAspectRatio = args.keepAspectRatio == null ? true : args.keepAspectRatio as Boolean
        containerSpecs.multiline = true
    }

}
