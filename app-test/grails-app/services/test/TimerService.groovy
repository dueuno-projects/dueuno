package test

import dueuno.elements.components.*
import dueuno.elements.core.Component
import dueuno.elements.style.TextAlign
import dueuno.elements.style.TextStyle
import groovy.util.logging.Slf4j

@Slf4j
class TimerService {

    List<Map> list(Integer total) {

        def result = []
        for (Integer i = 1; i <= total; i++) {
            result.add([
                    id: i,
                    name: "nome${i.toString()}",
                    description: "desc${i.toString()}",
                    quantity: (i * 2 - 1),
            ])
        }
        return result
    }

    Integer count(Integer total) {
        return total
    }

    Map get(Integer id) {
        return list(id).last()
    }

    Table createTable(Integer total) {
        Table table = Component.createInstance(Table, 'table')

        table.with {
            columns = [
                    'name',
                    'description',
                    'quantity',
            ]
            actions.removeAllActions()
            actions.addAction(action: 'minus', text: '', icon: 'fa-check')
            //actions.addTailAction(action: 'plus', text: '', icon: 'fa-plus')
        }

        table.body = list(total)

        return table
    }

    Form createHeadForm(Integer id) {
        Form form = Component.createInstance(Form, 'formHead')

        Map item = get(id)

        form.with {
            addField(
                    class: Label,
                    id: "labelHeadId${item.id}",
                    displayLabel: false,
                    text: item.name,
                    textStyle: TextStyle.BOLD,
            )
            addField(
                    class: Label,
                    id: "labelHeadDescId${item.id}",
                    displayLabel: false,
                    text: item.description,
            )
        }

        return form
    }

    Grid createGrid(Integer total) {
        Grid grid = Component.createInstance(Grid, 'grid1')

        for (Map item in list(total)) {
            grid.addColumn(sm: 2).with {
                addComponent(Form, "formId${item.id}").with {
                    cssClass = 'ws2 formitem'
                    addField(
                            class: Label,
                            id: "labelId${item.id}",
                            displayLabel: false,
                            text: item.name,
                            cssClass: 'lblname',
                            cols: 9,
                    )
                    addField(
                            class: Label,
                            id: "labelNumId${item.id}",
                            displayLabel: false,
                            text: item.id,
                            cssClass: 'lblname',
                            textAlign: TextAlign.END,
                            textStyle: TextStyle.BOLD,
                            cols: 3,
                    )
                    addField(
                            class: Label,
                            id: "labelDescId${item.id}",
                            displayLabel: false,
                            text: item.description,
                    )
                    addField(
                            class: Button,
                            id: "btn1Id${item.id}",
                            action: 'detail',
                            displayLabel: false,
                            text: 'Mostra',
                            icon: 'fa-eye',
                            params: [id: item.id],
                    )
                    /*addField(
                            class: Button,
                            id: "btn2Id${item.id}",
                            action: 'confirm1',
                            displayLabel: false,
                            text: 'Stampa',
                            icon: 'fa-print',
                            params: [id: item.id],
                            cols: 6,
                    )
                    addField(
                            class: Button,
                            id: "btn3Id${item.id}",
                            action: 'confirm2',
                            displayLabel: false,
                            text: 'Evadi',
                            icon: 'fa-circle-check',
                            params: [id: item.id],
                            cols: 6,
                    )*/
                }
            }
        }

        return grid
    }
}
