class HiddenField extends Control {

    static setValue($element, valueMap, trigger = false) {
        valueMap = TypedValue.require(valueMap);
        let value;

        switch (valueMap.type) {
            case Type.NA:
            case Type.BOOL:
            case Type.NUMBER:
            case Type.STRING:
                value = valueMap.value;
                break

            default:
                value = JSON.stringify(valueMap.value);
        }

        $element.val(value);
    }

    static getValue($element) {
        let value = Control.getServerValue($element);

        switch (value.type) {
            case Type.NA:
            case Type.BOOL:
            case Type.NUMBER:
            case Type.STRING:
                value.value = $element.val();
                break

            default:
                value.value = JSON.parse($element.val());
        }

        return TypedValue.require(value);
    }

}

Control.register(HiddenField);
