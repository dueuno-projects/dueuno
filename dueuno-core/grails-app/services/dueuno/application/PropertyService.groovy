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
package dueuno.application

import dueuno.commons.utils.FileUtils
import groovy.transform.CompileStatic

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * @author Gianluca Sartori
 */

@CompileStatic
abstract class PropertyService {

    protected static final Map<String, Map<String, Object>> inMemoryProperties = [:]
    protected static final Map<String, Closure> onChangeRegistry = [:]

    abstract void setValue(PropertyType type, String name, Object value, Object defaultValue = null, String validation = null)

    abstract Object getValue(PropertyType type, String name, Boolean reload = false)

    void onChange(String name, Closure closure) {
        onChangeRegistry[name] = closure
    }

    String getString(String name, Boolean reload = false) {
        return getValue(PropertyType.STRING, name, reload) as String ?: ''
    }

    String getFilename(String name, Boolean reload = false) {
        String value = getValue(PropertyType.FILENAME, name, reload) as String ?: ''
        return FileUtils.normalizePathname(value)
    }

    String getDirectory(String name, Boolean reload = false) {
        String value = getValue(PropertyType.DIRECTORY, name, reload) as String ?: ''
        return FileUtils.normalizePath(value)
    }

    String getUrl(String name, Boolean reload = false) {
        String value = getValue(PropertyType.URL, name, reload) as String ?: ''
        return value
    }

    Number getNumber(String name, Boolean reload = false) {
        return getValue(PropertyType.NUMBER, name, reload) as Number
    }

    Boolean getBoolean(String name, Boolean reload = false) {
        return getValue(PropertyType.BOOL, name, reload) as Boolean
    }

    LocalDate getDate(String name, Boolean reload = false) {
        return getValue(PropertyType.DATE, name, reload) as LocalDate
    }

    LocalTime getTime(String name, Boolean reload = false) {
        return getValue(PropertyType.TIME, name, reload) as LocalTime
    }

    LocalDateTime getDateTime(String name, Boolean reload = false) {
        return getValue(PropertyType.DATETIME, name, reload) as LocalDateTime
    }

    void setBoolean(String name, Boolean value, Boolean defaultValue = null) {
        setValue(PropertyType.BOOL, name, value, defaultValue)
    }

    void setNumber(String name, Number value, Number defaultValue = null) {
        setValue(PropertyType.NUMBER, name, value, defaultValue)
    }

    void setDateTime(String name, LocalDateTime value, LocalDateTime defaultValue = null) {
        setValue(PropertyType.DATETIME, name, value, defaultValue)
    }

    void setDate(String name, LocalDate value, LocalDate defaultValue = null) {
        setValue(PropertyType.DATE, name, value, defaultValue)
    }

    void setTime(String name, LocalTime value, LocalTime defaultValue = null) {
        setValue(PropertyType.TIME, name, value, defaultValue)
    }

    void setString(String name, String value, String defaultValue = null) {
        setValue(PropertyType.STRING, name, value, defaultValue)
    }

    void setFilename(String name, String value, String defaultValue = null) {
        def processedValue = FileUtils.normalizePathname(value)
        def processedDefaultValue = FileUtils.normalizePathname(defaultValue)
        def validation = validateFilename(processedValue)
        setValue(PropertyType.FILENAME, name, processedValue, processedDefaultValue, validation)
    }

    String validateFilename(String value) {
        if (!value)
            return ''

        File f = new File(value)
        if (!f.exists() || !f.isFile()) {
            return "Does not exist or is not a file"
        }

        return ''
    }

    void setDirectory(String name, String value, String defaultValue = null) {
        def processedValue = FileUtils.normalizePath(value)
        def processedDefaultValue = FileUtils.normalizePath(defaultValue)
        def validation = validateDirectory(processedValue)
        setValue(PropertyType.DIRECTORY, name, processedValue, processedDefaultValue, validation)
    }

    String validateDirectory(String value) {
        if (!value)
            return ''

        File f = new File(value)
        if (!f.exists() || !f.isDirectory()) {
            return "Does not exist or is not a directory"
        }

        return ''
    }

    void setUrl(String name, String value, String defaultValue = null) {
        def processedValue = value // TBD
        def processedDefaultValue = defaultValue // TBD
        def validation = validateUrl(processedValue)
        setValue(PropertyType.URL, name, processedValue, processedDefaultValue, validation)
    }

    String validateUrl(String value) {
        if (!value)
            return ''

        try {
            URL url = new URL(value)
            return ''

        } catch (Exception e) {
            return e.message
        }
    }
}
