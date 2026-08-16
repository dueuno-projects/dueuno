class DateTimeField extends Control {

    static get valueType() {
        return Type.DATETIME;
    }

    static initialize($element, $root) {
        let properties = Component.getProperties($element);
        let dateFormat = _21_.user.invertedMonth ? 'MM/dd/yyyy' : 'dd/MM/yyyy';
        let timeFormat = _21_.user.twelveHours ? 'hh:mm' : 'HH:mm';
        let hourCycle = _21_.user.twelveHours ? 'h12' : 'h23';
        let startOfTheWeek = _21_.user.firstDaySunday ? 0 : 1;

        let initOptions = {
            stepping: properties.timeStep ?? 1,
            allowInputToggle: false,
            display: {
                theme: 'light',
            },
            localization: {
                format: dateFormat + ' ' + timeFormat,
                hourCycle: hourCycle,
                startOfTheWeek: startOfTheWeek,
            },
            restrictions: {},
        }

        let td = new tempusDominus.TempusDominus($element.parent()[0], initOptions);
        td.dates.parseInput = DateTimeField.parseInput;
        $element.data('td', td);
    }

    static finalize($element, $root) {
        let properties = Component.getProperties($element);
        let initOptions = {
            restrictions: {
                minDate: properties.min ? new Date(properties.min) : undefined,
                maxDate: properties.max ? new Date(properties.max) : undefined,
            }
        };
        let td = $element.data('td');
        td.updateOptions(initOptions);

        $element.off('focus').on('focus', DateTimeField.onFocus);
        $element.off('paste').on('paste', Control.onPaste);
        $element.off('keypress').on('keypress', DateTimeField.onKeyPress);
        $element.off('change.td').on('change.td', DateTimeField.onChange);
        $element.off('show.td').on('show.td', DateTimeField.onShow);
        $element.parent().off('error.td').on('error.td', DateTimeField.onError);

        Transition.triggerEvent($element, 'load');
    }

    static dateToUTC(date) {
        return new tempusDominus.DateTime(
            date.getUTCFullYear(),
            date.getUTCMonth(),
            date.getUTCDate(),
            date.getUTCHours(),
            date.getUTCMinutes(),
            date.getUTCSeconds()
        );
    }

    static parseInput(value) {
        let year;
        let month;
        let day;
        let hour;
        let minute;

        value = value.replaceAll('/', '');
        value = value.replaceAll(' ', '');
        value = value.replaceAll(':', '');

        if (value.length <= 2) {
            let today = new Date();
            year = today.getFullYear().toString();
            month = (today.getMonth() + 1).toString().padStart(2, '0');
            day = value.padStart(2, '0');
            hour = '00';
            minute = '00';

        } else if (value.length == 4) {
            let today = new Date();
            year = today.getFullYear().toString();
            month = value.substring(2, 4);
            day = value.substring(0, 2);
            hour = '00';
            minute = '00';

        } else if (_21_.user.invertedMonth) {
            year = value.substring(4, 8);
            month = value.substring(0, 2);
            day = value.substring(2, 4);
            hour = value.substring(8, 10);
            minute = value.substring(10, 12);

        } else {
            year = value.substring(4, 8);
            month = value.substring(2, 4);
            day = value.substring(0, 2);
            hour = value.substring(8, 10);
            minute = value.substring(10, 12);
        }

        // This hack is here to input the right date/hour in different timezones
        // We should find a better solution, I'm not investigating since this control
        // is mostly used as a readonly view. Usually the time input is decoupled into a TimeField.
        let date = new tempusDominus.DateTime(year + '-' + month + '-' + day);
        let utcDate = DateTimeField.dateToUTC(date);
        let utcDay = utcDate.date.toString().padStart(2, '0');
        let dateTime = new tempusDominus.DateTime(year + '-' + month + '-' + utcDay + 'T' + hour + ':' + minute);
        return dateTime;
    }

    static onFocus(event) {
        let $element = $(event.currentTarget);
        let isReadonly = Component.getReadonly($element);

        if (!isReadonly) {
            $element.select();
        }
    }

    static onError(event) {
        let $element = $(event.currentTarget).find('input');
        DateTimeField.setValue($element, TypedValue.empty(DateTimeField.valueType), false);
    }

    static onChange(event) {
        let $element = $(event.currentTarget);

        if (event.oldDate || event.date) {
            $element.data('td-clear', event.oldDate ? false : true);
            Transition.triggerEvent($element, 'change');
        }
    }

    static onShow(event) {
        let $element = $(event.currentTarget);
        DateTimeField.setError($element, null);

        let autoPopulate = Control.getProperty($element, 'autoPopulate');
        if (!autoPopulate && $element.data('td-clear')) $element.val('');
        $element.data('td-clear', false);
    }

    static onKeyPress(event) {
        if (event.key == 'Enter') {
            event.preventDefault();
            TextField.onEnter(event);
            return;
        }

        let $element = $(event.currentTarget);
        DateTimeField.setError($element, null);

        let properties = Component.getProperties($element);
        let value = Control.getEventValue($element, event);

        let pattern = new RegExp(properties.pattern);
        let isValidValue = value.match(pattern);
        if (!isValidValue) {
            event.preventDefault();
        }

        Transition.triggerEvent($element, 'keypress');
    }

    static setValue($element, valueMap, trigger = true) {
        valueMap = TypedValue.require(valueMap);
        let td = $element.data('td');
        let value = valueMap.value;

        if (!value) {
            if (!trigger) $element.off('change.td');
            td.dates.clear();
            if (!trigger) $element.on('change.td', DateTimeField.onChange);
            return;
        }

        let dateTime = new tempusDominus.DateTime(
            value.year,
            value.month - 1,
            value.day,
            value.hour ?? 0,
            value.minute ?? 0,
            value.second ?? 0
        );

        if (!trigger) $element.off('change.td');
        td.dates.setValue(dateTime);
        if (!trigger) $element.on('change.td', DateTimeField.onChange);
    }

    static getValue($element) {
        let value = $element.val();
        if (!value) {
            return TypedValue.empty(DateTimeField.valueType);
        }

        let td = $element.data('td');
        let date = td.dates.parseInput(value);

        if (!date) {
            return TypedValue.empty(DateTimeField.valueType);
        }

        return TypedValue.of(Type.DATETIME, {
                year: date.year,
                month: date.month + 1,
                day: date.date,
                hour: date.hours,
                minute: date.minutes,
                second: date.seconds,
            });
    }

    static setReadonly($element, value) {
        let td = $element.data('td');
        if (value) {
            td.disable();
        } else {
            td.enable();
        }
    }

    static setVisible($element, value) {
        if (value == null || value == true) {
            $element.closest('.input-group').removeClass('invisible');
        } else {
            $element.closest('.input-group').addClass('invisible');
        }
    }

    static setDisplay($element, value) {
        if (value == null || value == true) {
            $element.closest('.input-group').removeClass('d-none');
        } else {
            $element.closest('.input-group').addClass('d-none');
        }
    }

    static setMin($element, value) {
        let dateTime = new Date(
            value?.year ?? 1900,
            value?.month ? value?.month - 1 : 0,
            value?.day ?? 1,
            value?.hour ?? 0,
            value?.minute ?? 0
        );

        let initOptions = {
            restrictions: {
                minDate: value ? dateTime : undefined,
            }
        };

        let td = $element.data('td');
        td.updateOptions(initOptions);
    }

    static setMax($element, value) {
        let dateTime = new Date(
            value?.year ?? 1900,
            value?.month ? value?.month - 1 : 0,
            value?.day ?? 1,
            value?.hour ?? 0,
            value?.minute ?? 0
        );

        let initOptions = {
            restrictions: {
                maxDate: value ? dateTime : undefined,
            }
        };

        let td = $element.data('td');
        td.updateOptions(initOptions);
    }

    static setError($element, value) {
        let $formField = $element.closest('[data-21-component="FormField"]');
        FormField.setError($formField, value);
    }

}

Control.register(DateTimeField);
