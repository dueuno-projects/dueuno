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

import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor

/**
 * Utility class for handling dates and times.
 * <p>
 * This class provides methods to:
 * <ul>
 *     <li>Retrieve the current date and time components (year, month, day, hour, minute, second).</li>
 *     <li>Convert between {@link java.util.Date} and {@link java.time.LocalDateTime}, {@link java.time.LocalDate}, {@link java.time.LocalTime}.</li>
 *     <li>Format dates and times to strings and parse strings to dates and times.</li>
 *     <li>Generate timestamps suitable for filenames.</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * String year = DateUtils.getCurrentYear(); // e.g., "2026"
 * String timestamp = DateUtils.getFilenameTimestamp(); // e.g., "2026-02-10-14-35-20"
 *
 * LocalDateTime ldt = DateUtils.parseLocalDateTime("10/02/2026 14:35");
 * Date date = DateUtils.toDate(ldt);
 * String formatted = DateUtils.format(ldt, "yyyy/MM/dd HH:mm");
 * }</pre>
 *
 * @author Gianluca
 */
@Slf4j
@CompileStatic
class DateUtils {

    /**
     * Returns the current year as a string.
     *
     * @return the current year, e.g., "2026"
     */
    static String getCurrentYear() {
        String year = LocalDate.now().year
        return year
    }

    /**
     * Returns the current month as a 2-digit string.
     *
     * @return the current month, e.g., "02"
     */
    static String getCurrentMonth() {
        String month = LocalDate.now().monthValue
        return month.padLeft(2, '0')
    }

    /**
     * Returns the current day of the month as a 2-digit string.
     *
     * @return the current day, e.g., "10"
     */
    static String getCurrentDay() {
        String day = LocalDate.now().dayOfMonth
        return day.padLeft(2, '0')
    }

    /**
     * Returns the current hour (24-hour format) as a 2-digit string.
     *
     * @return the current hour, e.g., "14"
     */
    static String getCurrentHour() {
        String time = LocalTime.now().hour
        return time.padLeft(2, '0')
    }

    /**
     * Returns the current minute as a 2-digit string.
     *
     * @return the current minute, e.g., "35"
     */
    static String getCurrentMinute() {
        String time = LocalTime.now().minute
        return time.padLeft(2, '0')
    }

    /**
     * Returns the current second as a 2-digit string.
     *
     * @return the current second, e.g., "20"
     */
    static String getCurrentSecond() {
        String time = LocalTime.now().second
        return time.padLeft(2, '0')
    }

    /**
     * Returns a timestamp suitable for filenames using the pattern "yyyy-MM-dd-HH-mm-ss".
     *
     * @return the formatted timestamp, e.g., "2026-02-10-14-35-20"
     */
    static String getFilenameTimestamp() {
        return format(LocalDateTime.now(), 'yyyy-MM-dd-HH-mm-ss')
    }

    // -------------------------
    // CONVERSIONS
    // -------------------------

    /**
     * Converts a {@link java.time.LocalDateTime} to a {@link java.util.Date}.
     *
     * @param localDateTime the local date-time to convert
     * @return the corresponding {@link Date}, or null if input is null
     */
    static Date toDate(LocalDateTime localDateTime) {
        if (!localDateTime) {
            return null
        }

        Instant instant = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        Date datetime = Date.from(instant)
        return datetime
    }

    /**
     * Converts a {@link java.time.LocalDate} to a {@link java.util.Date}.
     *
     * @param localDate the local date to convert
     * @return the corresponding {@link Date}, or null if input is null
     */
    static Date toDate(LocalDate localDate) {
        if (!localDate) {
            return null
        }

        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        Date date = Date.from(instant)
        return date
    }

    /**
     * Converts a {@link java.util.Date} to {@link java.time.LocalDateTime}.
     *
     * @param datetime the date to convert
     * @return the corresponding {@link LocalDateTime}, or null if input is null
     */
    static LocalDateTime toLocalDateTime(Date datetime) {
        if (!datetime) {
            return null
        }

        Instant instant = Instant.ofEpochMilli(datetime.getTime())
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return localDateTime
    }

