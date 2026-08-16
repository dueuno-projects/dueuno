class Select extends Control {

    static getValueType($element) {
        let properties = Component.getProperties($element);
        return properties.multiple ? Type.LIST : Type.STRING;
    }

    static initialize($element, $root) {
        let controlId = Component.getId($element);
        let properties = Component.getProperties($element);
        let hasButtons = $element.parent().has('a, .component-help').exists();

        let initOptions = {
            theme: 'bootstrap-5',
            dropdownParent: $root,
            language: _21_.user.language,
            multiple: properties['multiple'],
            placeholder: properties['multiple'] ? null : properties['placeholder'],
            minimumResultsForSearch: properties['search'] ? 0 : -1,
            allowClear: properties['multiple'] ? false : properties['allowClear'],
            dropdownAutoWidth : true,
            width: hasButtons ? 'auto' : '100%',
            escapeMarkup: function(markup) { return markup; },
            language: {
                inputTooShort: function (args) {
                    var remainingChars = args.minimum - args.input.length;
                    var message = properties.text['inputTooShort'].replace('{0}', remainingChars);
                    return message;
                },
                errorLoading: function() {
                    return properties.text['errorLoading'];
                },
                noResults: function() {
                    return properties.text['noResults'];;
                },
                searching: function() {
                    return properties.text['searching'];;
                },
            },
        };

        let searchEvent = Component.getEvent($element, 'search');
        if (searchEvent) {
            initOptions.minimumInputLength = properties['searchMinInputLength'];
            initOptions.ajax = {
                url: Transition.buildUrl(searchEvent),
                data: function (params) {
                    searchEvent.params = {
                        [controlId]: params.term ? params.term.replaceAll('%', '*') : '',
                    };
                    let submitEvent = Transition.build21Params(searchEvent);
                    return submitEvent;
                },
                processResults: function (data) {
                    let transition = Transition.fromHtml(data);
                    let optionsCommand = transition.commands.findLast(it => it.component == controlId && it.property == 'options');
                    if (optionsCommand) {
                        let options = optionsCommand.value.value ?? {};
                        return {results: options};
                    }
                }
            }
        }

        $element.select2(initOptions);
    }

    static finalize($element, $root) {
        $element.off('select2:select select2:unselect').on('select2:select select2:unselect', Select.onChange);

        // We need this to auto-focus the text input
        $element.off('select2:open').on('select2:open', Select.onOpen);

        // We need this to avoid displaying the title attribute as tooltip
        // This is a hack since there is no way to configure a different behaviour on Select2
        let $selection = $element.next().find('.select2-selection__rendered');
        $selection.off('mouseenter').on('mouseenter', Select.onMouseEnter);

        Transition.triggerEvent($element, 'load');
    }

    static deactivate($element) {
        Component.setDisplay($element, false);
    }

    static isInitialized($element) {
        return false;
    }

    static onMouseEnter(event) {
        let $element = $(event.currentTarget);
        $element.removeAttr('title');
        $element.closest('.input-group').find('[title]').each(function (key, item) {
            $(item).removeAttr('title');
        });
    }

    static onOpen(event) {
        let selectId = event.currentTarget.id;
        let $select = $(".select2-search__field[aria-controls='select2-" + selectId + "-results']");
        $select.each(function (key, element){
            element.focus();
        })
    }

    static onChange(event) {
        let $element = $(event.currentTarget);
        Transition.triggerEvent($element, 'change');
    }

    static setValue($element, valueMap, trigger = true) {
        valueMap = TypedValue.require(valueMap);
        if (!trigger) $element.off('select2:select select2:unselect');

        let searchEvent = Component.getEvent($element, 'search');
        let loadEvent = Component.getEvent($element, 'load');
        let hasOptions = Select.hasOptions($element);
        if (searchEvent && loadEvent && !hasOptions) {
            Select.setTemporaryOptions($element, valueMap);
            if (trigger) {
                Transition.submit(loadEvent);
            }
        }

        $element.val(valueMap.value);
        $element.trigger('change');

        if (!trigger) $element.on('select2:select select2:unselect', Select.onChange);
    }

    static getValue($element) {
        let properties = Component.getProperties($element);
        let value = $element.val();

        if (value == null || (Array.isArray(value) && value.length == 0)) {
            return TypedValue.empty(Select.getValueType($element));

        } else if (!properties['multiple']) {
            return TypedValue.string(value);

        } else {
            return TypedValue.list(Array.isArray(value) ? value : [value]);
        }
    }

    static hasOptions($element) {
        return $element.children('option').length;
    }

    static setOptions($element, options) {
        let valueMap = TypedValue.require(Select.getValue($element));

        $element.empty();
        if (!options || !options.length) {
            valueMap = TypedValue.empty(Select.getValueType($element));
            Select.setValue($element, valueMap, false);
            return;
        }

        let selectedValues = Select.valueList(valueMap.value);
        let isValueInOptions = false;
        for (let option of options) {
            let isSelected = selectedValues.includes(String(option.id));
            $element.append(new Option(option.text, option.id, isSelected, isSelected));
            if (isSelected) {
                isValueInOptions = true
            }
        }

        if (isValueInOptions) {
            let properties = Component.getProperties($element);
            let optionsCount = $element.children('option').length;
            if (!properties['autoSelect'] || optionsCount > 1 || properties['nullable']) {
                // Select2 automatically selects the first item on ajax loading
                // so we need to implement an inverse logic
                Select.setValue($element, valueMap, false);
            }
        } else {
            valueMap = TypedValue.empty(Select.getValueType($element));
            Select.setValue($element, valueMap, false);
        }
    }

    static setTemporaryOptions($element, valueMap) {
        $element.empty();

        for (let value of Select.valueList(valueMap.value)) {
            $element.append(new Option('...', value, true, true));
        }
    }

    static valueList(value) {
        if (value == null) {
            return [];
        }

        let values = Array.isArray(value) ? value : [value];
        return values.map(value => String(value));
    }

    static getReadonly($element) {
        return $element.prop('disabled');
    }

    static setReadonly($element, value) {
        Component.setReadonly($element, value);
        $element.prop('disabled', value);

        let $actions = $element.closest('.input-group').find('a');
        Component.setReadonly($actions, value);
    }

}

Control.register(Select);
