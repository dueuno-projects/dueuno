//= require NumberField

class QuantityField extends NumberField {

    static get valueType() {
        return null;
    }

    static finalize($element, $root) {
        NumberField.finalize($element, $root);

        let $dropdownItem = $element.closest('.input-group').find('li a');
        if ($dropdownItem.exists()) {
            $dropdownItem.off('click').on('click', QuantityField.onSelectUnit);
        }
     }

     static onSelectUnit(event) {
        event.preventDefault();

        let $selectedUnit = $(this);
        let unitKey = $selectedUnit.data('21-unit');
        let $element = $selectedUnit.closest('.input-group').find('.control-quantity-field');

        QuantityField.setUnit($element, unitKey);
        Component.setFocus($element, true);
     }

    static setValue($element, valueMap, trigger = true) {
        valueMap = TypedValue.require(valueMap);
        let value = valueMap['value'];
        if (!value) {
            NumberField.setValue($element, valueMap);
            return;
        }

        let amount = value['amount'];
        NumberField.setValue($element, TypedValue.number(amount));

        if (value['unit']) {
            QuantityField.setUnit($element, value['unit']);
        }
    }

    static getValue($element) {
        let valueMap = Control.getServerValue($element);
        valueMap.value['unit'] = QuantityField.getUnit($element);
        valueMap.value['amount'] = NumberField.getValue($element)['value'];

        if (valueMap.value['amount']) {
            return TypedValue.require(valueMap);
        } else {
            return TypedValue.empty(valueMap.type);
        }
    }

    static setUnit($element, value) {
        let $unit = $element.closest('.input-group').find('.control-quantity-field-unit-list');
        if (!$unit.length) {
            $unit = $element.closest('.input-group').find('.control-quantity-field-unit');
        }

        // Set data attribute
        $unit.data('21-unit', value);

        // Set label
        let $unitListItem = $element.closest('.input-group').find('li a[data-21-unit="' + value + '"]');
        if ($unitListItem.exists()) {
            let unitLabel = $unitListItem.html();
            $unit.html(unitLabel);
        } else {
            $unit.html(value);
        }
    }

    /**
     * Sets the units available for selection from a list of strings.
     */
    static setAvailableUnits($element, args) {
        let units = args?.units;
        if (!Array.isArray(units) || units.some(unit => typeof unit !== 'string')) {
            throw new TypeError('Available units must be a list of strings.');
        }

        let $group = $element.closest('.input-group');
        let $unitList = $group.find('.control-quantity-field-unit-list');
        let $unit = $group.find('.control-quantity-field-unit');
        let $menu = $group.find('ul.dropdown-menu');
        let currentUnit = QuantityField.getUnit($element);
        let canSelectUnit = units.length > 0 && (!$element.prop('readonly') || $unitList.length);

        if (canSelectUnit) {
            $unit.remove();
            if (!$unitList.length) {
                $unitList = $('<button>', {
                    class: 'control-quantity-field-unit-list btn dropdown-toggle fw-bold',
                    type: 'button',
                    'data-bs-toggle': 'dropdown',
                    'aria-expanded': 'false',
                }).insertBefore($element);
            }
            if (!$menu.length) {
                $menu = $('<ul>', {class: 'dropdown-menu'}).insertBefore($element);
            }

            $menu.empty();
            for (let unit of units) {
                let $link = $('<a>', {class: 'dropdown-item', href: '#'});
                $link.attr('data-21-unit', unit).data('21-unit', unit);
                $link.text(unit);
                $menu.append($('<li>').append($link));
                $link.on('click', QuantityField.onSelectUnit);
            }
        } else {
            $unitList.remove();
            $menu.remove();
            if (!$unit.length) {
                $unit = $('<span>', {
                    class: 'control-quantity-field-unit input-group-text',
                }).insertBefore($element);
            }
        }

        let selectedUnit = units.includes(currentUnit) ? currentUnit : (units[0] ?? currentUnit);
        QuantityField.setUnit($element, selectedUnit);
    }

    static getUnit($element) {
        let $unit = $element.closest('.input-group').find('.control-quantity-field-unit-list');
        if (!$unit.length) {
            $unit = $element.closest('.input-group').find('.control-quantity-field-unit');
        }
        let unitKey = $unit.data('21-unit');
        return unitKey;
    }

    static setReadonly($element, value) {
        Control.setReadonly($element, value);

        // If we can select the unit we need to disable the dropdown button as well
        let $button = $element.siblings('button');
        if (!$button.exists()) {
            return;
        }

        $button.prop('disabled', value);
        if (value) {
            // This is here to set just the attribute with no value
            let button = $button[0];
            button.setAttribute('disabled', '');

        } else {
            $button.removeAttr('disabled');
        }
    }
}

Control.register(QuantityField);
