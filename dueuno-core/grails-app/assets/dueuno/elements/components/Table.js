//= require TableStickyHeaders

class Table extends Component {

    static initialize($element, $root) {
        let $dataset = $element.find('.component-table-dataset');
        let $table = $element.find('.component-table-dataset table');
        PageContent.addScrollableElement($table, $dataset);
    }

    static finalize($element, $root) {
        Table.initializeStickyHeader($element);

        let $dataset = $element.find('.component-table-dataset');
        $dataset.on('scroll', Table.onScroll);
        let $selectAll = $element.find('.component-table-selection-header input');
        $selectAll.off('click').on('click', Table.onSelectAll);
        let $selectRow = $element.find('.component-table-selection input');
        $selectRow.off('click').on('click', Table.onSelectRow);
    }

    static initializeStickyHeader($element) {
        // Not working on modals
        if (PageModal.isActive) {
            return;
        }

        let stickyHeaderZIndex = Component.getProperty($element, 'stickyHeaderZIndex');
        let stickyHeaderOffset = Component.getProperty($element, 'stickyHeaderOffset');
        if (!stickyHeaderOffset) {
            stickyHeaderOffset = PageStickyBox.offset;
        }

        let $table = $element.find('table');
        $table.stickyTableHeaders('destroy')
        $table.stickyTableHeaders({
            scrollableElement: '.component-table-dataset',
            fixedOffset: stickyHeaderOffset,
            zIndex: stickyHeaderZIndex == 0 ? 1 : stickyHeaderZIndex,
        });
    }

    static onScroll(event) {
        let $element = $(event.currentTarget);
        let scrollLeft = $element[0].scrollLeft;
        PageContent.setScroll($element, scrollLeft);
    }

    static onSelectAll(event) {
        let $element = $(event.currentTarget);
        let $checkboxes = $element.closest('table').find('.component-table-selection input');
        Checkbox.setValue($checkboxes, TypedValue.bool($element.is(':checked')));

        Table.displayGroupFeatures($element);
    }

    static onSelectRow(event) {
        let $element = $(event.currentTarget);
        Table.displayGroupFeatures($element);
    }

    static setValueByIndex($element, value) {
        let id = Component.getId($element);
        let cellId = '.' + id + '-' + value.row + '-' + value.column;
        let $cell = $element.find(cellId + ' span');
        $cell.text(value.value);
    }

    static setValueByKey($element, value) {
        let $cell = $element.find('.component-table-row:has(.component-table-values):has([data-21-id="' + value.keyName + '"][value="' + value.keyValue + '"]) .' + value.column + ' span');
        $cell.text(value.value);
    }

    static setHtmlByKey($element, valueMap) {
        let $cell = $element.find('.component-table-row:has(.component-table-values):has([data-21-id="' + valueMap.keyName + '"][value="' + valueMap.keyValue + '"]) .' + valueMap.column);
        $cell.html(valueMap.value);
        PageTooltips.initialize();
        PageTooltips.finalize();
    }

    static getValues($element) {
        let $rows = $element.find('tbody tr');
        let rows = [];

        let i = 0;
        $rows.each(function () {
            let $row = $(this);
            let $controls = $row.find('[data-21-control][data-21-value]');

            let values = {}
            $controls.each(function () {
                let $control = $(this);
                let control = Control.getByElement($control);
                let name = Control.getId($control);
                let value = Elements.callMethod($control, control, 'getValue');

                let columnName = Table.processColumnName(name);
                values[columnName] = value;
            });

            rows.push(TypedValue.map(values));
            i++;
        });

        return {rows: TypedValue.list(rows)};
    }

    static processColumnName(name) {
        if (name.search('-') < 0) {
            return name
        } else {
            return name.split('-')[2];
        }
    }

    static displayGroupFeatures($element) {
        let $tableSelection = $element.closest('table').find('.component-table-selection');
        let countAll = $tableSelection.find('input').length;
        let countChecked = $tableSelection.find('input:checked').length;

        Table.displayGroupActions($element, countChecked > 0);
        Table.displaySelectAllRows($element, countChecked > 0 && countChecked == countAll);
    }

    static displayGroupActions($element, value) {
        let $groupActions = $element.closest('table').find('.component-table-actions-header [data-21-id="groupActions"]');
        let $actionsLabel = $groupActions.next();

        Component.setDisplay($groupActions, value);
        Component.setDisplay($actionsLabel, !value);
    }

    static displaySelectAllRows($element, value) {
        let $tableSelectAllRows = $element.closest('table').find('.component-table-selection-header input');
        Checkbox.setValue($tableSelectAllRows, TypedValue.bool(value));
    }
}

Component.register(Table);



class TableDataset extends Component {

}

Component.register(TableDataset);
