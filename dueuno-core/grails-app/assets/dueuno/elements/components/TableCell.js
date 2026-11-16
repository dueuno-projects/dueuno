class TableCell extends Component {

    static initialize($element, $root) {
        let properties = Component.getProperties($element);
        let $link = $element.find('[data-21-component="Link"]');
        if ($link.exists()) {
            let clickEvent = Component.getEvent($link, 'click');
            let sort = clickEvent.params._21TableSort.value[properties.column].value;
            $link.addClass('sort-' + sort);
        }
    }

}

Component.register(TableCell);