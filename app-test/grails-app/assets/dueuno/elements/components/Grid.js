class Grid extends Component {

    static increment($element, value, $component) {
        let $columns = $element.find('[data-21-component="GridColumn"]');
        let $newColumns = $component.find('[data-21-component="GridColumn"]');
        let $row = $element.find('.row').first();
        let columnsToRemove = [];
        let replace = value.replace == null ? true : value.replace;
        let remove = value.remove == null ? true : value.remove;

        for (let column of $columns) {
            let $column = $(column);
            let id = Elements.getComponentId($column);
            columnsToRemove.push(id);
        }

        for (let newColumn of $newColumns) {
            let $newColumn = $(newColumn);
            let id = Elements.getComponentId($newColumn);

            let $column = Elements.getElementById(id);
            if ($column.length) {
                if (replace) {
                    $column.replaceWith($newColumn);
                }
                columnsToRemove = columnsToRemove.filter((value) => value != id);
            } else {
                $row.append($newColumn);
            }
        }

        for (let id of columnsToRemove) {
            if (remove) {
                Grid.removeColumn($element, {id: id});
            }
        }

        Page.reinitializeContent($element);
    }

    static removeColumn($element, value) {
        if (!value.id) {
            log.error('Grid column id not specified');
            return;
        }

        let $column = $element.find('[data-21-component="GridColumn"][data-21-id="' + value.id + '"]');
        let timeout = value.timeout;

        if (timeout) {
            $column.addClass('deleting');
            setTimeout(() => {
                $column.remove();
            }, timeout);
        }

        if (!$column.hasClass('deleting')) {
            $column.remove();
        }
    }

    static addClass($element, value) {
        if (!value.id) {
            log.error('Grid column id not specified');
            return;
        }

        let $column = $element.find('[data-21-component="GridColumn"][data-21-id="' + value.id + '"]');
        let cssClass = value.cssClass;
        let timeout = value.timeout == null ? 0 : value.timeout;

        if (cssClass) {
            setTimeout(() => {
                $column.addClass(cssClass);
            }, timeout);
        }
    }

    static removeClass($element, value) {
        if (!value.id) {
            log.error('Grid column id not specified');
            return;
        }

        let $column = $element.find('[data-21-component="GridColumn"][data-21-id="' + value.id + '"]');
        let cssClass = value.cssClass;
        let timeout = value.timeout == null ? 0 : value.timeout;

        if (cssClass) {
            setTimeout(() => {
                $column.removeClass(cssClass);
            }, timeout);
        }
    }
}

Component.register(Grid);
