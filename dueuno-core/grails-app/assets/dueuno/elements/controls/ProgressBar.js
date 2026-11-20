class ProgressBar extends Control {

    static get valueType() {
        return Type.NUMBER;
    }

    static setMin($element, value) {
        Component.setProperty($element, 'min', value);
        $element.attr('aria-valuemin', value);
        ProgressBar.update($element);
    }

    static setMax($element, value) {
        Component.setProperty($element, 'max', value);
        $element.attr('aria-valuemax', value);
        ProgressBar.update($element);
    }

    static setValue($element, valueMap, trigger = true) {
        valueMap = TypedValue.require(valueMap);
        Component.setProperty($element, 'value', valueMap.value);
        ProgressBar.update($element);
    }

    static getValue($element) {
        let valueMap = Control.getServerValue($element);
        valueMap.value = Component.getProperty($element, 'value');
        return TypedValue.require(valueMap);
    }

    static setIndefinite($element, value) {
        let indefinite = value === true || value === 'true';
        Component.setProperty($element, 'indefinite', indefinite);
        $element.removeClass('indefinite definite').addClass(indefinite ? 'indefinite' : 'definite');
        ProgressBar.update($element);
    }

    static setPercentage($element, value) {
        let percentage = value === true || value === 'true';
        Component.setProperty($element, 'percentage', percentage);
        ProgressBar.update($element);
    }

    static togglePercentage($element) {
        let percentage = Component.getProperty($element, 'percentage') !== false;
        ProgressBar.setPercentage($element, !percentage);
    }

    static update($element) {
        let properties = Component.getProperties($element);
        let min = Number(properties.min ?? 0);
        let max = Number(properties.max ?? 100);
        let value = Number(properties.value ?? 0);
        let range = max - min;
        let percentage = range > 0 ? Math.min(100, Math.max(0, (value - min) / range * 100)) : 0;
        let decimals = Number(properties.decimals ?? 0);
        let percentageText = percentage.toFixed(decimals).replace('.', ',');
        let displayValue = properties.percentage === false ? value.toFixed(decimals).replace('.', ',') : percentageText;

        if (properties.indefinite) {
            $element.removeAttr('aria-valuenow');
        } else {
            $element.attr('aria-valuenow', value);
        }

        $element.find('.progress-bar').css('width', percentage + '%');
        $element.find('.progress-label').text(displayValue + (properties.percentage === false ? '' : '%'));
    }

}

Control.register(ProgressBar);
