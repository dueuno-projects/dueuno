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
package dueuno.types

import dueuno.exceptions.ElementsException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.math.RoundingMode
import java.text.ParseException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Static utility class for type registration, identification, serialisation, and
 * deserialisation of values exchanged between the Elements server and frontend.
 * <p>
 * Elements uses a simple typed-value protocol: each value is wrapped in a map with
 * {@code type} and {@code value} keys. Built-in types are defined by the {@link Type}
 * enum; additional application-specific types can be registered at startup via
 * {@link #register(Class)}, provided they implement {@link CustomType}.
 * </p>
 * <p>
 * Supported built-in Java types and their corresponding {@link Type} constants:
 * </p>
 * <ul>
 *     <li>{@link Boolean} → {@link Type#BOOL}</li>
 *     <li>{@link Number} → {@link Type#NUMBER}</li>
 *     <li>{@link String} / {@link Enum} → {@link Type#TEXT}</li>
 *     <li>{@link Map} → {@link Type#MAP}</li>
 *     <li>{@link java.util.List} → {@link Type#LIST}</li>
 *     <li>{@link java.time.LocalDateTime} → {@link Type#DATETIME}</li>
 *     <li>{@link java.time.LocalDate} → {@link Type#DATE}</li>
 *     <li>{@link java.time.LocalTime} → {@link Type#TIME}</li>
 *     <li>{@code null} → {@link Type#NA}</li>
 * </ul>
 *
 * @author Gianluca Sartori
 */

@Slf4j
@CompileStatic
class Types {

    private static Map<String, Class> registry = [:]

    /**
     * Registers a custom type so that it can be serialised and deserialised by the framework.
     * The supplied class must implement {@link CustomType} and expose a static {@code TYPE_NAME}
     * field that serves as the unique type identifier.
     *
     * @param type the class to register; must implement {@link CustomType}
     * @throws dueuno.exceptions.ElementsException if the class does not implement {@link CustomType}
     */
    static register(Class type) {
        if (type !in CustomType) {
            throw new ElementsException("Cannot register class '${type}'. Ony classes implementing '${CustomType.getName()}' can be registered as custom types.")
        }

        String typeName = type['TYPE_NAME']
        registry[typeName] = type
    }

    /**
     * Returns {@code true} if a custom type with the given type-name string has been registered.
     *
     * @param typeName the type identifier (value of {@code TYPE_NAME} on the custom type class)
     * @return {@code true} if the type is registered, {@code false} otherwise
     */
    static Boolean isRegistered(String typeName) {
        return registry[typeName]
    }

    /**
     * Returns {@code true} if the runtime class of {@code value} has been registered as a custom type.
     *
     * @param value the object whose class is checked
     * @return {@code true} if the class is a registered custom type
     */
    static Boolean isRegistered(Object value) {
        return isRegistered(value.class)
    }

    /**
     * Returns {@code true} if the given class implements {@link CustomType} and has been registered.
     *
     * @param type the class to check
     * @return {@code true} if the class is a registered custom type
     */
    static Boolean isRegistered(Class type) {
        if (type !in CustomType) {
            return false
        }

        return registry[type['TYPE_NAME']]
    }

    /**
     * Instantiates and returns a new instance of the custom type identified by {@code typeName}.
     *
     * @param typeName the type identifier
     * @return a new {@link CustomType} instance
     * @throws dueuno.exceptions.ElementsException if the type has not been registered
     */
    private static CustomType create(String typeName) {
        if (registry.containsKey(typeName)) {
            return registry[typeName].getDeclaredConstructor().newInstance() as CustomType

        } else {
            throw new ElementsException("Cannot instantiate custom type '${typeName}', please register the new type (eg. Types.register('CUSTOM_TYPE', CustomType)")
        }
    }

    /**
     * Serialises every entry of a flat map using {@link #serializeValue(Object, String)}.
     * Nested {@link Map} values are serialised recursively and wrapped with type {@link Type#MAP}.
     *
     * @param items the map whose values should be serialised
     * @return a new map where every value has been replaced by its typed-value representation
     */
    static Map serialize(Map items) {
        Map results = [:]

        for (item in items) {
            if (item.value in Map) {
                results[item.key] = [type: Type.MAP.toString(), value: serialize(item.value as Map)]

            } else {
                results[item.key] = serializeValue(item.value)
            }
        }

        return results
    }

    /**
     * Returns a human-readable list of all known type names — both built-in
     * ({@link Type} enum constants) and registered custom types — including their
     * corresponding Java class names.
     *
     * @return a list of strings in the form {@code "TYPE_NAME (fully.qualified.ClassName)"}
     */
    static List<String> getAvailableTypeNames() {
        List primitiveTypes = Type.values().collect {"${it.name()} (${it.clazz?.name})" }
        List customTypes = registry.collect { "${it.key} (${it.value.name})"}
        return (primitiveTypes + customTypes) as List<String>
    }

    /**
     * Returns {@code true} if {@code valueType} is recognised as either a built-in
     * {@link Type} constant or a registered custom type name.
     *
     * @param valueType the type name string to check
     * @return {@code true} if the type is known
     */
    static Boolean isType(String valueType) {
        Boolean isPrimitiveType = valueType in Type.values()*.toString()
        Boolean isCustomType = isRegistered(valueType)
        return isPrimitiveType || isCustomType
    }

    /**
     * Determines and returns the {@link Type} constant (as a string) that corresponds
     * to the runtime type of {@code value}.
     * <ul>
     *     <li>{@code null} → {@link Type#NA}</li>
     *     <li>{@link Enum} → {@link Type#TEXT}</li>
     *     <li>Registered {@link CustomType} → the value of its {@code TYPE_NAME} field</li>
     * </ul>
     *
     * @param value the object to inspect
     * @return the type name string for {@code value}
     * @throws dueuno.exceptions.ElementsException if the runtime class is not a known type
     */
    static String getType(Object value) {
        if (value == null) {
            return Type.NA
        }

        if (value in Enum) {
            return Type.TEXT
        }

        if (isRegistered(value)) {
            return value.getClass()['TYPE_NAME']
        }

        switch (value) {
            case Boolean:
                return Type.BOOL

            case Number:
                return Type.NUMBER

            case String:
                return Type.TEXT

            case Map:
                return Type.MAP

            case List:
                return Type.LIST

            case LocalDateTime:
                return Type.DATETIME

            case LocalDate:
                return Type.DATE

            case LocalTime:
                return Type.TIME

            case Enum:
                return Type.TEXT

            default:
                throw new ElementsException("Object of class '${value.getClass()}' cannot be identified as one of the available types: ${availableTypeNames}.")
        }
    }

    /**
     * Wraps a single value in the typed-value map protocol used by the Elements frontend.
     * <ul>
     *     <li>If {@code value} is {@code null}, returns {@code {type: valueType|"NA", value: null}}.</li>
     *     <li>If {@code value} implements {@link CustomType}, delegates to {@link CustomType#serialize()}.</li>
     *     <li>{@link java.time.LocalDate}, {@link java.time.LocalTime}, and {@link java.time.LocalDateTime}
     *         are decomposed into their individual numeric fields.</li>
     *     <li>{@link Enum} values are serialised using their {@link Enum#name()}.</li>
     *     <li>All other known types are wrapped as-is.</li>
     * </ul>
     *
     * @param value     the object to serialise; may be {@code null}
     * @param valueType optional type override used only when {@code value} is {@code null}
     *                  or falls through to the {@code default} branch
     * @return a map with {@code type} and {@code value} keys
     */
    static Map serializeValue(Object value, String valueType = null) {
        if (value == null) {
            return [
                    type: valueType ?: Type.NA.toString(),
                    value: value,
            ]
        }

        Class valueTypeClass = value.getClass()
        if (valueTypeClass in CustomType) {
            return (value as CustomType).serialize()
        }

        switch (value) {
            case Boolean:
                return [
                        type : Type.BOOL.toString(),
                        value: value,
                ]

            case Number:
                return [
                        type : Type.NUMBER.toString(),
                        value: value,
                ]

            case String:
                return [
                        type : Type.TEXT.toString(),
                        value: value,
                ]

            case Map:
                return [
                        type : Type.MAP.toString(),
                        value: value,
                ]

            case List:
                return [
                        type : Type.LIST.toString(),
                        value: value,
                ]

            case LocalDateTime:
                return [
                        type : Type.DATETIME.toString(),
                        value: [
                                year  : (value as LocalDateTime).year,
                                month : (value as LocalDateTime).monthValue,
                                day   : (value as LocalDateTime).dayOfMonth,
                                hour  : (value as LocalDateTime).hour,
                                minute: (value as LocalDateTime).minute,
                        ]
                ]

            case LocalDate:
                return [
                        type : Type.DATE.toString(),
                        value: [
                                year : (value as LocalDate).year,
                                month: (value as LocalDate).monthValue,
                                day  : (value as LocalDate).dayOfMonth,
                        ]
                ]

            case LocalTime:
                return [
                        type : Type.TIME.toString(),
                        value: [
                                hour  : (value as LocalTime).hour,
                                minute: (value as LocalTime).minute,
                        ]
                ]

            case Enum:
                return [
                        type: Type.TEXT.toString(),
                        value: (value as Enum).name(),
                ]

            default:
                return [
                        type: valueType ?: Type.NA.toString(),
                        value: value,
                ]
        }
    }

    /**
     * Deserialises an entire map of typed-value entries, delegating each entry to
     * {@link #deserializeValue(Object)}.
     *
     * @param values a map whose values are typed-value maps (with {@code type} and {@code value} keys)
     * @return a new map with every value replaced by its deserialised Java object
     */
    static Map deserialize(Map values) {
        Map results = [:]

        for (value in values) {
            String valueName = value.key
            Object valueMap = value.value
            results[valueName] = deserializeValue(valueMap)
        }

        return results
    }

    /**
     * Deserialises a single typed-value representation back to its Java object.
     * <p>
     * If {@code value} is not a {@link Map} it is returned as-is. Otherwise the
     * {@code type} key is used to dispatch to the appropriate typed deserialiser.
     * Unknown types are handled by looking them up in the custom type registry;
     * if not found, the raw {@code value} entry is returned.
     * </p>
     *
     * @param value the typed-value map to deserialise, or a raw value
     * @return the deserialised Java object, or {@code null} on error
     */
    static Object deserializeValue(Object value) {
        if (value == null) {
            return null
        }

        if (value !in Map) {
            return value
        }

        Map valueMap = value as Map
        try {
            switch (valueMap.type) {
                case Type.BOOL.toString():
                    return deserializeBoolean(valueMap)

                case Type.NUMBER.toString():
                    return deserializeNumber(valueMap)

                case Type.TEXT.toString():
                    return deserializeString(valueMap)

                case Type.MAP.toString():
                    return deserializeMap(valueMap)

                case Type.LIST.toString():
                    return deserializeList(valueMap)

                case Type.DATETIME.toString():
                    return deserializeLocalDateTime(valueMap)

                case Type.DATE.toString():
                    return deserializeLocalDate(valueMap)

                case Type.TIME.toString():
                    return deserializeLocalTime(valueMap)

                case Type.NA.toString():
                    return valueMap.value

                default:
                    try {
                        CustomType customTypeValue = create(valueMap.type as String)
                        customTypeValue.deserialize(valueMap)
                        return customTypeValue

                    } catch (Exception ignore) {
                        return valueMap.value
                    }
            }

        } catch (Exception e) {
            log.error "Error deserializing '${valueMap}': ${e.message}"
            return null
        }
    }

    /**
     * Extracts a {@link Boolean} from a typed-value map.
     *
     * @param valueMap the typed-value map with type {@link Type#BOOL}
     * @return the deserialised {@link Boolean}
     */
    static Boolean deserializeBoolean(Map valueMap) {
        Boolean result = valueMap.value as Boolean
        return result
    }

    /**
     * Extracts a {@link BigDecimal} from a typed-value map, delegating to
     * {@link #deserializeBigDecimal(String, Integer)}.
     *
     * @param valueMap the typed-value map with type {@link Type#NUMBER};
     *                 may contain an optional {@code decimals} key for scale
     * @return the deserialised {@link BigDecimal}
     */
    static BigDecimal deserializeNumber(Map valueMap) {
        BigDecimal result = deserializeBigDecimal(valueMap.value as String, valueMap.decimals as Integer)
        return result
    }

    /**
     * Extracts a {@link String} from a typed-value map.
     *
     * @param valueMap the typed-value map with type {@link Type#TEXT}
     * @return the deserialised {@link String}
     */
    static String deserializeString(Map valueMap) {
        return valueMap.value
    }

    /**
     * Recursively deserialises a nested map from a typed-value map with type {@link Type#MAP}.
     *
     * @param valueMap the typed-value map whose {@code value} key holds the nested map
     * @return the deserialised {@link Map}, or an empty map if the value is absent or not a map
     */
    static Map deserializeMap(Map valueMap) {
        if (valueMap.value !in Map) {
            return [:]
        }

        return deserialize(valueMap.value as Map)?: [:]
    }

    /**
     * Deserialises a list from a typed-value map with type {@link Type#LIST},
     * applying {@link #deserializeValue(Object)} to each element.
     *
     * @param valueMap the typed-value map whose {@code value} key holds the list
     * @return the deserialised {@link List}, or an empty list if the value is absent or not a list
     */
    static List deserializeList(Map valueMap) {
        if (!valueMap) {
            return []
        }

        if (valueMap.value !in List) {
            return []
        }

        return (valueMap.value as List).collect { item ->
            deserializeValue(item)
        }
    }

    /**
     * Parses a string representation of a decimal number and returns it as a
     * {@link BigDecimal} with the requested scale (defaulting to 2 decimal places).
     *
     * @param value    the string to parse
     * @param decimals the number of decimal places for scaling; defaults to {@code 2} if {@code null}
     * @return the parsed {@link BigDecimal}, or {@code null} if parsing fails
     */
    static BigDecimal deserializeBigDecimal(String value, Integer decimals) {
        BigDecimal result
        try {
            result = new BigDecimal(value)
            result.setScale(decimals ?: 2, RoundingMode.HALF_UP)

        } catch (ParseException ignore) {
            return null

        } finally {
            return result
        }
    }

    /**
     * Reconstructs a {@link LocalDate} from a typed-value map with type {@link Type#DATE}.
     * The {@code value} entry must contain {@code year}, {@code month}, and {@code day} keys.
     *
     * @param valueMap the typed-value map to deserialise
     * @return the reconstructed {@link LocalDate}, or {@code null} if any field is missing
     */
    static LocalDate deserializeLocalDate(Map valueMap) {
        Map date = valueMap.value as Map
        Integer day = (Integer) date.day
        Short month = (Short) date.month
        Short year = (Short) date.year

        if (day != null && month != null && year != null) {
            return LocalDate.of(year, month, day)
        }

        return null
    }

    /**
     * Reconstructs a {@link LocalTime} from a typed-value map with type {@link Type#TIME}.
     * The {@code value} entry must contain {@code hour} and {@code minute} keys.
     *
     * @param valueMap the typed-value map to deserialise
     * @return the reconstructed {@link LocalTime}, or {@code null} if any field is missing
     */
    static LocalTime deserializeLocalTime(Map valueMap) {
        Map time = valueMap.value as Map
        Byte hour = (Byte) time.hour
        Byte minute = (Byte) time.minute

        if (hour != null && minute != null) {
            return LocalTime.of(hour, minute)
        }

        return null
    }

    /**
     * Reconstructs a {@link LocalDateTime} from a typed-value map with type {@link Type#DATETIME}.
     * The {@code value} entry must contain {@code year}, {@code month}, {@code day},
     * {@code hour}, and {@code minute} keys.
     *
     * @param valueMap the typed-value map to deserialise
     * @return the reconstructed {@link LocalDateTime}, or {@code null} if any field is missing
     */
    static LocalDateTime deserializeLocalDateTime(Map valueMap) {
        Map date = valueMap.value as Map
        Short year = (Short) date.year
        Short month = (Short) date.month
        Integer day = (Integer) date.day
        Byte hour = (Byte) date.hour
        Byte minute = (Byte) date.minute

        if (day != null && month != null && year != null && hour != null && minute != null) {
            return LocalDateTime.of(year, month, day, hour, minute)
        }

        return null
    }

}
