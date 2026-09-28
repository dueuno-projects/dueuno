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
package dueuno.commons.utils

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Utility class for logging operations and exceptions.
 * <p>
 * Provides methods to format stack traces, log operations with object properties,
 * and benchmark code execution time.
 * </p>
 * Author: Gianluca Sartori
 */
@Slf4j
@CompileStatic
class LogUtils {

    /**
     * Converts the stack trace of an exception into a string.
     *
     * @param e the exception to log
     * @return a string containing the stack trace
     */
    static String logStackTrace(Exception e) {
        StringWriter sw = new StringWriter()
        PrintWriter pw = new PrintWriter(sw)
        e.printStackTrace(pw)
        return sw.toString()
    }

    /**
     * Joins a list of error messages into a single string separated by new lines.
     *
     * @param errorArray the list of error messages
     * @return a string containing all errors separated by newline characters
     */
    static String logStackTrace(List<String> errorArray) {
        StringBuilder builder = new StringBuilder()
        for (String s : errorArray) {
            builder.append(s + '\n')
        }
        return builder.toString()
    }

    /**
     * Logs an operation with an optional message.
     *
     * @param operation the name of the operation
     * @param message an optional message to include
     * @return a formatted log string
     */
    static String logOperation(String operation, String message = '') {
        return logOperation(operation, null, [], message)
    }

    /**
     * Logs an operation performed on an object with optional property modifications.
     *
     * @param operation the name of the operation
     * @param object the object involved in the operation
     * @param propertyModifier a closure to modify object properties before logging
     * @return a formatted log string
     */
    static String logOperation(String operation, Object object, Closure propertyModifier = {}) {
        return logOperation(operation, object, [], null, propertyModifier)
    }

    /**
     * Logs an operation performed on an object, specifying which properties to display.
     *
     * @param operation the name of the operation
     * @param object the object involved in the operation
     * @param display the list of property names to display
     * @param propertyModifier a closure to modify object properties before logging
     * @return a formatted log string
     */
    static String logOperation(String operation, Object object, List display, Closure propertyModifier = {}) {
        return logOperation(operation, object, display, null, propertyModifier)
    }

    /**
     * Logs an operation performed on an object with specific properties and an optional message.
     *
     * @param operation the name of the operation
     * @param object the object involved in the operation
     * @param display the list of property names to display
     * @param message an optional message to include
     * @param propertyModifier a closure to modify object properties before logging
     * @return a formatted log string
     */
    static String logOperation(String operation, Object object, List display, String message, Closure propertyModifier = {}) {
        String formattedMessage = message ? " '${message}' " : ' '
        String formattedProperties = ''

        if (object) {
            Map properties = object.properties.findAll { it.key as String != 'class' }
            List propertyNames = properties.collect { it.key as String }
            List propertyNamesToDisplay = display ?: propertyNames

            propertyModifier.call(properties)

            for (propertyName in propertyNamesToDisplay) {
                String key = StringUtils.camelToScreamingSnake(propertyName as String)
                String value = properties[propertyName]
                formattedProperties += "${key}: '${value}' "
            }
        }

        return "${operation}${formattedMessage}${formattedProperties}"
    }

    /**
     * Benchmarks the execution time of a closure and logs the duration if enabled.
     *
     * @param name a descriptive name for the benchmark
     * @param enabled whether benchmarking and logging are enabled
     * @param codeToBenchmark the closure containing the code to execute
     * @return the result of executing the closure
     */
    static Object benchmark(String name, Boolean enabled = true, Closure codeToBenchmark) {
        def result

        if (!enabled) {
            result = codeToBenchmark.call()

        } else {
            def start = System.currentTimeMillis()
            result = codeToBenchmark.call()
            def now = System.currentTimeMillis()
            if (enabled) {
                LogUtils.log.debug "'$name' execution took: ${now - start} ms"
            }
        }

        return result
    }

}
