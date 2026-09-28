//= require NumberField

class MoneyField extends NumberField {

    static get valueType() {
        return null;
    }

    static setValue($element, valueMap, trigger = true) {
        valueMap = TypedValue.require(valueMap);
        let value = valueMap['value']
        if (!value) {
            NumberField.setValue($element, valueMap);
            return;
        }

        let amount = value['amount'];
        NumberField.setValue($element, TypedValue.number(amount));

        if (value['currency']) {
            let $currency = $element.prev();
            $currency.text(value['currency']);
        }
    }

    static getValue($element) {
        let valueMap = Control.getServerValue($element);
        valueMap.value['amount'] = NumberField.getValue($element)['value'];

        if (valueMap.value['amount']) {
            return TypedValue.require(valueMap);
        } else {
            return TypedValue.empty(valueMap.type);
        }
    }

}

Control.register(MoneyField);
