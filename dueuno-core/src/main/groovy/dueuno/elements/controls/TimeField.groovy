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
package dueuno.elements.controls

import dueuno.types.Type
import groovy.transform.CompileStatic

/**
 * A time picker control backed by {@link java.time.LocalTime}.
 * <p>
 * Extends {@link DateTimeField} with the value type fixed to {@link dueuno.types.Type#TIME}
 * and an input validation pattern that restricts entry to digits and colons
 * ({@code ^[0-9:]*$}), suitable for time input in the format {@code HH:mm}.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */
@CompileStatic
class TimeField extends DateTimeField {

    /**
     * Creates a {@code TimeField} instance configured from the supplied argument map.
     * Sets the value type to {@link dueuno.types.Type#TIME}.
     *
     * @param args initialisation arguments forwarded to {@link DateTimeField#DateTimeField(Map)}
     */
    TimeField(Map args) {
        super(args)

        valueType = Type.TIME
    }

    /**
     * Serialises this field's properties to JSON, adding the time input validation
     * pattern ({@code ^[0-9:]*$}) to restrict entry to digits and colons.
     *
     * @param properties additional properties to merge before serialisation
     * @return the JSON string representation of this field's properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                pattern: '^[0-9:]*$',
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

}
