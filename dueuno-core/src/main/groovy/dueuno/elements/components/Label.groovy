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
 * @author Gianluca Sartori
 */

@CompileStatic
class Label extends Component {

    Object text
    String html
    String url

    /** Icon that graphically represents the Link. Choose one from Font Awesome icons */
    String icon
    String iconClass

    /** An SVG image that graphically represents the Link.
     * If specified it must be present in the Grails asset folder.
     */
    String image
    String imageClass

    /**
     * If specified a tooltip will be displayed on mouse hover
     */
    String tooltip

    /**
     * Text alignment & styling
     */
    VerticalAlign verticalAlign
    TextAlign textAlign
    TextWrap textWrap
    List<TextStyle> textStyle

    /**
     * Normally a label will not be selectable (not copy/pastable) unless userSelect = true
     */
    Boolean userSelect

    /**
     * Display the label as it was a tag.
     * Set the backgroundColor to render the boxing background.
     */
    Boolean tag

    PrettyPrinterProperties prettyPrinterProperties

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

    String getText() {
        prettyPrinterProperties.locale = locale
        return prettyPrint(text, prettyPrinterProperties)
    }

    void setTextArgs(List value) {
        prettyPrinterProperties.textArgs = value
    }

    List getTextArgs() {
        return prettyPrinterProperties.textArgs
    }

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

    String getTextStyle() {
        return textStyle.join(' ')
    }

    void setHtml(String value) {
        if (html) {
            textWrap = TextWrap.DEFAULT
        }
        html = value
    }

    String getHtml() {
        prettyPrinterProperties.locale = locale
        return prettyPrint(html, prettyPrinterProperties)
    }

}
