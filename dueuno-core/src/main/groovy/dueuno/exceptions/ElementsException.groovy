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
package dueuno.exceptions

import dueuno.core.PrettyPrinter
import dueuno.core.WebRequestAware
import groovy.transform.CompileStatic
import org.springframework.validation.ObjectError

/**
 * The standard exception type used throughout the Elements framework.
 * <p>
 * When thrown inside a web request, {@link #getMessage()} automatically resolves the
 * exception message through the Spring i18n message source using the current request
 * locale and any supplied interpolation arguments. Outside a web request (e.g. in tests
 * or background jobs) the raw message string is returned as-is.
 * </p>
 * <p>
 * This means the message passed to the constructor can be either a literal string or an
 * i18n message key; the correct value will be returned depending on context.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class ElementsException extends Exception implements WebRequestAware {

    /** Optional interpolation arguments for the i18n message lookup. */
    private List args

    /**
     * Creates an {@code ElementsException} from a Spring
     * {@link org.springframework.validation.ObjectError}, using the error's
     * {@link ObjectError#code} as the message key and its
     * {@link ObjectError#arguments} as interpolation arguments.
     *
     * @param error the validation error to convert into an exception
     */
    ElementsException(ObjectError error) {
        super(error.code)
        this.args = error.arguments as List
    }

    /**
     * Creates an {@code ElementsException} with the given message key or literal text
     * and optional interpolation arguments.
     *
     * @param message an i18n message key or a literal error message
     * @param args    optional interpolation arguments used during i18n message resolution
     */
    ElementsException(String message, List args = []) {
        super(message)
        this.args = args as List
    }

    /**
     * Creates an {@code ElementsException} wrapping another throwable.
     * No i18n interpolation arguments are set.
     *
     * @param message an i18n message key or a literal error message
     * @param cause   the underlying cause
     */
    ElementsException(String message, Throwable cause) {
        super(message, cause)
        this.args = []
    }

    /**
     * Returns the exception message, resolved through the i18n message source when a
     * web request is active. Falls back to the raw message string outside a request context.
     *
     * @return the localised message if inside a web request, or the raw message otherwise
     */
    @Override
    String getMessage() {
        if (hasRequest()) {
            return PrettyPrinter.message(locale, super.message, args)
        }

        return super.message
    }
}