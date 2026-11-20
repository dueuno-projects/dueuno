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
 * When added to a form, the field's {@code rows} setting controls its height unless
 * both {@code keepAspectRatio} and {@code fitWidth} are {@code true}; in that case,
 * the image uses the full field width and its natural aspect ratio determines its height.
 * The {@code keepAspectRatio} property defaults to {@code true}; set it to {@code false}
 * to stretch the image to fill the field. When the aspect ratio is preserved and
 * {@code fitWidth} is {@code false}, unused space is transparent unless a
 * {@code backgroundColor} is specified.
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
     * Whether to size the image to the full field width, even if it exceeds the field height.
     * Defaults to {@code false}.
     */
    Boolean fitWidth

    /** Inner spacing between the image and its edge, in rem. Defaults to {@code 0}. */
    Double padding

    /**
     * Creates an image component from the given arguments.
     *
     * @param args component arguments, including {@code image}, {@code keepAspectRatio},
     *             {@code fitWidth}, and {@code padding}
     */
    Image(Map args) {
        super(args)

        image = args.image

        Double argsPadding = args.padding as Double
        padding = (argsPadding == null || argsPadding < 0) ? 0d : argsPadding
        fitWidth = args.fitWidth == null ? false : args.fitWidth as Boolean

        keepAspectRatio = args.keepAspectRatio == null ? true : args.keepAspectRatio as Boolean
        containerSpecs.multiline = !(keepAspectRatio && fitWidth)
    }

    String getCssStyle() {
        String result = cssStyleColors
        if (padding > 0) result += " padding: ${padding}rem;"
        return result
    }

}
