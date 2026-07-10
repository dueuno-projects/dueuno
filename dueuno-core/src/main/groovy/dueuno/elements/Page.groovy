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
import groovy.util.logging.Slf4j

/**
 * Base class for all full-page layouts in the Elements framework.
 * <p>
 * A {@code Page} is the root {@link Component} rendered when the browser performs a full
 * page load or refresh. It provides the common infrastructure shared by every page layout:
 * </p>
 * <ul>
 *     <li>A {@link KeyPress} component for global keyboard/barcode-scanner event handling.</li>
 *     <li>A {@link PageModal} for displaying modal dialogs.</li>
 *     <li>A {@link PageMessageBox} for displaying informational, error, and confirmation messages.</li>
 *     <li>A transient {@link PageContent} slot that holds the main body content injected by the
 *         current controller action.</li>
 *     <li>Browser tab and launcher icon configuration ({@link #favicon}, {@link #appicon}).</li>
 * </ul>
 * <p>
 * Concrete subclasses define the visual shell (navigation, sidebars, etc.) by extending this class
 * and providing a corresponding GSP view under {@code /dueuno/elements/pages/}.
 * </p>
 *
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
abstract class Page extends Component {

    /** The modal dialog container for this page. */
    PageModal modal

    /** The message box used to display info, error, and confirmation messages. */
    PageMessageBox messageBox

    /** The global keyboard/barcode-scanner listener for this page. */
    KeyPress keyPress

    /**
     * The main content area of the page. This is transient (not serialised to the session)
     * and is set by the controller action on each request.
     */
    transient PageContent content

    /** The path or URL of the browser tab favicon. */
    String favicon

    /** The path or URL of the application icon (e.g. used on mobile home-screen launchers). */
    String appicon

    /**
     * Creates a {@code Page} from the given argument map.
     * All {@link Component} arguments are supported, plus:
     * <ul>
     *     <li>{@code favicon} — browser tab icon path or URL</li>
     *     <li>{@code appicon} — application launcher icon path or URL</li>
     *     <li>{@code keyPress} — a nested map of {@link KeyPress} constructor arguments</li>
     * </ul>
     *
     * @param args map of page properties
     */
    Page(Map args = [:]) {
        super(args)

        viewPath = '/dueuno/elements/pages/'

        modal = addComponent(PageModal, 'modal', args)
        messageBox = addComponent(PageMessageBox, 'messageBox', args)
        keyPress = addComponent(KeyPress, 'keyPress', args.keyPress as Map)

        favicon = args.favicon
        appicon = args.appicon
    }

    /**
     * Returns the global components registry used by GSP views to include the JavaScript and
     * CSS assets contributed by third-party Elements components (see {@code _Shell.gsp}).
     * <p><strong>Internal use only.</strong></p>
     *
     * @return the list of registered component asset identifiers
     */
    List<String> getComponentsRegistry() {
        return Elements.componentsRegistry
    }
}
