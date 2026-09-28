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
package dueuno.elements.core

import dueuno.elements.ElementsException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * INTERNAL USE ONLY
 * The Transformer lets the user register closures to transform a value to any kind of other value. It works in
 * pair with PrettyPrinter
 *
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class Transformer {

    private static Map<String, Closure> transformersRegistry = [:]

    static void register(String name, Closure transformer) {
        transformersRegistry[name] = transformer
    }

    static Object transform(String transformerName, Object value) {
        if (value == null) {
            return null
        }

        Closure transformer = transformersRegistry[transformerName]
        if (!transformer) {
            String transformers = "'${transformersRegistry.collect { it.key }.join("', '")}'"
            throw new ElementsException("Cannot find a transformer called '${transformerName}' in registered transformers: ${transformers}")
        }

        Object result
        try {
            result = transformer.call(value)

        } catch (Exception e) {
            result = "Error transforming value '${value}' with transformer '${transformerName}': ${e.message}"
            //throw new ElementsException("Error transforming value '${value}' with transformer '${transformerName}': ${e.message}", e)

        } finally {
            return result
        }
    }

}
