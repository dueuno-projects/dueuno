//= require Component

class Control extends Component {

    static register(clazz) {
        let clazzName = clazz.name;
        log.trace("REGISTERING CONTROL '" + clazzName + "'");
        Elements.controls.set(
            clazzName,
            clazz
        );
    }

    static onPaste(event) {
        let $element = $(event.currentTarget);
        let properties = Component.getProperties($element);
        let value = event.originalEvent.clipboardData.getData('text/plain');

        if (properties.pattern) {
            let pattern = new RegExp(properties.pattern);
            let isValidValue = value.match(pattern);
            if (!isValidValue) {
                event.preventDefault();
            }
        }

        Transition.triggerEvent($element, 'paste');
    }

    static getByClassName(className) {
        let control = Elements.controls.get(className);
        return control;
    }

    static getByElement($element) {
        let className = Control.getClassName($element);
        return Control.getByClassName(className);
    }

    static getClassName($element) {
        return $element.data('21-control');
    }

    static getServerValue($element) {
        // The server value is immutable client-side and comes only from the DOM attribute.
        let value = JSON.parse($element.attr('data-21-value'));
        return TypedValue.require(value);
    }

    static getValueType($element) {
        let control = Control.getByElement($element);
        return control?.valueType ?? Control.getServerValue($element).type;
    }

    static getValue($element) {
        let className = Elements.getClassName($element);
        log.error('Control "' + className + '" has no "getValue()" method. Please implement one.')
    }

    static setValue($element, valueMap, trigger = true) {
        let className = Elements.getClassName($element);
        log.error('Control "' + className + '" has no "setValue()" method. Please implement one.')
    }

    static getEventValue($element, event) {
        let keyPressed = event.key;
        let oldValue = event.target.value || '';
        let selStart = event.target.selectionStart;

        let value;
        if (selStart) {
            value = oldValue.substr(0, selStart) + keyPressed + oldValue.substr(selStart);
        } else {
            value = oldValue + keyPressed;
        }

        // If selected we clear the current input
        if (oldValue && document.getSelection().toString() == oldValue) {
            value = keyPressed;
        }

        return value;
    }

    static isPrintable(keycode) {
        let printable =
            (keycode > 47 && keycode < 58)   || // number keys
            (keycode == 32)                  || // space bar
            (keycode > 64 && keycode < 91)   || // letter keys
            (keycode > 95 && keycode < 112)  || // numpad keys
            (keycode > 185 && keycode < 193) || // ;=,-./` (in order)
            (keycode > 218 && keycode < 223);   // [\]' (in order)
        return printable;
    }

    static isModifierPressed(event, lastKey = '') {
        return event.shiftKey || event.ctrlKey || event.altKey || lastKey == 'AltGraph';
    }

    static setNullable($element, value) {
        let $formField = $element.closest('[data-21-component="FormField"]');
        if ($formField) FormField.setNullable($formField, value);
    }

    static setReadonly($element, value) {
        Component.setReadonly($element, value);

        let $formField = $element.closest('[data-21-component="FormField"]');
        if ($formField) FormField.setReadonly($formField, value);
    }
}
