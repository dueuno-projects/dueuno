class Checkbox extends Control {

    static get valueType() {
        return Type.BOOL;
    }

    static finalize($element, $root) {
        $element
            .off('change.bootstrapSwitch')
            .on('change.bootstrapSwitch', Checkbox.onChange);

        $element.closest('.control-checkbox, .control-checkbox-simple')
            .off('click.checkbox')
            .on('click.checkbox', {element: $element[0]}, Checkbox.onClick);

        Transition.triggerEvent($element, 'load');
    }

    static onClick(event) {
        let $element = $(event.data.element);
        if ($element.is(':disabled')) return;
        if ($(event.target).closest('input, label, a, button, select, textarea').length) return;

        $element.trigger('focus');
        $element.trigger('click');
    }

    static onChange(event) {
        let $element = $(event.currentTarget);
        Transition.triggerEvent($element, 'change');
    }

    static getValue($element) {
        let valueMap = Control.getServerValue($element);
        valueMap['value'] = $element.prop('checked');
        return TypedValue.require(valueMap);
    }

    static setValue($element, valueMap, trigger = true) {
        valueMap = TypedValue.require(valueMap);
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