    /**
     * Converts a {@link java.util.Date} to {@link java.time.LocalDate}.
     *
     * @param date the date to convert
     * @return the corresponding {@link LocalDate}, or null if input is null
     */
    static LocalDate toLocalDate(Date date) {
        if (!date) {
            return null
        }

        Instant instant = Instant.ofEpochMilli(date.getTime())
        LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate()
        return localDate
    }

    /**
     * Converts a {@link java.util.Date} to {@link java.time.LocalTime}.
     *
     * @param date the date to convert
     * @return the corresponding {@link LocalTime}, or null if input is null
     */
    static LocalTime toLocalTime(Date date) {
        if (!date) {
            return null
        }

        Instant instant = Instant.ofEpochMilli(date.getTime())
        LocalTime localTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime()
        return localTime
    }

    // -------------------------
    // FORMATTING
    // -------------------------

    /**
     * Formats a {@link java.time.LocalDate} using the locale-sensitive medium date style.
     *
     * @param date   the local date to format
     * @param locale the locale to use for formatting (default: system default)
     * @return the formatted date string, e.g., {@code "Feb 10, 2026"} for {@code Locale.ENGLISH}
     */
    static String format(LocalDate date, Locale locale = Locale.getDefault()) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(locale)

        return formatWith(date, formatter)
    }

    /**
     * Formats a {@link java.time.LocalDate} using the given pattern.
     *
     * @param date    the local date to format
     * @param pattern the pattern to use, e.g., {@code "yyyy/MM/dd"}
     * @param locale  the locale to use for formatting (default: system default)
     * @return the formatted date string
     */
    static String format(LocalDate date, String pattern, Locale locale = Locale.getDefault()) {
        return formatWith(date, DateTimeFormatter.ofPattern(pattern, locale))
    }

    /**
     * Formats a {@link java.time.LocalDateTime} using the locale-sensitive medium date-time style.
     *
     * @param dateTime the local date-time to format
     * @param locale   the locale to use for formatting (default: system default)
     * @return the formatted date-time string, e.g., {@code "Feb 10, 2026, 2:35:20 PM"} for {@code Locale.ENGLISH}
     */
    static String format(LocalDateTime dateTime, Locale locale = Locale.getDefault()) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(locale)

        return formatWith(dateTime, formatter)
    }

    /**
     * Formats a {@link java.time.LocalDateTime} using the given pattern.
     *
     * @param dateTime the local date-time to format
     * @param pattern  the pattern to use, e.g., {@code "yyyy/MM/dd HH:mm"}
     * @param locale   the locale to use for formatting (default: system default)
     * @return the formatted date-time string
     */
    static String format(LocalDateTime dateTime, String pattern, Locale locale = Locale.getDefault()) {
        return formatWith(dateTime, DateTimeFormatter.ofPattern(pattern, locale))
    }

    /**
     * Formats a {@link java.time.LocalTime} using the locale-sensitive medium time style.
     *
     * @param time   the local time to format
     * @param locale the locale to use for formatting (default: system default)
     * @return the formatted time string, e.g., {@code "2:35:20 PM"} for {@code Locale.ENGLISH}
     */
    static String format(LocalTime time, Locale locale = Locale.getDefault()) {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(locale)

        return formatWith(time, formatter)
    }

    /**
     * Formats a {@link java.time.LocalTime} using the given pattern.
     *
     * @param time    the local time to format
     * @param pattern the pattern to use, e.g., {@code "HH:mm"}
     * @param locale  the locale to use for formatting (default: system default)
     * @return the formatted time string
     */
    static String format(LocalTime time, String pattern, Locale locale = Locale.getDefault()) {
        return formatWith(time, DateTimeFormatter.ofPattern(pattern, locale))
    }

    /**
     * Formats a {@link java.util.Date} using the locale-sensitive medium date-time style.
     *
     * @param date   the date to format
     * @param locale the locale to use for formatting (default: system default)
     * @return the formatted date-time string, e.g., {@code "Feb 10, 2026, 2:35:20 PM"} for {@code Locale.ENGLISH}
     */
    static String format(Date date, Locale locale = Locale.getDefault()) {
        Instant instant = date.toInstant()
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault())

        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(locale)

        return formatWith(zdt, formatter)
    }

    /**
     * Formats a {@link java.util.Date} using the given pattern.
     *
     * @param date    the date to format
     * @param pattern the pattern to use, e.g., {@code "yyyy/MM/dd HH:mm"}
     * @param locale  the locale to use for formatting (default: system default)
     * @return the formatted date-time string
     */
    static String format(Date date, String pattern, Locale locale = Locale.getDefault()) {
        Instant instant = date.toInstant()
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault())

        return formatWith(zdt, DateTimeFormatter.ofPattern(pattern, locale))
    }

    /**
     * Formats a {@link java.time.temporal.TemporalAccessor} using the given {@link java.time.format.DateTimeFormatter}.
     *
     * @param temporal  the temporal value to format
     * @param formatter the formatter to apply
     * @return the formatted string
     */
    private static String formatWith(TemporalAccessor temporal, DateTimeFormatter formatter) {
        return formatter.format(temporal)
    }

    /**
     * Reformats a date string from one pattern to another.
     * <p>
     * The input string is parsed as a {@link java.time.LocalDate} using {@code patternFrom},
     * then formatted back to a string using {@code patternTo}.
     * <p>
     * Example:
     * <pre>{@code
     * String result = DateUtils.reformat("2026-02-10", "yyyy-MM-dd", "dd/MM/yyyy"); // "10/02/2026"
     * }</pre>
     *
     * @param date        the date string to reformat
     * @param patternFrom the pattern of the input string, e.g., {@code "yyyy-MM-dd"}
     * @param patternTo   the pattern for the output string, e.g., {@code "dd/MM/yyyy"}
     * @return the reformatted date string, or an empty string if input is null or blank
     */
    static String reformat(String date, String patternFrom, String patternTo) {
        if (!date) {
            return ''
        }

        LocalDate localDate = parseLocalDate(date, patternFrom)
        return format(localDate, patternTo)
    }

    // -------------------------
    // PARSING
    // -------------------------

    /**
     * Parses a string into {@link java.time.LocalDateTime} using the given pattern.
     *
     * @param dateTime the string to parse
     * @param pattern the format pattern (default: "dd/MM/yyyy HH:mm")
     * @return the parsed {@link LocalDateTime}, or null if input is null
     */
    static LocalDateTime parseLocalDateTime(String dateTime, String pattern = 'dd/MM/yyyy HH:mm') {
        if (!dateTime) {
            return null
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern)
        return LocalDateTime.parse(dateTime, dtf)
    }

    /**
     * Parses a string into {@link java.time.LocalDate} using the given pattern.
     *
     * @param date the string to parse
     * @param pattern the format pattern (default: "dd/MM/yyyy")
     * @return the parsed {@link LocalDate}, or null if input is null
     */
    static LocalDate parseLocalDate(String date, String pattern = 'dd/MM/yyyy') {
        if (!date) {
            return null
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern)
        return LocalDate.parse(date, dtf)
    }

    /**
     * Parses a string into {@link java.time.LocalTime} using the given pattern.
     *
     * @param time the string to parse
     * @param pattern the format pattern (default: "HH:mm")
     * @return the parsed {@link LocalTime}, or null if input is null
     */
    static LocalTime parseLocalTime(String time, String pattern = 'HH:mm') {
        if (!time) {
            return null
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern)
        return LocalTime.parse(time, dtf)
    }

    /**
     * Parses a string into {@link java.util.Date} using the given pattern.
     *
     * @param date the string to parse
     * @param pattern the format pattern (default: "dd/MM/yyyy")
     * @return the parsed {@link Date}, or null if input is null
     */
    static Date parseDate(String date, String pattern = 'dd/MM/yyyy') {
        if (!date) {
            return null
        }

        SimpleDateFormat sdf = new SimpleDateFormat(pattern)
        return sdf.parse(date)
    }
}
