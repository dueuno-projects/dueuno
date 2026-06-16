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
 * INTERNAL USE ONLY
 * The PrettyPrinter is the main component that renders values on screen:
 *  - Each value can have different properties
 *  - Each value property affects how a certain value is rendered
 *
 *    See: PrettyPrinterProperties
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@Slf4j
@CompileStatic
class PrettyPrinter {

    private static Map<String, Template> templateRegistry = [:]

    static void register(Class clazz, String template) {
        String templateName = clazz.canonicalName
        register(templateName, template)
    }

    static void register(String templateName, String template) {
        SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()
        Template tpl = templateEngine.createTemplate(template)
        templateRegistry[templateName] = tpl
    }

    static Boolean isRegistered(Object nameOrClass) {
        return getTemplate(nameOrClass)
    }

    private static Template getTemplate(Object nameOrClass) {
        if (nameOrClass instanceof String) {
            return getTemplate(nameOrClass as String)

        } else if (nameOrClass instanceof Class) {
            return getTemplate(nameOrClass as Class)

        } else {
            return null
        }
    }

    private static Template getTemplate(Class clazz) {
        String className = Elements.getDomainClassName(clazz)
        return getTemplate(className)
    }

    private static Template getTemplate(String prettyPrinterName) {
        return templateRegistry[prettyPrinterName]
    }

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

    static String printBoolean(Boolean value, PrettyPrinterProperties properties) {
        return printObject(value as String, properties)
    }

    static String printInteger(BigDecimal value, PrettyPrinterProperties properties) {
        properties.decimals = 0
        return printDecimal(value, properties)
    }

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

    static String printList(List value, PrettyPrinterProperties properties) {
        if (properties.renderDelimiter == null) properties.renderDelimiter = ', '

        List results = []
        for (item in value) {
            results << printObject(item, properties)
        }

        return results.join(properties.renderDelimiter as String)
    }

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

    static String printMapEntry(Map.Entry value, PrettyPrinterProperties properties) {
        return printObject(value.value, properties)
    }

    static String printDate(Date value, PrettyPrinterProperties properties) {
        LocalDate localDate = DateUtils.toLocalDate(value)
        return printLocalDate(localDate, properties)
    }

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

    static String message(Locale locale, String code, List args = [], String defaultMessage = code) {
        return Holders.applicationContext.getMessage(code, args as Object[], defaultMessage, locale)
    }

    static String messageOrBlank(Locale locale, String code, List args = []) {
        return message(locale, code, args,'')
    }

    static String message(Locale locale, ObjectError error) {
        return message(
                locale,
                error.code,
                error.arguments as List,
                error.code
        )
    }

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
