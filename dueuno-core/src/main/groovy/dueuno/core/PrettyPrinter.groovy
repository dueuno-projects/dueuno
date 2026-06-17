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

import dueuno.commons.utils.DateUtils
import dueuno.elements.Elements
import dueuno.types.CustomType
import dueuno.types.Money
import dueuno.types.Quantity
import grails.util.Holders
import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.validation.ObjectError

import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Central rendering engine that converts typed Java values into locale-aware,
 * human-readable strings.
 * <p>
 * {@code PrettyPrinter} is the single entry point for all value-to-string conversions
 * in the Elements framework. Formatting behaviour is controlled by a
 * {@link PrettyPrinterProperties} instance passed to every {@code print*()} method.
 * </p>
 * <p>
 * Domain classes (or any arbitrary class) can register a Groovy
 * {@link groovy.text.SimpleTemplateEngine} template via {@link #register} that is used
 * to render object instances. The template receives the object bound to the variable
 * {@code it}.
 * </p>
 * <p>
 * Supported value types out of the box:
 * </p>
 * <ul>
 *     <li>{@link Boolean}</li>
 *     <li>{@link Integer}, {@link Long}, {@link Double}, {@link Float}, {@link java.math.BigDecimal}</li>
 *     <li>{@link String}</li>
 *     <li>{@link java.util.List} / {@link java.util.Set}</li>
 *     <li>{@link java.util.Map} / {@link java.util.Map.Entry}</li>
 *     <li>{@link java.util.Date}, {@link java.time.LocalDate}, {@link java.time.LocalTime}, {@link java.time.LocalDateTime}</li>
 *     <li>Any {@link dueuno.types.CustomType} implementation</li>
 * </ul>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@Slf4j
@CompileStatic
class PrettyPrinter {

    private static Map<String, Template> templateRegistry = [:]

    /**
     * Registers a Groovy {@link groovy.text.SimpleTemplateEngine} template for the given class.
     * The canonical class name is used as the template key. The template receives
     * the value bound to the variable {@code it}.
     *
     * @param clazz    the class whose instances should be rendered with this template
     * @param template the Groovy template string (e.g. {@code '${it.firstName} ${it.lastName}'})
     */
    static void register(Class clazz, String template) {
        String templateName = clazz.canonicalName
        register(templateName, template)
    }

    /**
     * Registers a Groovy {@link groovy.text.SimpleTemplateEngine} template under an explicit name.
     *
     * @param templateName the key used to look up the template (usually a canonical class name)
     * @param template     the Groovy template string
     */
    static void register(String templateName, String template) {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()
        Template tpl = templateEngine.createTemplate(template)
        templateRegistry[templateName] = tpl
    }

    /**
     * Returns {@code true} if a rendering template has been registered for the given
     * class or template name.
     *
     * @param nameOrClass a {@link String} template name or a {@link Class}
     * @return {@code true} if a template exists for the given key
     */
    static Boolean isRegistered(Object nameOrClass) {
        return getTemplate(nameOrClass)
    }

    /**
     * Looks up a template by either a {@link String} name or a {@link Class}.
     * Returns {@code null} for any other type.
     */
    private static Template getTemplate(Object nameOrClass) {
        if (nameOrClass instanceof String) {
            return getTemplate(nameOrClass as String)

        } else if (nameOrClass instanceof Class) {
            return getTemplate(nameOrClass as Class)

        } else {
            return null
        }
    }

    /** Resolves the domain class name and delegates to the string-keyed overload. */
    private static Template getTemplate(Class clazz) {
        String className = Elements.getDomainClassName(clazz)
        return getTemplate(className)
    }

    /** Returns the compiled template for the given name, or {@code null} if none is registered. */
    private static Template getTemplate(String prettyPrinterName) {
        return templateRegistry[prettyPrinterName]
    }

    /**
     * Renders {@code object} as a human-readable string using the supplied formatting properties.
     * <p>
     * If no locale is set on {@code properties}, the current thread's locale from
     * {@link org.springframework.context.i18n.LocaleContextHolder} is used.
     * If a {@link PrettyPrinterProperties#transformer} is configured, the object is
     * transformed before rendering.
     * </p>
     * <p>
     * Dispatch order: {@code null}/empty → {@code List}/{@code Set} → {@code Map} →
     * {@code Map.Entry} → numeric types → {@code Boolean} → date/time types →
     * {@link dueuno.types.CustomType} → fallback {@link #printObject}.
     * </p>
     *
     * @param object     the value to render; may be {@code null}
     * @param properties formatting options; a default instance is used if not supplied
     * @return the formatted string, or an empty string if {@code object} is {@code null} or blank
     */
    static String print(Object object, PrettyPrinterProperties properties = new PrettyPrinterProperties()) {
        if (!properties.locale) {
            properties.locale = LocaleContextHolder.locale
        }

        Object value = transform(object, properties)

        switch (value) {
            case null:
            case '':
                return ''

            case Set:
            case List:
                return printList(value as List, properties)

            case Map:
                return printMap(value as Map, properties)

            case Map.Entry:
                return printMapEntry(value as Map.Entry, properties)

            case Integer:
            case Long:
                return printInteger(value as BigDecimal, properties)

            case Double:
            case Float:
                return printDecimal(value as BigDecimal, properties)

            case BigDecimal:
                return printDecimal(value as BigDecimal, properties)

            case Boolean:
                return printBoolean(value as Boolean, properties)

            case Date:
                return printDate(value as Date, properties)

            case LocalDate:
                return printLocalDate(value as LocalDate, properties)

            case LocalTime:
                return printLocalTime(value as LocalTime, properties)

            case LocalDateTime:
                return printLocalDateTime(value as LocalDateTime, properties)

            case CustomType:
                return (value as CustomType).prettyPrint(properties)

            default:
                return printObject(value, properties)
        }
    }

    /**
     * Applies the configured transformer (if any) to {@code object} before rendering.
     * If the transformer produces a value of a different runtime type, the
     * {@link PrettyPrinterProperties#prettyPrinter} template is cleared because
     * type-specific templates would no longer match.
     */
    private static Object transform(Object object, PrettyPrinterProperties properties) {
        if (object == null) {
            return null
        }

        String transformer = properties.transformer
        if (transformer == null) {
            return object
        }

        Object result = Transformer.transform(transformer, object)
        // Disabling pretty printer if the transformed type is different
        // from the object type since it wont work
        if (result !in object.class) {
            properties.prettyPrinter = null
        }

        return result
    }

    //
    // PRETTY PRINTERS
    //

    /**
     * Renders an arbitrary object using a registered Groovy template if one is available.
     * If no template is found, the object is rendered by calling {@link #printString} on its
     * string representation.
     * <p>Template lookup order:</p>
     * <ol>
     *     <li>{@link PrettyPrinterProperties#prettyPrinter} (explicit override)</li>
     *     <li>The canonical class name of {@code value}</li>
     * </ol>
     *
     * @param value      the object to render
     * @param properties formatting options; a default instance is used if not supplied
     * @return the rendered string
     */
    static String printObject(Object value, PrettyPrinterProperties properties = new PrettyPrinterProperties()) {
        Object prettyPrinter = properties.prettyPrinter

        Template template
        if (prettyPrinter) {
            template = getTemplate(prettyPrinter)

        } else {
            Class clazz = value.getClass()
            String className = Elements.getDomainClassName(clazz)
            template = getTemplate(className)
        }

        if (template) {
            Map obj = [it: value]
            String result
            try {
                result = template.make(obj).toString()
            } catch (Exception e) {
                result = e.message
            }
            return printString(result, properties)

        } else {
            return printString(value as String, properties)
        }
    }

    /**
     * Renders a string value, optionally resolving it as an i18n message key.
     * <p>
     * When {@link PrettyPrinterProperties#renderTextPrefix} is {@code true} (the default),
     * the optional {@link PrettyPrinterProperties#textPrefix} is prepended before the i18n
     * lookup. If the key is not found in the message source, the raw code is returned as-is.
     * </p>
     *
     * @param value      the string or i18n message code to render
     * @param properties formatting options
     * @return the localised string, or the original code if no message is found
     */
    static String printString(String value, PrettyPrinterProperties properties) {
        Locale locale = properties.locale
        Boolean renderTextPrefix = properties.renderTextPrefix == null ? true : properties.renderTextPrefix
        String prefix = properties.textPrefix ? properties.textPrefix + '.' : ''
        String code = renderTextPrefix ? prefix + value : value
        List args = properties.textArgs ?: []

        String localizedValue = message(locale, code, args, 'X')
        if (localizedValue == 'X') {
            // X = Not found in i18n messages
            return code
        } else {
            return localizedValue
        }
    }

    /**
     * Renders a {@link Boolean} value. Delegates to {@link #printObject} so that a
     * registered template or i18n key can customise the output (e.g. "Yes"/"No").
     *
     * @param value      the boolean to render
     * @param properties formatting options
     * @return the rendered string
     */
    static String printBoolean(Boolean value, PrettyPrinterProperties properties) {
        return printObject(value as String, properties)
    }

    /**
     * Renders an integer numeric value (zero decimal places) by forcing
     * {@link PrettyPrinterProperties#decimals} to {@code 0} and delegating to
     * {@link #printDecimal}.
     *
     * @param value      the integer value as a {@link java.math.BigDecimal}
     * @param properties formatting options
     * @return the formatted integer string
     */
    static String printInteger(BigDecimal value, PrettyPrinterProperties properties) {
        properties.decimals = 0
        return printDecimal(value, properties)
    }

    /**
     * Renders a decimal numeric value according to the decimal format and scale specified
     * in {@code properties}. Defaults to 2 decimal places and {@link PrettyPrinterDecimalFormat#ISO_COM}.
     * If the value is zero and {@link PrettyPrinterProperties#renderZero} is set, that token
     * is returned instead.
     *
     * @param value      the decimal to render
     * @param properties formatting options
     * @return the formatted decimal string
     */
    static String printDecimal(BigDecimal value, PrettyPrinterProperties properties) {
        if (value == 0 && properties.renderZero) {
            return properties.renderZero
        }

        Integer decimals = properties.decimals == null ? 2 : properties.decimals as Integer
        PrettyPrinterDecimalFormat decimalFormat = properties.decimalFormat as PrettyPrinterDecimalFormat ?: PrettyPrinterDecimalFormat.ISO_COM
        DecimalFormatSymbols forceSymbols = new DecimalFormatSymbols()

        String s
        switch (decimalFormat) {
            case PrettyPrinterDecimalFormat.ISO_COM:
                s = decimals > 0 ? '###0.' + ('0' * decimals) : '###0'
                forceSymbols.setDecimalSeparator(',' as char)
//                forceSymbols.setGroupingSeparator(' ' as char)
                break

            case PrettyPrinterDecimalFormat.ISO_DOT:
                s = decimals > 0 ? '###0.' + ('0' * decimals) : '###0'
                forceSymbols.setDecimalSeparator('.' as char)
//                forceSymbols.setGroupingSeparator(' ' as char)
                break
        }

        java.text.DecimalFormat df = new java.text.DecimalFormat(s, forceSymbols)
        String formattedValue = df.format(value)

        return printObject(formattedValue, properties)
    }

    /**
     * Renders each element of a list with {@link #printObject} and joins the results
     * using {@link PrettyPrinterProperties#renderDelimiter} (defaults to {@code ", "}).
     *
     * @param value      the list to render
     * @param properties formatting options
     * @return the joined string
     */
    static String printList(List value, PrettyPrinterProperties properties) {
        if (properties.renderDelimiter == null) properties.renderDelimiter = ', '

        List results = []
        for (item in value) {
            results << printObject(item, properties)
        }

        return results.join(properties.renderDelimiter as String)
    }

    /**
     * Renders a map value. If a {@link PrettyPrinterProperties#prettyPrinter} template is set,
     * the entire map is passed to {@link #printObject} as a single object. Otherwise each
     * map entry's value is rendered individually and joined with
     * {@link PrettyPrinterProperties#renderDelimiter} (defaults to {@code ", "}).
     *
     * @param value      the map to render
     * @param properties formatting options
     * @return the rendered string
     */
    static String printMap(Map value, PrettyPrinterProperties properties) {
        if (properties.renderDelimiter == null) properties.renderDelimiter = ', '

        // If a prettyPrinter is specified we consider the whole Map as an Object...
        if (properties.prettyPrinter) {
            return printObject(value, properties)
        }

        // ...otherwise we render each element of the map
        List results = []
        for (item in value) {
            results << printObject(item.value, properties)
        }

        return results.join(properties.renderDelimiter as String)
    }

    /**
     * Renders a single {@link java.util.Map.Entry} by passing its value to {@link #printObject}.
     *
     * @param value      the map entry to render
     * @param properties formatting options
     * @return the rendered string for the entry's value
     */
    static String printMapEntry(Map.Entry value, PrettyPrinterProperties properties) {
        return printObject(value.value, properties)
    }

    /**
     * Converts a legacy {@link java.util.Date} to {@link java.time.LocalDate} and
     * delegates to {@link #printLocalDate}.
     *
     * @param value      the date to render
     * @param properties formatting options
     * @return the formatted date string
     */
    static String printDate(Date value, PrettyPrinterProperties properties) {
        LocalDate localDate = DateUtils.toLocalDate(value)
        return printLocalDate(localDate, properties)
    }

    /**
     * Renders a {@link java.time.LocalDate}. When {@link PrettyPrinterProperties#renderDatePattern}
     * is set, that pattern is applied directly. Otherwise the date is formatted as
     * {@code dd/MM/yyyy} or {@code MM/dd/yyyy} depending on
     * {@link PrettyPrinterProperties#invertedMonth}.
     *
     * @param value      the date to render
     * @param properties formatting options
     * @return the formatted date string
     */
    static String printLocalDate(LocalDate value, PrettyPrinterProperties properties) {
        if (properties.renderDatePattern) {
            return value.format(DateTimeFormatter.ofPattern(properties.renderDatePattern as String))

        } else {
            Boolean invertedMonth = properties.invertedMonth == null ? false : properties.invertedMonth
            String datePattern = invertedMonth ? 'MM/dd/yyyy' : 'dd/MM/yyyy'
            DateTimeFormatter df = DateTimeFormatter.ofPattern(datePattern)

            String formattedValue = df.format(value)
            return printObject(formattedValue, properties)
        }
    }

    /**
     * Renders a {@link java.time.LocalTime}. When {@link PrettyPrinterProperties#renderDatePattern}
     * is set, that pattern is applied directly. Otherwise the time is formatted in 24-hour or
     * 12-hour (AM/PM) notation based on {@link PrettyPrinterProperties#twelveHours}, with optional
     * seconds controlled by {@link PrettyPrinterProperties#renderSeconds}.
     *
     * @param value      the time to render
     * @param properties formatting options
     * @return the formatted time string
     */
    static String printLocalTime(LocalTime value, PrettyPrinterProperties properties) {
        if (properties.renderDatePattern) {
            return value.format(DateTimeFormatter.ofPattern(properties.renderDatePattern as String))

        } else {
            Boolean twelveHours = properties.twelveHours == null ? false : properties.twelveHours
            Boolean renderSeconds = properties.renderSeconds == null ? false : properties.renderSeconds

            String seconds = renderSeconds ? ':ss' : ''
            String timePattern = twelveHours ? "h:mm${seconds} a" : "HH:mm${seconds}"
            DateTimeFormatter df = DateTimeFormatter.ofPattern(timePattern)

            String formattedValue = df.format(value)
            return printObject(formattedValue, properties)
        }
    }

    /**
     * Renders a {@link java.time.LocalDateTime}. When {@link PrettyPrinterProperties#renderDatePattern}
     * is set, that pattern is applied directly. Otherwise the date and/or time portions are rendered
     * separately via {@link #printLocalDate} and {@link #printLocalTime} (both enabled by default)
     * and joined with a space.
     *
     * @param value      the date/time to render
     * @param properties formatting options
     * @return the formatted date/time string, or just the date or time portion if the other is disabled
     */
    static String printLocalDateTime(LocalDateTime value, PrettyPrinterProperties properties) {
        if (properties.renderDatePattern) {
            return value.format(DateTimeFormatter.ofPattern(properties.renderDatePattern as String))

        } else {
            if (properties.renderDate == null) properties.renderDate = true
            if (properties.renderTime == null) properties.renderTime = true

            String date
            String time

            if (properties.renderDate) {
                LocalDate dateValue = value.toLocalDate()
                date = printLocalDate(dateValue, properties)
            }

            if (properties.renderTime) {
                LocalTime timeValue = value.toLocalTime()
                time = printLocalTime(timeValue, properties)
            }

            if (date && time) {
                return date + ' ' + time

            } else if (date) {
                return date

            } else if (time) {
                return time
            }
        }
    }

    /**
     * Resolves an i18n message by code, using the Spring {@link org.springframework.context.ApplicationContext}.
     *
     * @param locale         the locale to use for the lookup
     * @param code           the message key
     * @param args           optional interpolation arguments
     * @param defaultMessage the value returned when the code is not found; defaults to the code itself
     * @return the resolved message, or {@code defaultMessage} if not found
     */
    static String message(Locale locale, String code, List args = [], String defaultMessage = code) {
        return Holders.applicationContext.getMessage(code, args as Object[], defaultMessage, locale)
    }

    /**
     * Resolves an i18n message by code, returning an empty string when the code is not found.
     *
     * @param locale the locale to use for the lookup
     * @param code   the message key
     * @param args   optional interpolation arguments
     * @return the resolved message, or an empty string if not found
     */
    static String messageOrBlank(Locale locale, String code, List args = []) {
        return message(locale, code, args,'')
    }

    /**
     * Resolves the i18n message for a Spring {@link org.springframework.validation.ObjectError},
     * using its {@link org.springframework.validation.ObjectError#code} and
     * {@link org.springframework.validation.ObjectError#arguments}.
     *
     * @param locale the locale to use for the lookup
     * @param error  the validation error to resolve
     * @return the resolved error message
     */
    static String message(Locale locale, ObjectError error) {
        return message(
                locale,
                error.code,
                error.arguments as List,
                error.code
        )
    }

    /**
     * Formats a request-parameter map as a multi-line debug string.
     * Each entry is rendered on its own line as:
     * {@code (SimpleClassName) paramName = paramValue}
     * <p>
     * The {@code controller} and {@code action} keys are excluded from the output.
     * {@link dueuno.types.Money} and {@link dueuno.types.Quantity} values receive
     * a compact {@code amount unit/currency} representation.
     * </p>
     *
     * @param params the request parameter map to format
     * @return a multi-line string representation of the parameters, for debugging purposes
     */
    static String printParams(Map params) {
        String result = ''
        for (param in params) {
            String paramName = param.key
            Object paramValue = param.value

            String displayValue
            switch (paramValue) {
                case String:
                    displayValue = "'$paramValue'"
                    break

                case Money:
                    Money m = paramValue as Money
                    displayValue = "${m.amount} ${m.currency}"
                    break

                case Quantity:
                    Quantity q = paramValue as Quantity
                    displayValue = "${q.amount} ${q.unit}"
                    break

                default:
                    displayValue = paramValue
                    break
            }

            if (paramName != 'controller' && paramName != 'action') {
                result = result + '(' + paramValue?.getClass()?.getSimpleName() + ') ' + paramName + ' = ' + displayValue + '\n'
            }
        }
        return result
    }

}
