//= require DateTimeField

class MonthField extends DateTimeField {

    static get valueType() {
        return Type.DATE;
    }

    static initialize($element, $root) {
        let dateFormat = 'MM/yyyy';
        let startOfTheWeek = _21_.user.firstDaySunday ? 0 : 1;

        let initOptions = {
            allowInputToggle: false,
            display: {
                theme: 'light',
                viewMode: 'months',
                components: {
                    clock: false,
                    date: false,
                },
            },
            localization: {
                format: dateFormat,
                startOfTheWeek: startOfTheWeek,
            },
            restrictions: {},
        };

        let td = new tempusDominus.TempusDominus($element.parent()[0], initOptions);
        td.dates.parseInput = MonthField.parseInput;
        $element.data('td', td);
    }

    static parseInput(value) {
        let year;
        let month;

        value = value.replaceAll('/', '');

        if (value.length <= 2) {
            let today = new Date();
            year = today.getFullYear().toString();
            month = value.padStart(2, '0');

        } else if (value.length == 5) {
            year = value.substring(1, 5);
            month = value.substring(0, 1).padStart(2, '0');

        } else {
            year = value.substring(2, 6);
            month = value.substring(0, 2);
        }

        let dateTime = new tempusDominus.DateTime(year + '-' + month + '-01');
        let dateTimeUTC = DateTimeField.dateToUTC(dateTime);
        return dateTimeUTC;
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
            1,
            0, 0, 0
        );

        if (!trigger) $element.off('change.td');
        td.dates.setValue(dateTime);
        if (!trigger) $element.on('change.td', DateTimeField.onChange);
    }

    static getValue($element) {
        let value = $element.val();
        if (!value) {
            return TypedValue.empty(Type.DATE);
        }

        let td = $element.data('td');
        let date = td.dates.parseInput(value);

        if (!date) {
            return TypedValue.empty(Type.DATE);
        }

        return TypedValue.of(Type.DATE, {
                year: date.year,
                month: date.month + 1,
                day: 1,
            });
    }

}

Control.register(MonthField);
