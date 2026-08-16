//= require DateTimeField

class DateField extends DateTimeField {

    static get valueType() {
        return Type.DATE;
    }

    static initialize($element, $root) {
        let dateFormat = _21_.user.invertedMonth ? 'MM/dd/yyyy' : 'dd/MM/yyyy';
        let startOfTheWeek = _21_.user.firstDaySunday ? 0 : 1;

        let initOptions = {
            allowInputToggle: false,
            display: {
                theme: 'light',
                viewMode: 'calendar',
                components: {
                    clock: false,
                },
            },
            localization: {
                format: dateFormat,
                startOfTheWeek: startOfTheWeek,
            },
            restrictions: {},
        };

        let td = new tempusDominus.TempusDominus($element.parent()[0], initOptions);
        td.dates.parseInput = DateField.parseInput;
        $element.data('td', td);
    }

    static parseInput(value) {
        let year;
        let month;
        let day;

        value = value.replaceAll('/', '');

        if (value.length <= 2) {
            let today = new Date();
            year = today.getFullYear().toString();
            month = (today.getMonth() + 1).toString().padStart(2, '0');
            day = value.padStart(2, '0');

        } else if (value.length == 4) {
            let today = new Date();
            year = today.getFullYear().toString();
            month = value.substring(2, 4);
            day = value.substring(0, 2);

        } else if (_21_.user.invertedMonth) {
            year = value.substring(4, 8);
            month = value.substring(0, 2);
            day = value.substring(2, 4);

        } else {
            year = value.substring(4, 8);
            month = value.substring(2, 4);
            day = value.substring(0, 2);
        }

        let date = new tempusDominus.DateTime(year + '-' + month + '-' + day);
        let dateUTC = DateTimeField.dateToUTC(date);
        return dateUTC;
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
                day: date.date,
            });
    }

}

Control.register(DateField);
