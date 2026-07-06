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
package dueuno.core

import groovy.transform.CompileStatic

/**
 * Carries the formatting options used by {@code PrettyPrinter} when rendering a typed value
 * as a human-readable string.
 * <p>
 * An instance of this class is passed to every {@link dueuno.types.CustomType#prettyPrint}
 * call and to the various {@code PrettyPrinter.print*()} methods so that formatting
 * decisions (locale, decimal precision, date/time layout, unit display, etc.) are
 * centralised and consistent.
 * </p>
 * <p>
 * All properties are optional; a {@code null} value means "use the default" for that
 * particular formatter.
 * </p>
 *
 * @author Gianluca Sartori
 */

@CompileStatic
class PrettyPrinterProperties implements Serializable {

    /** The {@code PrettyPrinter} instance responsible for rendering; used for delegation within custom types. */
    Object prettyPrinter

    /** Optional name of a value-transformer to apply before rendering. */
    String transformer

    /** Locale used for number formatting, date/time patterns, and i18n lookups. */
    Locale locale

    /** Whether to prepend a translated text prefix to the rendered output. */
    Boolean renderTextPrefix

    /** The i18n message key (or literal text) to use as a prefix when {@link #renderTextPrefix} is {@code true}. */
    String textPrefix

    /** Optional interpolation arguments for the {@link #textPrefix} message. */
    List textArgs

    /** Whether to render a boolean value as a human-readable label (implemented in {@code Label}). */
    Boolean renderBoolean       // Implemented in Label

    /** Whether to apply a visual highlight when the rendered numeric value is negative (implemented in {@code Label}). */
    Boolean highlightNegative   // Implemented in Label

    /** Token used to represent a zero numeric value (e.g. {@code "-"} or {@code "0"}); {@code null} means render normally. */
    String renderZero

    /** Whether to include the date portion when rendering a date/time value. */
    Boolean renderDate

    /** Custom {@link java.time.format.DateTimeFormatter} pattern for the date portion (e.g. {@code "dd/MM/yyyy"}). */
    String renderDatePattern

    /** Whether to include the time portion when rendering a date/time value. */
    Boolean renderTime

    /** Whether to include seconds in the rendered time portion. */
    Boolean renderSeconds

    /** Delimiter placed between the date and time portions (e.g. {@code " "} or {@code "T"}). */
    String renderDelimiter

    /** Number of decimal places used when formatting numeric values. */
    Integer decimals

    /** Java {@link java.text.DecimalFormat} pattern for number formatting (e.g. {@code "#,##0.00"}). */
    String decimalFormat

    /** Whether to place the unit symbol before the numeric value (prefix) rather than after (suffix). */
    Boolean prefixedUnit

    /** Whether to use a symbolic currency representation (e.g. {@code "$"}) instead of the ISO code. */
    Boolean symbolicCurrency

    /** Whether to use a symbolic quantity representation instead of a numeric one. */
    Boolean symbolicQuantity

    /** Whether month and day are swapped in the date pattern (e.g. US-style {@code MM/dd/yyyy}). */
    Boolean invertedMonth

    /** Whether to render time values in 12-hour (AM/PM) format instead of 24-hour. */
    Boolean twelveHours

    /** Whether the week starts on Sunday (US convention) rather than Monday. */
    Boolean firstDaySunday

    /**
     * Creates a {@code PrettyPrinterProperties} from a map of named arguments.
     * Every key corresponds directly to a field of this class; unrecognised keys are ignored.
     *
     * @param args map of formatting options; all entries are optional
     */
    PrettyPrinterProperties(Map args = [:]) {
        prettyPrinter = args.prettyPrinter
        transformer = args.transformer
        locale = args.locale as Locale
        renderTextPrefix = args.renderTextPrefix
        textPrefix = args.textPrefix
        textArgs = args.textArgs as List
        renderBoolean = args.renderBoolean
        highlightNegative = args.highlightNegative
        renderZero = args.renderZero
        renderDate = args.renderDate
        renderDatePattern = args.renderDatePattern
        renderTime = args.renderTime
        renderSeconds = args.renderSeconds
        renderDelimiter = args.renderDelimiter
        decimals = args.decimals as Integer
        decimalFormat = args.decimalFormat
        prefixedUnit = args.prefixedUnit
        symbolicCurrency = args.symbolicCurrency
        symbolicQuantity = args.symbolicQuantity
        invertedMonth = args.invertedMonth
        twelveHours = args.twelveHours
        firstDaySunday = args.firstDaySunday
    }

    /**
     * Bulk-sets multiple properties from a map using Groovy's dynamic {@code setProperty} dispatch.
     * Keys must match field names of this class exactly.
     *
     * @param properties map of property name → value pairs to apply
     */
    void set(Map properties) {
        for (property in properties) {
            setProperty(property.key as String, property.value)
        }
    }

    /**
     * Sets {@link #textPrefix} to the given value and automatically enables
     * {@link #renderTextPrefix} so the prefix is included in the rendered output.
     *
     * @param value the i18n message key or literal text to use as the prefix
     */
    void setTextPrefix(String value) {
        textPrefix = value
        renderTextPrefix = true
    }
}