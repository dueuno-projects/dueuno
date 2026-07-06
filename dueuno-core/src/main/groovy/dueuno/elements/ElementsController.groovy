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

import dueuno.commons.utils.LogUtils
import dueuno.core.LinkGeneratorAware
import dueuno.core.WebRequestAware
import dueuno.elements.contents.ContentHeader
import dueuno.elements.pages.PageBlank
import dueuno.exceptions.ElementsException
import grails.artefact.Controller
import grails.artefact.Enhances
import grails.artefact.controller.RestResponder
import grails.validation.Validateable
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.core.util.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.validation.Errors

/**
 * Grails controller trait that provides the core Elements action lifecycle to every
 * controller that mixes it in.
 * <p>
 * {@code ElementsController} is automatically applied to all Grails controllers in an
 * Elements application via the {@code @Enhances("Controller")} annotation. It adds the
 * following capabilities:
 * </p>
 * <ul>
 *     <li>Rendering {@link Transition} responses for AJAX requests ({@link #display}).</li>
 *     <li>Rendering full {@link Page} responses for direct browser requests ({@link #display}).</li>
 *     <li>Factory methods for creating {@link Page}, {@link PageContent}, and {@link Transition} instances.</li>
 *     <li>File download helpers ({@link #download}, {@link #getDownloadOutputStream}).</li>
 *     <li>Keyboard input access ({@link #getKeyPressed}).</li>
 * </ul>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
@Enhances("Controller")
trait ElementsController implements Controller, RestResponder, WebRequestAware, LinkGeneratorAware {

    private Logger log = LoggerFactory.getLogger(ElementsController)
    private String DISPLAY_EXCEPTION_MESSAGE = "'display()' must be the last statement of an action"

    /**
     * Property-style shorthand for {@link #display(Map)} with no arguments.
     * Renders the current {@link Transition} and returns {@code true}.
     * Throws {@link dueuno.exceptions.ElementsException} if called more than once per action.
     *
     * @return {@code true} after the transition has been rendered
     * @throws dueuno.exceptions.ElementsException if {@code display} has already been rendered in this action
     */
    @CompileDynamic
    Boolean getDisplay() {
        if (requestParams._21TransitionRendered) {
            throw new ElementsException(DISPLAY_EXCEPTION_MESSAGE)
        }

        try {
            render transition()
            requestParams._21TransitionRendered = true

        } catch (Exception ignore) {
            log.error LogUtils.logStackTrace(ignore)
        }
        return true
    }

    /**
     * Renders the response for the current action, choosing between a {@link Transition}
     * (AJAX) or a full {@link Page} depending on whether the request carries the
     * {@code _21Transition} flag.
     * <p>
     * Supported {@code args} keys:
     * </p>
     * <ul>
     *     <li>{@code page} — a {@link Page} instance to render directly (forces a full-page render)</li>
     *     <li>{@code content} — a {@link PageContent} to inject into the transition or page</li>
     *     <li>{@code message} / {@code messageArgs} — show an info message in the message box</li>
     *     <li>{@code confirmMessage} / {@code messageArgs} — show a confirmation dialog</li>
     *     <li>{@code errorMessage} / {@code messageArgs} — show an error message</li>
     *     <li>{@code exception} — log and display the exception message as an error</li>
     *     <li>{@code errors} — display field-level or global validation errors on a submitted component</li>
     *     <li>{@code controller} / {@code action} — redirect the browser</li>
     *     <li>{@code loading} — show or hide the loading indicator</li>
     *     <li>{@code transition} — use an explicit {@link Transition} instance instead of creating a new one</li>
     * </ul>
     * <p>
     * This method must be the last statement of a controller action; calling it more than
     * once throws {@link dueuno.exceptions.ElementsException}.
     * </p>
     *
     * @param args optional map of display arguments (see above)
     * @throws dueuno.exceptions.ElementsException if {@code display} has already been rendered in this action
     */
    @CompileDynamic
    void display(Map args = [:]) {
        if (requestParams._21TransitionRendered) {
            throw new ElementsException(DISPLAY_EXCEPTION_MESSAGE)
        }

        StopWatch sw = new StopWatch()
        if (!args.page && requestParams._21Transition) {
            try {
                sw.start()
                render transition(args)
                requestParams._21TransitionRendered = true
                sw.stop()

//                if (EnvUtils.isDevelopment()) {
//                    log.warn "TRANSITION rendered in ${sw.lastTaskTimeMillis}ms, args: ${args}"
//                }

            } catch (Exception ignore) {
                log.error LogUtils.logStackTrace(ignore)
            }

        } else { // When the user hits the browser REFRESH button
            try {
                sw.start()
                response.setHeader("Cache-Control", "no-store")
                render page(args)
                requestParams._21TransitionRendered = true
                sw.stop()

//                if (EnvUtils.isDevelopment()) {
//                    log.warn "PAGE rendered in ${sw.lastTaskTimeMillis}ms, args: ${args}"
//                }

            } catch (Exception ignore) {
                log.error LogUtils.logStackTrace(ignore)
            }
        }
    }

    Boolean isMobileDevice() {
        return getMainPage().isMobileDevice()
    }

    /**
     * Returns the name of the key that was pressed by the user in the current request,
     * as reported by the main page's key-press component.
     *
     * @return the pressed key name, or {@code null} if no key was pressed
     */
    String getKeyPressed() {
        return getMainPage().keyPress.keyPressed
    }

    /** Returns the application-scoped {@link PageService} bean. */
    private PageService getPageService() {
        return Elements.getBean('pageService') as PageService
    }

    /** Returns the main {@link Page} for the current session, falling back to a blank page. */
    private Page getMainPage() {
        return getPageService().mainPage ?: createPage(PageBlank, params)
    }

    /**
     * Creates and returns a new instance of the specified {@link Page} subclass.
     *
     * @param clazz the {@link Page} subclass to instantiate
     * @param args  optional map of constructor/property arguments
     * @return the newly created page instance
     */
    public <T> T createPage(Class<T> clazz, Map args = [:]) {
        return getPageService().createPage(clazz, args)
    }

    /**
     * Creates and returns the default {@link ContentHeader} for the current page.
     *
     * @return a new {@link ContentHeader} instance
     */
    ContentHeader createContent() {
        return getPageService().createContent()
    }

    /**
     * Creates and returns a new instance of the specified {@link PageContent} subclass.
     *
     * @param clazz the {@link PageContent} subclass to instantiate
     * @param args  optional map of constructor/property arguments
     * @return the newly created content instance
     */
    public <T> T createContent(Class<T> clazz, Map args = [:]) {
        return getPageService().createContent(clazz, args)
    }

    /**
     * Creates and returns a new {@link Transition} scoped to the current request.
     *
     * @return a new {@link Transition} instance
     */
    Transition createTransition() {
        return getPageService().createTransition()
    }

    /**
     * Returns an {@link OutputStream} that streams a file to the browser.
     *
     * @param pathname the server-side path of the file to stream
     * @param inline   {@code true} to suggest inline display (e.g. PDF preview);
     *                 {@code false} (default) to force a download prompt
     * @return the output stream to write the file content to
     */
    OutputStream getDownloadOutputStream(String pathname, Boolean inline = false) {
        return getPageService().getDownloadOutputStream(pathname, inline)
    }

    /**
     * Streams a file from the server to the browser as a download (or inline display).
     *
     * @param pathname the server-side path of the file to send
     * @param inline   {@code true} to suggest inline display; {@code false} (default) to force download
     */
    void download(String pathname, Boolean inline = false) {
        getPageService().download(pathname, inline)
    }

    /**
     * Builds the Grails render model for a {@link Transition} response, interpreting
     * the {@code args} map to populate the transition with the appropriate commands
     * (content render, messages, errors, redirect, loading indicator, etc.).
     */
    private Map transition(Map args = [:]) {
        Transition t = args.transition as Transition ?: createTransition()

        if (args.loading != null) {
            t.loading(args.loading as Boolean)
        }

        if (args.content) {
            PageContent content = args.content as PageContent
            content.setRenderProperties(args)
            t.renderContent(content)

        } else if (args.message) {
            String message = args.message as String
            List messageArgs = args.messageArgs as List ?: []
            t.infoMessage(message, messageArgs, new ComponentEvent(args))

        } else if (args.confirmMessage) {
            String message = args.confirmMessage as String
            List messageArgs = args.messageArgs as List ?: []
            t.confirmMessage(message, messageArgs, new ComponentEvent(args))

        } else if (args.errorMessage) {
            String message = args.errorMessage as String
            List messageArgs = args.messageArgs as List ?: []
            t.errorMessage(message, messageArgs, new ComponentEvent(args))

        } else if (args.exception) {
            Exception e = args.exception as Exception
            log.error LogUtils.logStackTrace(e)
            String message = e.message ?: e.cause.message ?: "${e.toString()} caused by ${e.cause.toString()}"
            t.errorMessage(message , new ComponentEvent(args))

        } else if (args.errors) {
            Integer submittedComponentCount = requestParams._21SubmittedCount as Integer
            String submittedComponentName = requestParams._21SubmittedName as String
            Object componentErrors = args.errors

            if (submittedComponentCount > 1) { // Multiple components submitted
                if (componentErrors !in Map) {
                    t.errorMessage("Multiple components submitted, please set 'errors' as a Map (Eg. 'display errors: [formName1: obj1, formName2: obj2, ...]'")

                } else {
                    for (component in componentErrors as Map) {
                        String componentName = component.key
                        Object componentError = component.value
                        Object errors = getComponentErrors(t, componentName, componentError)
                        t.set(componentName, 'errors', errors)
                    }
                }

            } else if (submittedComponentCount == 1) { // Single component submitted
                Object errors = getComponentErrors(t, submittedComponentName, componentErrors)
                t.set(submittedComponentName, 'errors', errors)

            } else try { // no component submitted
                if (componentErrors['errors']) {
                    Errors errors = componentErrors['errors'] as Errors
                    if (errors.globalError) {
                        t.errorMessage(errors.globalError.codes[1])
                    } else {
                        t.errorMessage(errors.allErrors.join('. '))
                    }

                } else if (componentErrors in Map) {
                    t.errorMessage("No component submitted, please specify one in the event definition.")

                } else {
                    throw new Exception("Wrong use of the 'errors' feature.")
                }
            } catch (Exception ignore) {
                t.errorMessage("Cannot display errors, please refer to the user guide.")
            }

        } else if (args.controller || args.action) {
            t.redirect(args)
        }

        return [
                template: t.view,
                model   : t.model,
        ]
    }

    /**
     * Converts a plain {@code fieldName → errorMessage} map into the structured error list
     * format expected by the Elements frontend component.
     *
     * @param componentName the identifier of the target component (used for debug logging)
     * @param errorsMap     a map of field names to i18n error message keys or literal messages
     * @return a map with an {@code errors} key holding a list of {@code {field, message}} maps
     */
    private Map getErrorsFromMap(String componentName, Map errorsMap) {
        List<Map> errors = []

        for (error in errorsMap) {
            String fieldName = error.key
            String fieldError = message(error.value as String)
            errors.add([
                    field  : fieldName,
                    message: fieldError,
            ])
            log.debug "[${componentName}] ${fieldName}: ${fieldError}"
        }

        return [errors: errors]
    }

    /**
     * Extracts the Spring {@link org.springframework.validation.Errors} object from a
     * Grails {@link grails.validation.Validateable} (or GORM domain) instance and logs each error.
     *
     * @param componentName the identifier of the target component (used for debug logging)
     * @param validateable  a Grails {@link grails.validation.Validateable} or GORM domain instance
     * @return the {@link org.springframework.validation.Errors} object from the validateable
     */
    private Errors getErrorsFromValidatable(String componentName, Object validateable) {
        Errors errors = validateable['errors'] as Errors
        for (error in errors.allErrors) {
            log.debug "[${componentName}] " + message(error.defaultMessage, error.arguments)
        }
        return errors
    }

    /**
     * Resolves the error representation for a single component, dispatching to the
     * appropriate helper based on the type of {@code componentErrors}:
     * <ul>
     *     <li>{@link Map} — delegates to {@link #getErrorsFromMap}</li>
     *     <li>Object with an {@code errors} property ({@link grails.validation.Validateable} / GORM) — delegates to {@link #getErrorsFromValidatable}</li>
     *     <li>Anything else — adds an error message to {@code t} and returns an empty map</li>
     * </ul>
     *
     * @param t               the current {@link Transition} (used to display an error message on unsupported types)
     * @param componentName   the identifier of the target component
     * @param componentErrors the errors object (a {@link Map}, a validateable, or a GORM domain instance)
     * @return the resolved error structure to set on the component
     */
    private Object getComponentErrors(Transition t, String componentName, Object componentErrors) {
        if (componentErrors in Map) {
            // display errors: [field1: 'Some error']
            return getErrorsFromMap(componentName, componentErrors as Map)

        } else if (componentErrors.hasProperty('errors') || componentErrors['errors'] in Errors) {
            // display errors: gormObject (or grailsValidator)
            return getErrorsFromValidatable(componentName, componentErrors)

        } else {
            // No other ways to submit errors at the moment
            t.errorMessage("Cannot use object '${componentErrors.class.name}' to display errors. Please specify a Map (eg. [fieldname: 'Some error']) or an instance of an object implementing '${Validateable.name}'")
            return [:]
        }
    }

    /**
     * Builds the Grails render model for a full {@link Page} response (used when the browser
     * refreshes or when {@code args.page} is explicitly set). Injects a {@link PageContent}
     * into the page if {@code args.content} is provided.
     *
     * @param args the display arguments; may contain {@code page} and/or {@code content} keys
     * @return the Grails render model map with {@code template} and {@code model} keys
     */
    private Map page(Map args) {
        Page p

        if (args.page) {
            p = args.page as Page
            args.remove('page')
        } else {
            p = getMainPage()
        }

        if (args.content) {
            PageContent content = args.content as PageContent
            p.content = content
        }

        return [
                template: p.view,
                model   : p.model + args,
        ]
    }
}
