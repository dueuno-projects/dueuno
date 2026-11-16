class Label extends Component {

    static setText($element, value) {
        let $text = $element.find('span');
        $text.html(value);
    }

    static setIcon($element, value) {
        let $icon = $element.find('i');
        $icon.removeClassesStartingWith('fa-');
        $icon.addClass(value);
    }

    static setReadonly($element, value) {
        // no-op
    }

    static setHtml($element, value) {
        $element.html(value);
    }
}

Component.register(Label);
