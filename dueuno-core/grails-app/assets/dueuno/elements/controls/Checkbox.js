class Checkbox extends Control {

    static finalize($element, $root) {
        $element
            .off('change.bootstrapSwitch')
            .on('change.bootstrapSwitch', Checkbox.onChange);

        Transition.triggerEvent($element, 'load');
    }

    static onChange(event) {
        let $element = $(event.currentTarget);
        Transition.triggerEvent($element, 'change');
    }

    static getValue($element) {
        let value = Control.getServerValue($element);
        value['value'] = $element.prop('checked');
        return value;
    }

    static setValue($element, valueMap, trigger = true) {
        $element.val(valueMap.value);
        $element.prop('checked', valueMap.value);
    }

    static setReadonly($element, value) {
        $element.prop('disabled', value);
    }

    static setText($element, value) {
        let $text = $element.closest('.input-group').find('.text-wrapper');
        if ($text.exists()) {
            $text.html(value);
        }
    }

}

Control.register(Checkbox);
