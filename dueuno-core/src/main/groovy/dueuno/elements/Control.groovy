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
package dueuno.elements

import dueuno.core.PrettyPrinterProperties
import dueuno.elements.style.TextStyle
import dueuno.types.Types
import groovy.transform.CompileStatic

/**
 * Base class for all leaf-level UI components that hold a typed value and accept user input.
 * <p>
 * A {@code Control} is a {@link Component} that sits at the leaves of the UI component tree.
 * It encapsulates:
 * </p>
 * <ul>
 *     <li>A typed {@link #value} together with its {@link #valueType} and an optional {@link #defaultValue}.</li>
 *     <li>Input validation via character allow/deny lists ({@link #invalidChars}, {@link #validChars})
 *         and a regex {@link #pattern}.</li>
 *     <li>Rendering options through a {@link PrettyPrinterProperties} instance
 *         ({@link #prettyPrinterProperties}), including a configurable {@link #prettyPrinter}
 *         template and an optional value {@link #transformer}.</li>
 *     <li>Visual text styling via {@link #textStyle}.</li>
 * </ul>
 * <p>
 * Concrete subclasses include controls such as {@code TextField}, {@code DateField}, and
 * {@code Button}.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
abstract class Control extends Component {

    /** The current value held by this control. */
    Object value

    /** The Elements type name of {@link #value} (a {@link dueuno.types.Type} constant or a registered custom type name). */
    String valueType

    /** The value to use when no explicit value has been assigned. */
    Object defaultValue

    /** When {@code true} (default), the control's value may be {@code null}. */
    Boolean nullable

    /**
     * Regex pattern built from a set of forbidden characters. Input is rejected if it matches
     * any character in this set. Takes precedence over {@link #validChars} and {@link #pattern}.
     * Set via {@link #setInvalidChars(String)}.
     */
    String invalidChars

    /**
     * Regex pattern built from a set of allowed characters. Input is rejected if it contains
     * any character outside this set. Takes precedence over {@link #pattern}.
     * Set via {@link #setValidChars(String)}.
     */
    String validChars

    /**
     * Arbitrary regex pattern for input validation. Applied only when neither
     * {@link #invalidChars} nor {@link #validChars} is set.
     */
    String pattern

    /** Formatting options passed to {@link dueuno.core.PrettyPrinter} when rendering {@link #getPrettyValue()}. */
    PrettyPrinterProperties prettyPrinterProperties

    /** One or more {@link TextStyle} values that control the visual appearance of the rendered text. */
    List<TextStyle> textStyle

    /**
     * Creates a new {@code Control} from the given argument map.
     * All {@link Component} arguments are supported, plus:
     * <ul>
     *     <li>{@code valueType} — the type name string</li>
     *     <li>{@code nullable} — defaults to {@code true}</li>
     *     <li>{@code invalidChars} / {@code validChars} / {@code pattern} — input validation</li>
     *     <li>{@code textStyle} — a {@link TextStyle} or list of {@link TextStyle} values</li>
     *     <li>{@code prettyPrinter} / {@code transformer} — rendering customisation</li>
     *     <li>{@code renderTextPrefix} / {@code textPrefix} / {@code textArgs} — i18n prefix options</li>
     *     <li>{@code defaultValue} / {@code value} — initial values</li>
     * </ul>
     *
     * @param args map of control properties
     */
    Control(Map args) {
        super(args)

        viewPath = args.viewPath ?: '/dueuno/elements/controls/'

        valueType = args.valueType
        nullable = args.nullable == null ? true : args.nullable

        setInvalidChars(args.invalidChars as String)
        setValidChars(args.validChars as String)
        setTextStyle(args.textStyle)
        pattern = args.pattern ?: ''

        prettyPrinterProperties = new PrettyPrinterProperties(args)
        prettyPrinterProperties.locale = locale
        prettyPrinterProperties.renderTextPrefix = args.renderTextPrefix == null ? true : args.renderTextPrefix
        prettyPrinterProperties.textPrefix = args.textPrefix ?: controllerName
        prettyPrinterProperties.textArgs = args.textArgs as List
        prettyPrinterProperties.prettyPrinter = args.prettyPrinter
        prettyPrinterProperties.transformer = args.transformer

        defaultValue = args.defaultValue
        value = args.value
    }

    /**
     * Registers a server-side action to be invoked when the user submits this control
     * (e.g. by pressing Enter). If an {@code enter} event is already registered, this
     * method has no effect.
     *
     * @param args the event definition arguments passed to {@link Component#on(Map)}
     * @return this control, for chaining
     */
    Component onSubmit(Map args) {
        String submitEvent = 'enter'
        if (!hasEvent(submitEvent)) {
            args.event = submitEvent
            on(args)
        }

        return this
    }

    /**
     * Sets the name of the value transformer to apply before rendering.
     *
     * @param value the transformer name registered with {@link dueuno.core.Transformer}
     */
    void setTransformer(String value) {
        prettyPrinterProperties.transformer = value
    }

    /**
     * Returns the name of the value transformer currently configured on this control.
     *
     * @return the transformer name, or {@code null} if none is set
     */
    String getTransformer() {
        return prettyPrinterProperties.transformer
    }

    /**
     * Sets the name of the {@link dueuno.core.PrettyPrinter} template used to render
     * this control's value.
     *
     * @param value the template name registered with {@link dueuno.core.PrettyPrinter#register}
     */
    void setPrettyPrinter(String value) {
        prettyPrinterProperties.prettyPrinter = value
    }

    /**
     * Returns the name of the {@link dueuno.core.PrettyPrinter} template currently
     * configured on this control.
     *
     * @return the template name, or {@code null} if none is set
     */
    String getPrettyPrinter() {
        return prettyPrinterProperties.prettyPrinter
    }

    /**
     * Sets the text style(s) for this control. Accepts a single {@link TextStyle},
     * a {@link List} of {@link TextStyle} values, or any other value (defaults to
     * {@link TextStyle#BOLD}).
     *
     * @param value a {@link TextStyle}, a {@code List<TextStyle>}, or {@code null}
     */
    void setTextStyle(Object value) {
        switch (value) {
            case TextStyle:
                textStyle = [value as TextStyle]
                break

            case List<TextStyle>:
                textStyle = value as List<TextStyle>
                break

            default:
                textStyle = [TextStyle.BOLD]
        }
    }

    /**
     * Returns all configured text styles joined into a single space-separated CSS class string
     * suitable for rendering in the view.
     *
     * @return a space-separated string of {@link TextStyle} CSS class names
     */
    String getTextStyle() {
        return textStyle.join(' ')
    }

    /**
     * Returns the control's current value rendered as a human-readable string by
     * {@link dueuno.core.PrettyPrinter}, using this control's {@link #prettyPrinterProperties}.
     *
     * @return the formatted value string
     */
    String getPrettyValue() {
        return prettyPrint(value, prettyPrinterProperties)
    }

    /**
     * Builds a regex pattern that rejects input containing any of the specified characters
     * and stores it in {@link #invalidChars}. Special regex characters are automatically escaped.
     * Passing {@code null} or an empty string clears the pattern.
     *
     * @param chars a string of characters to forbid in the input
     */
    void setInvalidChars(String chars) {
        invalidChars = chars ? '^[^' + escapeSpecialChars(chars) + ']*$' : null
    }

    /**
     * Builds a regex pattern that accepts only input composed of the specified characters
     * and stores it in {@link #validChars}. Special regex characters are automatically escaped.
     * Passing {@code null} or an empty string clears the pattern.
     *
     * @param chars a string of characters to allow in the input
     */
    void setValidChars(String chars) {
        validChars = chars ? '^[' + escapeSpecialChars(chars) + ']*$' : null
    }

    /**
     * Escapes regex special characters within the given character set string so they can be
     * safely embedded inside a character class ({@code [...]}).
     *
     * @param chars the raw character set string to escape
     * @return the escaped string
     */
    private String escapeSpecialChars(String chars) {
        String result = ''

        for (String c in chars) {
            if ('.^$*+-?()[]{}\\|'.contains(c)) {
                result = result + '\\' + c
            } else {
                result += c
            }
        }

        return result
    }

    /**
     * Serialises the control's current value to a JSON string using the Elements
     * typed-value protocol. Returns an empty JSON object ({@code {}}) when no
     * {@link #valueType} has been set.
     *
     * @return a JSON string representing the typed value
     */
    String getValueAsJSON() {
        if (!valueType) {
            return Elements.encodeAsJSON([:])
        }

        Map valueMap = Types.serializeValue(value, valueType)
        return Elements.encodeAsJSON(valueMap)
    }

    /**
     * Returns a JSON string of this control's client-side properties, merging the
     * base {@link Component} properties with {@code nullable} and the active input
     * validation pattern ({@link #invalidChars} takes precedence over {@link #validChars},
     * which takes precedence over {@link #pattern}).
     *
     * @param properties additional properties to include; merged on top of this control's own properties
     * @return a JSON string of all component properties
     */
    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                nullable: nullable,
                pattern: invalidChars ?: validChars ?: pattern,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

}