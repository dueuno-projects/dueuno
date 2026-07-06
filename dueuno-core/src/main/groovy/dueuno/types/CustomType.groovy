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

import dueuno.core.PrettyPrinterProperties
import groovy.transform.CompileStatic

/**
 * Contract for application-defined types that can participate in the Elements
 * typed-value serialisation protocol.
 * <p>
 * Implementors must also declare a {@code public static final String TYPE_NAME}
 * field whose value is the unique identifier used to register the type with
 * {@link Types#register(Class)} and to look it up during deserialisation.
 * </p>
 * <p>
 * Example skeleton:
 * </p>
 * <pre>{@code
 * class MyType implements CustomType {
 *     static final String TYPE_NAME = 'MY_TYPE'
 *     // ...
 * }
 * // Registration (e.g. in Bootstrap):
 * Types.register(MyType)
 * }</pre>
 *
 * @author Gianluca Sartori
 * @see Types
 */
@CompileStatic
interface CustomType {

    /**
     * Serialises this value to the typed-value map protocol expected by the Elements frontend.
     * The returned map must contain at least a {@code type} key (set to {@code TYPE_NAME})
     * and a {@code value} key holding the serialised representation.
     *
     * @return a map with {@code type} and {@code value} keys
     */
    Map serialize()

    /**
     * Populates this instance from a typed-value map previously produced by {@link #serialize()}.
     * Implementations should read the {@code value} entry of {@code valueMap} and restore
     * all internal state from it.
     *
     * @param valueMap the typed-value map to deserialise; contains at least {@code type} and {@code value} keys
     */
    void deserialize(Map valueMap)

    /**
     * Returns a human-readable, locale-aware string representation of this value,
     * formatted according to the supplied {@link PrettyPrinterProperties}.
     *
     * @param properties formatting options such as locale, number format, and date/time patterns
     * @return the formatted string representation of this value
     */
    String prettyPrint(PrettyPrinterProperties properties)

}