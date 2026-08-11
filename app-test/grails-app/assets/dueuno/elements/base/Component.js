//= require Elements

class Component {

    static register(clazz) {
        let clazzName = clazz.name;
        log.trace("REGISTERING COMPONENT '" + clazzName + "'");
        Elements.components.set(
            clazzName,
            clazz
        );
    }

    static setInitialized($element, value) {
        let element = $element[0];
        if (value) {
            element.setAttribute('initialized', '');
        } else {
            element.removeAttribute('initialized');
        }
    }

    static isInitialized($element) {
        let initialized = $element.attr('initialized');
        return initialized != undefined;
    }

    static setDeactivate($element, value) {
        let element = $element[0];
        if (value) {
            element.setAttribute('deactivate', '');
        } else {
            element.removeAttribute('deactivate');
        }
    }

    static getByClassName(className) {
        let component = Elements.components.get(className);
        return component;
    }

    static getByElement($element) {
        let className = Component.getClassName($element);
        return Component.getByClassName(className);
    }

    static getId($element) {
        return $element.data('21-id');
    }

    static getClassName($element) {
        return $element.data('21-component');
    }

    static getProperty($element, propertyName) {
        let properties = Component.getProperties($element);
        let property = properties ? properties[propertyName] : null;
        return property;
    }

    static setProperty($element, propertyName, value) {
        let properties = Component.getProperties($element);
        properties[propertyName] = value;
        Component.setProperties($element, properties);
    }

    static getProperties($element) {
        let values = $element.data('21-properties') ?? {};
        return values;
    }

    static setProperties($element, value) {
        $element.data('21-properties', value ?? {});
    }

    static getEvent($element, eventName) {
        let events = Component.getEvents($element);
        let event = events ? events[eventName] : null;
        if (event) event['event'] = eventName;
        return event;
    }

    static setEvent($element, eventName, componentEvent) {
        let eventsData = Component.getEvents($element);
        eventsData[eventName] = componentEvent;
        Component.setEvents($element, eventsData);
    }

    static getEvents($element) {
        let values = $element.data('21-events') ?? {};
        return values;
    }

    static setEvents($element, eventsData) {
        $element.data('21-events', eventsData);
    }

    static getValues($element) {
        let $controls = $element.find('[data-21-control]');
        let results = {};

        $controls.each(function () {
            let $control = $(this);
            let control = Control.getByElement($control);
            let name = Control.getId($control);
            let value = Elements.callMethod($control, control, 'getValue');

            results[name] = value;
        });

        return results;
    }

    static getVisible($element) {
        return !$element.hasClass('invisible');
    }

    static setVisible($element, value) {
        if (value == null || value == true) {
            $element.removeClass('invisible');
        } else {
            $element.addClass('invisible');
        }
    }

    static getDisplay($element) {
        return $element.closest('.d-none').length == 0;
    }

    static setDisplay($element, value) {
        if (value == null || value == true) {
            $element.removeClass('d-none');
        } else {
            $element.addClass('d-none');
        }
    }

    static getReadonly($element) {
        return $element.prop('disabled') ?? false;
    }

    static setReadonly($element, value) {
        if (value == null || value == false) {
            $element.removeAttr('disabled');
            $element.removeAttr('tabindex');

        } else {
            for (let element of $element) {
                element.setAttribute('disabled', '');
                element.setAttribute('tabindex', '-1');
            }
        }
    }

    static setBackgroundColor($element, value) {
        $element.css('background-color', value);
    }

    static setTextColor($element, value) {
        $element.css('color', value);
    }

    static setFocus($element, value) {
        if (value) {
            $element[0].focus();
        } else {
            document.activeElement.blur();
        }
    }

}
