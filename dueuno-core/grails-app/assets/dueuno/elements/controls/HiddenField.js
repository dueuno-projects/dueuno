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
        let valueMap = Control.getServerValue($element);

        switch (valueMap.type) {
            case Type.NA:
            case Type.BOOL:
            case Type.NUMBER:
            case Type.STRING:
                valueMap.value = $element.val();
                break

            default:
                valueMap.value = JSON.parse($element.val());
        }

        return TypedValue.require(valueMap);
    }

}

Control.register(HiddenField);
