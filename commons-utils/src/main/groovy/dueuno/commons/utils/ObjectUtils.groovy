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

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Utility class for dynamically accessing object properties, including nested properties.
 * <p>
 * This class allows retrieving the value of a field from an object using a dotted path notation.
 * It is particularly useful when working with domain objects or maps with nested structures.
 * </p>
 *
 * <h3>Example usage:</h3>
 * <pre>
 * def person = [name: 'Alice', address: [street: 'Main St', city: 'Springfield']]
 *
 * // Retrieve top-level property
 * def name = ObjectUtils.getValue(person, 'name')  // returns 'Alice'
 *
 * // Retrieve nested property
 * def street = ObjectUtils.getValue(person, 'address.street')  // returns 'Main St'
 *
 * // Access non-existent property safely
 * def zip = ObjectUtils.getValue(person, 'address.zip')  // returns null
 * </pre>
 *
 * Author: Gianluca Sartori
 */
@Slf4j
@CompileStatic
class ObjectUtils {

    static Boolean hasId(Object obj) {
        if (!obj) {
            return false
        }

        if (obj in Map) {
            return (obj as Map).containsKey('id')
        }

        if (obj.hasProperty('id')) {
            return true
        }

        return false
    }

    static Boolean isBasicType(Object object) {
        return false
                || object in Boolean
                || object in String
                || object in Number
                || object in LocalDate
                || object in LocalTime
                || object in LocalDateTime
    }

    /**
     * Retrieves the value of a property from an object using a dotted path notation.
     * <p>
     * Supports recursive access to nested properties.
     *
     * @param object the object containing the property
     * @param propertyName the dotted path name of the property (e.g., 'person.address.street')
     * @return the value of the property, or null if not found
     *
     * <h3>Example:</h3>
     * <pre>
     * def person = [name: 'Alice', address: [street: 'Main St']]
     * ObjectUtils.getValue(person, 'name')          // returns 'Alice'
     * ObjectUtils.getValue(person, 'address.street') // returns 'Main St'
     * ObjectUtils.getValue(person, 'address.zip')   // returns null
     * </pre>
     */
    static Object getValue(Object object, String propertyName) {
        if (!object) {
            return null
        }

        if (object.hasProperty(propertyName) && isBasicType(object[propertyName])) {
            return object[propertyName]
        }

        String[] fieldNameList = propertyName.split('\\.')
        if (fieldNameList.size() == 1) {
            return getValueOrThrowException(object, fieldNameList[0])

        } else {
            String nextFieldName = fieldNameList.tail().join('.') ?: [:]
            Object nextValues = getValueOrThrowException(object, fieldNameList[0])
            return getValue(nextValues, nextFieldName)
        }
    }

    /**
     * Retrieves the value of a direct property from an object.
     * <p>
     * If the property does not exist, returns null.
     * If the property exists but cannot be read due to an unexpected error (e.g., lazy-loaded domain object),
     * an exception is thrown with a descriptive message.
     *
     * @param object the object containing the property
     * @param fieldName the name of the property
     * @return the value of the property, or null if the property does not exist
     * @throws Exception if the property cannot be read due to an unexpected error
     *
     * <h3>Example:</h3>
     * <pre>
     * def person = [name: 'Alice', age: 30]
     * ObjectUtils.getValueOrThrowException(person, 'name')  // returns 'Alice'
     * ObjectUtils.getValueOrThrowException(person, 'email') // returns null
     * </pre>
     */
    private static Object getValueOrThrowException(Object object, String fieldName) {
        try {
            Object value = object[fieldName]
            return value

        } catch (MissingPropertyException ignore) {
            return null

        } catch (Exception e) {
            throw new Exception(
                    "Cannot read property '${fieldName}' from object '${object.getClass().getName()}'. " +
                            "If this is a domain object please make sure to set eager fetching" +
                            "for each field that you want to access whose type is a domain class " +
                            "(eg. DomainClass.list(fetch: [fieldName: 'join']): " +
                            e)
        }
    }

    /**
     * Converts a flat map with dot-separated keys into a nested map.
     * <p>For example, the key {@code "day.info.author"} is converted into:
     *
     * <pre>{@code
     * [
     *     day: [
     *         info: [
     *             author: value
     *         ]
     *     ]
     * ]
     * }</pre>
     *
     * @param map the flat map to convert
     * @return a nested map representing the same key/value pairs
     */
    static Map nestMap(Map map) {
        Map result = [:]

        for (entry in map) {
            String key = entry.key
            List<String> parts = key.tokenize('.')
            Map current = result

            for (int i = 0; i < parts.size(); i++) {
                String part = parts[i]

                if (i == parts.size() - 1) {
                    current[part] = entry.value
                } else {
                    current = current.computeIfAbsent(part) { [:] } as Map
                }
            }
        }

        return result
    }

    /**
     * Converts a map with dot-notation keys into a nested map structure,
     * while also preserving the original flat keys.
     *
     * Example:
     * Input:  ["month.day": 1]
     * Output: [month: [day: 1], "month.day": 1]
     *
     * @param map input map with dot-notation keys
     * @return nested map with original keys preserved
     */
    static Map expandDotKeysMap(Map map) {
        Map result = [:]

        for (entry in map) {
            String key = entry.key
            List<String> parts = key.tokenize('.')
            Map current = result

            for (int i = 0; i < parts.size(); i++) {
                String part = parts[i]

                if (i == parts.size() - 1) {
                    current[part] = entry.value
                } else {
                    current = current.computeIfAbsent(part) { [:] } as Map
                }
            }

            result[key] = entry.value
        }

        return result
    }
}
