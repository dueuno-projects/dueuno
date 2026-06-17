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
package dueuno.elements.components

import dueuno.core.PrettyPrinterProperties
import dueuno.elements.Component
import dueuno.elements.style.TextAlign
import dueuno.elements.style.TextStyle
import dueuno.elements.style.TextWrap
import dueuno.elements.style.VerticalAlign
import groovy.transform.CompileStatic

/**
 * A read-only display component that renders a formatted text value, optional icon or image,
 * and supports rich text styling options.
 * <p>
 * The displayed value is passed as {@code text} and is pretty-printed on read via
 * {@link dueuno.core.PrettyPrinter} using the configured {@link #prettyPrinterProperties}.
 * Alternatively, raw HTML can be provided via {@link #html}. Boolean values are rendered as
 * a check-mark icon; negative numbers are highlighted in red by default.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class Label extends Component {

    /** The raw value to be pretty-printed and displayed. Use {@link #setText(Object)} to set it. */
    Object text

    /** Raw HTML content to render instead of {@link #text}; bypasses pretty-printing. */
    String html

    /** Optional URL associated with the label (e.g. for rendering as a hyperlink). */
    String url

    /** Font Awesome icon class (e.g. {@code "fa-solid fa-check"}) displayed alongside the text. */
    String icon

    /** Additional CSS classes applied to the icon element. */
    String iconClass

    /**
     * SVG image path (relative to the Grails asset folder) displayed instead of or alongside the text.
     * If specified, the asset must exist in the Grails {@code assets} directory.
     */
    String image

    /** Additional CSS classes applied to the image element. */
    String imageClass

    /** Tooltip text shown on mouse hover; {@code null} means no tooltip. */
    String tooltip

    /** Vertical alignment of the label content within its container. */
    VerticalAlign verticalAlign

    /** Horizontal text alignment. */
    TextAlign textAlign

    /** Text-wrapping behaviour. Defaults to {@link TextWrap#SOFT_WRAP}. */
    TextWrap textWrap

    /** One or more text-style modifiers (bold, italic, etc.). */
    List<TextStyle> textStyle

    /**
     * Whether the rendered text is user-selectable (copy/paste enabled).
     * Defaults to {@code false}.
     */
    Boolean userSelect

    /**
     * Whether the label is rendered as a visually boxed tag.
     * Set {@code backgroundColor} to control the tag's background colour.
     * Defaults to {@code true} unless {@link #html} is set.
     */
    Boolean tag

    /** Formatting options used when pretty-printing {@link #text} and {@link #html}. */
    PrettyPrinterProperties prettyPrinterProperties

    /**
     * Creates a {@code Label} instance configured from the supplied argument map.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code text} (the display value),
     *             {@code html} ({@link String}),
     *             {@code url} ({@link String}),
     *             {@code icon} ({@link String}), {@code iconClass} ({@link String}),
     *             {@code image} ({@link String}), {@code imageClass} ({@link String}),
     *             {@code tooltip} ({@link String}),
     *             {@code verticalAlign} ({@link VerticalAlign}),
     *             {@code textAlign} ({@link TextAlign}),
     *             {@code textWrap} ({@link TextWrap}),
     *             {@code textStyle} ({@link TextStyle} or {@link List}),
     *             {@code userSelect} ({@link Boolean}, default {@code false}),
     *             {@code tag} ({@link Boolean}),
     *             {@code renderTextPrefix} ({@link Boolean}, default {@code false}),
     *             {@code renderBoolean} ({@link Boolean}, default {@code true}),
     *             {@code highlightNegative} ({@link Boolean}, default {@code true}),
     *             plus all keys accepted by {@link Component#Component(Map)}
     *             and {@link PrettyPrinterProperties#PrettyPrinterProperties(Map)}
     */
    Label(Map args) {
        super(args)

        skipFocus = true

        html = args.html
        url = args.url
        icon = args.icon
        iconClass = args.iconClass
        image = args.image
        imageClass = args.imageClass
        tooltip = args.tooltip

        verticalAlign = args.verticalAlign == null ? VerticalAlign.DEFAULT : args.verticalAlign as VerticalAlign
        textAlign = args.textAlign == null ? TextAlign.DEFAULT : args.textAlign as TextAlign
        textWrap = args.textWrap == null ? TextWrap.SOFT_WRAP : args.textWrap as TextWrap
        setTextStyle(args.textStyle)

        userSelect = args.userSelect == null ? false : args.userSelect
        tag = args.tag == null ? (html ? false : true) : args.tag

        prettyPrinterProperties = new PrettyPrinterProperties(args)
        prettyPrinterProperties.renderTextPrefix = args.renderTextPrefix == null ? false : args.renderTextPrefix
        prettyPrinterProperties.renderBoolean = args.renderBoolean == null ? true : args.renderBoolean
        prettyPrinterProperties.highlightNegative = args.highlightNegative == null ? true : args.highlightNegative

        containerSpecs.label = args.label ?: ''

        setText(args.text)
    }

    /**
     * Sets the display value of this label with type-specific rendering rules:
     * <ul>
     *   <li>{@code null} — the label is auto-generated from the component {@code id}.</li>
     *   <li>{@link Boolean} — a check-mark icon is shown when {@code true}; text is cleared.</li>
     *   <li>{@link Number} — negative values are highlighted in red when
     *       {@link PrettyPrinterProperties#highlightNegative} is {@code true}.</li>
     *   <li>Any other value — stored as-is for later pretty-printing by {@link #getText()}.</li>
     * </ul>
     *
     * @param value the value to display
     */
    void setText(Object value) {
        switch (value) {
            case null:
                text = buildLabel(id, prettyPrinterProperties)
                break

            case Boolean:
                if (prettyPrinterProperties.renderBoolean) {
                    if (value) icon = 'fa-solid fa-check'
                    text = ''
                }
                break

            case Number:
                if (prettyPrinterProperties.highlightNegative) {
                    if ((value as Number) < 0) textColor = '#aa0000'
                    text = value
                }
                break

            default:
                text = value
        }
    }

    /**
     * Returns the pretty-printed text for this label, resolving i18n keys and applying
     * number/date formatting according to {@link #prettyPrinterProperties}.
     *
     * @return the formatted display string
     */
    String getText() {
        prettyPrinterProperties.locale = locale
        return prettyPrint(text, prettyPrinterProperties)
    }

    /**
     * Sets the interpolation arguments used when resolving the {@link #text} i18n message key.
     *
     * @param value the list of message arguments
     */
    void setTextArgs(List value) {
        prettyPrinterProperties.textArgs = value
    }

    /**
     * Returns the interpolation arguments for the {@link #text} i18n message key.
     *
     * @return the list of message arguments
     */
    List getTextArgs() {
        return prettyPrinterProperties.textArgs
    }

    /**
     * Sets the text style(s) for this label. Accepts a single {@link TextStyle} constant or
     * a {@link List} of constants; any other value resets the style to {@link TextStyle#NONE}.
     *
     * @param value a {@link TextStyle}, a {@link List}{@code <TextStyle>}, or {@code null}
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
                textStyle = [TextStyle.NONE]
        }
    }

    /**
     * Returns the combined Bootstrap CSS class string for all active text styles
     * (e.g. {@code "fw-bold fst-italic"}).
     *
     * @return space-separated CSS class string for the active text styles
     */
    String getTextStyle() {
        return textStyle.join(' ')
    }

    /**
     * Sets the raw HTML content for this label and switches text-wrap to
     * {@link TextWrap#DEFAULT} when HTML is already present.
     *
     * @param value the raw HTML string to render
     */
    void setHtml(String value) {
        if (html) {
            textWrap = TextWrap.DEFAULT
        }
        html = value
    }

    /**
     * Returns the pretty-printed HTML content for this label, applying i18n and formatting
     * according to {@link #prettyPrinterProperties}.
     *
     * @return the formatted HTML string
     */
    String getHtml() {
        prettyPrinterProperties.locale = locale
        return prettyPrint(html, prettyPrinterProperties)
    }

}
