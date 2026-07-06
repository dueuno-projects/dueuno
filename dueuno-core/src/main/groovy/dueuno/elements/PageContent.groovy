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
 * Base class for the main content area injected into a {@link Page} by a controller action.
 * <p>
 * A {@code PageContent} is the component that fills the primary content slot of a {@link Page}.
 * Each controller action typically creates a concrete subclass (e.g. a form, a list, or a
 * custom layout) and passes it to {@link ElementsController#display(Map)} via the
 * {@code content} key.
 * </p>
 * <p>
 * The content's page title defaults to the i18n key
 * {@code <controllerName>.<actionName>.title} and can be overridden via the {@link #title}
 * property. Rendering behaviour (modal display, animations, scroll reset, etc.) is controlled
 * by {@link #renderProperties}.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
abstract class PageContent extends Component {

    /**
     * The i18n message key (or literal text) used as the page/modal title.
     * Defaults to {@code <controllerName>.<actionName>.title}.
     */
    String title

    /** Optional interpolation arguments for the {@link #title} i18n message. */
    List titleArgs

    /**
     * Rendering options that control how this content is displayed
     * (e.g. as a full page, as a modal dialog, with a specific animation or scroll behaviour).
     */
    PageRenderProperties renderProperties

    /**
     * Creates a {@code PageContent} from the given argument map.
     * All {@link Component} arguments are supported, plus:
     * <ul>
     *     <li>{@code title} — i18n key or literal title text; defaults to
     *         {@code <controllerName>.<actionName>.title}</li>
     *     <li>{@code titleArgs} — interpolation arguments for the title message</li>
     *     <li>Any {@link PageRenderProperties} key (e.g. {@code modal}, {@code animate},
     *         {@code scroll})</li>
     * </ul>
     *
     * @param args map of content properties
     */
    PageContent(Map args = [:]) {
        super(args)

        viewPath = '/dueuno/elements/'
        viewTemplate = 'PageContent'

        String defaultTitle = controllerName + '.' + actionName + '.title'
        title = args.title ?: defaultTitle
        titleArgs = (args.titleArgs == null) ? [] : args.titleArgs as List

        renderProperties = new PageRenderProperties(args)
    }

    /**
     * Applies matching entries from {@code args} to {@link #renderProperties}.
     * Only keys that correspond to existing properties of {@link PageRenderProperties}
     * are applied; the {@code class} meta-property is skipped.
     * This method is called by the framework when {@link ElementsController#display(Map)}
     * forwards render options to the content.
     *
     * @param args a map that may contain {@link PageRenderProperties} property names as keys
     */
    void setRenderProperties(Map args) {
        for (property in renderProperties.properties) {
            if (property.key != 'class') {
                String name = property.key
                Object value = args[name]
                renderProperties.setProperty(name, value)
            }
        }
    }

    /**
     * Returns a JSON string of this content's client-side properties, adding the current
     * controller name, action name, and serialised {@link #renderProperties} on top of the
     * inherited {@link Component} properties.
     *
     * @param properties additional properties to merge
     * @return a JSON representation of all component properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                controller: controllerName,
                action: actionName,
                renderProperties: renderProperties.asMap(),
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

}
