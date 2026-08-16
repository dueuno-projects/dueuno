class MultipleCheckbox extends Control {

    static get valueType() {
        return Type.LIST;
    }

    static getValue($element) {
        let items = [];
        let $checkboxes = $element.siblings().find('input[type="checkbox"]');
        $checkboxes.each(function () {
            if ($(this).is(':checked')) {
                let value = Control.getProperty($(this), 'option');
                items.push(value);
            }
        });
        return TypedValue.list(items);
    }

    static setValue($element, valueMap, trigger = true) {
        valueMap = TypedValue.require(valueMap);
        if (!Array.isArray(valueMap.value)) {
            throw new Error('MultipleCheckbox expects a LIST typed value');
        }

        let $checkboxes = $element.siblings().find('input[type="checkbox"]');
        $checkboxes.each(function () {
            let control = Control.getByElement($(this));
            let controlValue = Control.getProperty($(this), 'option');
            if (valueMap['value'].includes(controlValue)) {
                Elements.callMethod($(this), control, 'setValue', TypedValue.bool(true));
            }
        });
    }

}

Control.register(MultipleCheckbox);
