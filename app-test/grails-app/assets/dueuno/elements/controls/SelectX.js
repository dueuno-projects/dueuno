/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */
class SelectX extends Select {

    static initialize($element, $root) {
        let element = $element[0];
        let controlId = Component.getId($element);
        let properties = Component.getProperties($element);
        let dropboxPortal = SelectX.getDropboxPortal(element);

        let initOptions = {
            ele: element,
            dropboxWrapper: '#' + dropboxPortal.id,
            zIndex: 1060,
            options: SelectX.toVirtualOptions(properties.options),
            multiple: properties.multiple,
            search: properties.search,
            placeholder: properties.multiple ? '' : properties.placeholder,
            hideClearButton: properties.multiple || !properties.allowClear,
            autoSelectFirstOption: false,
            disableSelectAll: true,
            noOptionsText: properties.text.noResults,
            noSearchResultsText: properties.text.noResults,
            additionalClasses: 'w-100',
            silentInitialValueSet: true,
            showDropboxAsPopup: false,
            showDuration: 0,
            hideDuration: 0,
        };

        let searchEvent = Component.getEvent($element, 'search');
        if (searchEvent) {
            initOptions.search = true;
            initOptions.onServerSearch = function (searchValue, virtualSelect) {
                if (searchValue.length < properties.searchMinInputLength) {
                    virtualSelect.setServerOptions([]);
                    return;
                }

                searchEvent.params = {
                    [controlId]: searchValue ? searchValue.replaceAll('%', '*') : '',
                };

                $.ajax({
                    url: Transition.buildUrl(searchEvent),
                    data: Transition.build21Params(searchEvent),
                    success: function (data) {
                        let transition = Transition.fromHtml(data);
                        let command = transition.commands.findLast(it =>
                            it.component == controlId && it.property == 'options'
                        );
                        let options = command?.value?.value ?? [];
                        virtualSelect.setServerOptions(SelectX.toVirtualOptions(options));
                    },
                    error: function () {
                        virtualSelect.setServerOptions([]);
                    },
                });
            };
        }

        VirtualSelect.init(initOptions);
    }

    static getDropboxPortal(element) {
        let modal = element.closest('.modal');
        let portalId = modal ? 'select-x-dropbox-portal-modal' : 'select-x-dropbox-portal';
        let portal = document.getElementById(portalId);
        if (!portal) {
            portal = document.createElement('div');
            portal.id = portalId;
            portal.className = 'control-select-x';
            (modal ?? document.body).appendChild(portal);
        }
        return portal;
    }

    static finalize($element, $root) {
        $element.off('change', SelectX.onChange).on('change', SelectX.onChange);
        Transition.triggerEvent($element, 'load');
    }

    static isInitialized($element) {
        return false;
    }

    static onChange(event) {
        Transition.triggerEvent($(event.currentTarget), 'change');
    }

    static setValue($element, valueMap, trigger = true) {
        valueMap = TypedValue.require(valueMap);
        let element = $element[0];
        if (!element.setValue) return;

        if (!trigger) $element.off('change', SelectX.onChange);

        let searchEvent = Component.getEvent($element, 'search');
        let loadEvent = Component.getEvent($element, 'load');
        if (searchEvent && loadEvent && !SelectX.hasOptions($element)) {
            SelectX.setTemporaryOptions($element, valueMap);
            if (trigger) Transition.submit(loadEvent);
        }

        element.setValue(valueMap.value, !trigger);

        if (!trigger) $element.on('change', SelectX.onChange);
    }

    static getValue($element) {
        let properties = Component.getProperties($element);
        let value = $element[0].value;

        if (value == null || value === '' || (Array.isArray(value) && value.length == 0)) {
            return TypedValue.empty(SelectX.getValueType($element));
        } else if (!properties.multiple) {
            return TypedValue.string(value);
        }
        return TypedValue.list(Array.isArray(value) ? value : [value]);
    }

    static hasOptions($element) {
        return ($element[0].options?.length ?? 0) > 0;
    }

    static setOptions($element, options) {
        let element = $element[0];
        let valueMap = TypedValue.require(SelectX.getValue($element));
        let selectedValues = SelectX.valueList(valueMap.value);
        let newOptions = options ?? [];
        let optionValues = newOptions.map(option => String(option.id));
        let validValues = selectedValues.filter(value => optionValues.includes(value));
        let properties = Component.getProperties($element);

        if (!validValues.length && properties.autoSelect && !properties.nullable && newOptions.length == 1) {
            validValues = [String(newOptions[0].id)];
        }

        element.setOptions(SelectX.toVirtualOptions(newOptions), false);
        let value = properties.multiple ? validValues : (validValues[0] ?? null);
        element.setValue(value, true);
    }

    static setTemporaryOptions($element, valueMap) {
        let options = SelectX.valueList(valueMap.value).map(value => ({value: value, label: '...'}));
        $element[0].setOptions(options, false);
        $element[0].setValue(valueMap.value, true);
    }

    static setReadonly($element, value) {
        Component.setReadonly($element, value);
        if (value) {
            $element[0].disable();
        } else {
            $element[0].enable();
        }

        let $actions = $element.closest('.input-group').find('a');
        Component.setReadonly($actions, value);
    }

    static toVirtualOptions(options) {
        return (options ?? []).map(option => ({
            value: String(option.id),
            label: option.text,
        }));
    }
}

Control.register(SelectX);
