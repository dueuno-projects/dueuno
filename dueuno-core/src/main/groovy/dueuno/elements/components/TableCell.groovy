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

import dueuno.commons.utils.ObjectUtils
import dueuno.core.PrettyPrinterProperties
import dueuno.elements.Component
import dueuno.elements.controls.HiddenField
import dueuno.elements.style.TextAlign
import dueuno.elements.style.TextStyle
import dueuno.elements.style.TextWrap
import dueuno.elements.style.VerticalAlign
import groovy.contracts.Requires
import groovy.transform.CompileStatic

/**
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@CompileStatic
class TableCell extends Component {

    Table table
    String column
    TableRow row

    Integer colspan
    Integer rowspan

    TextAlign textAlign
    VerticalAlign verticalAlign

    String componentId

    @Requires({ args.table && args.row && args.column })
    TableCell(Map args) {
        super(args)

        table = args.table as Table
        row = args.row as TableRow
        column = args.column

        colspan = 0
        rowspan = 0
        textAlign = TextAlign.DEFAULT
        verticalAlign = VerticalAlign.DEFAULT

        componentId = getId() + '-component'

        buildCellComponent(args.component as Component)
    }

    @Override
    String getPropertiesAsJSON(Map properties = [:]) {
        Map thisProperties = [
                column: column,
        ]
        return super.getPropertiesAsJSON(thisProperties + properties)
    }

    private void buildCellComponent(Component component) {
        if (component) {
            setComponent(component)
        } else {
            setLabel()
        }
    }

    private void setLabel() {
        addComponent(
                class: Label,
                id: componentId,
                replace: true,
                textPrefix: controllerName,
                textWrap: TextWrap.NO_WRAP,
                tag: false,
        )
    }

    Label getLabel() {
        Component component = getComponent()
        if (component in Label) {
            return component as Label
        } else {
            return null
        }
    }

    void setComponent(Component component) {
        if (!row.isHeader) {
            table.hasComponents = true
            row.viewTemplate = 'TableRowComponent'
        }

        component.id = componentId
        addComponent(component)
    }

    void setComponent(Map args) {
        if (!row.isHeader) {
            table.hasComponents = true
            row.viewTemplate = 'TableRowComponent'
        }

        if (args.id) {
            componentId = args.id
        } else {
            args.id = componentId
        }
        args.replace = true
        addComponent(args)
    }

    Component getComponent() {
        return getComponent(componentId)
    }

    void setSubmitValue(Object value) {
        addComponent(
                class: HiddenField,
                id: getId() + '-value',
                value: value,
                replace: true,
        )
    }

    Component getSubmitValue() {
        return getComponent(getId() + '-value')
    }

    void setPrettyPrinterProperties(Map value) {
        Label label = getLabel()
        if (label) {
            label.prettyPrinterProperties.set(value)
        }
    }

    PrettyPrinterProperties getPrettyPrinterProperties() {
        Label label = getLabel()
        if (label) {
            return label.prettyPrinterProperties
        } else {
            return new PrettyPrinterProperties()
        }
    }

    void setTextWrap(TextWrap value) {
        Label label = getLabel()
        if (label) {
            label.textWrap = value
        }
    }

    void setTextStyle(TextStyle value) {
        Label label = getLabel()
        if (label) {
            label.setTextStyle(value)
        }
    }

    void setTextStyle(List<TextStyle> value) {
        Label label = getLabel()
        if (label) {
            label.setTextStyle(value)
        }
    }

    void setText(Object value) {
        Label label = getLabel()
        if (label) {
            label.text = value
        }
    }

    void setIcon(String value) {
        Label label = getLabel()
        if (label) {
            label.icon = value
        }
    }

    void setTooltip(String value) {
        Label label = getLabel()
        if (label) {
            label.tooltip = value
        }
    }

    void setTag(Boolean value) {
        Label label = getLabel()
        Object labelValue = ObjectUtils.getValue(row.values, column)

        if (label && (labelValue != null || label.icon)) {
            String backgroundColor = table.rowStriped
                    ? (row.index % 2 == 0 ? mainForegroundColor : mainBackgroundColor)
                    : mainBackgroundColor

            textAlign = TextAlign.CENTER
            label.tag = value
            label.backgroundColor = value
                    ? backgroundColor
                    : null
        }
    }

    void setUrl(String value) {
        Label label = getLabel()
        if (label) {
            label.url = value
        }
    }

    void setHtml(String value) {
        Label label = getLabel()
        if (label) {
            label.html = value
        }
    }

    Boolean isColumnSpanned() {
        Integer span = 0
        table.columns.find { String col ->
            span--
            if (col == column) return true
            TableCell cell = row.cells[col]
            if (cell.colspan > 1) {
                span = cell.colspan
            }
            return false
        }
        return span > 0
    }

}
