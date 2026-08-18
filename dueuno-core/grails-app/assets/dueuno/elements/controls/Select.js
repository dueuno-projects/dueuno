/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */
class Select extends Control {

    static getValueType($element) {
        let properties = Component.getProperties($element);
        return properties.multiple ? Type.LIST : Type.STRING;
    }

    static initialize($element, $root) {
        let element = $element[0];
        let controlId = Component.getId($element);
        let properties = Component.getProperties($element);
        let dropboxPortal = Select.getDropboxPortal(element);

        let initOptions = {
            ele: element,
            dropboxWrapper: '#' + dropboxPortal.id,
            zIndex: 1060,
            options: Select.toVirtualOptions(properties.options),
            multiple: properties.multiple,
            search: properties.search,
            placeholder: properties.multiple ? '' : properties.placeholder,
            hideClearButton: properties.multiple || !properties.allowClear,
            autoSelectFirstOption: false,
            disableSelectAll: true,
            searchPlaceholderText: properties.text.search,
            loadingText: properties.text.searching,
            noOptionsText: properties.text.noResults,
            noSearchResultsText: properties.text.noResults,
            optionSelectedText: properties.text.oneSelected,
            optionsSelectedText: properties.text.manySelected,
            allOptionsSelectedText: properties.text.allSelected,
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
                        Select.setServerOptions(virtualSelect, options, searchValue);
                    },
                    error: function () {
                        virtualSelect.setServerOptions([]);
                    },
                });
            };
        }

        VirtualSelect.init(initOptions);
        let $navigationElements = $element.add(element.virtualSelect.$dropboxWrapper);
        $navigationElements
            .off('keydown.select')
            .on('keydown.select', {element: element}, Select.onKeyDown);
        $element.find('.vscomp-value')
            .off('click.select')
            .on('click.select', Select.onValueClick);
        $element.closest('.input-group')
            .children('.component-link')
            .off('keydown.select')
            .on('keydown.select', Select.onActionKeyDown);
        $element.closest('.input-group')
            .children('.component-help')
            .off('keydown.select')
            .on('keydown.select', Select.onHelpKeyDown);
        $element.closest('form')
            .off('keydown.selectNavigation')
            .on('keydown.selectNavigation', Select.onFormKeyDown);
    }

    static getDropboxPortal(element) {
        let $modal = $(element).closest('.modal');
        let portalId = $modal.length ? 'select-dropbox-portal-modal' : 'select-dropbox-portal';
        let $portal = $('#' + portalId);
        if (!$portal.length) {
            $portal = $('<div>', {id: portalId, class: 'control-select'});
            $portal.appendTo($modal.length ? $modal : $('body'));
        }
        return $portal[0];
    }

    static finalize($element, $root) {
        $element.off('change', Select.onChange).on('change', Select.onChange);
        Transition.triggerEvent($element, 'load');
    }

    static isInitialized($element) {
        return false;
    }

    static onChange(event) {
        Transition.triggerEvent($(event.currentTarget), 'change');
    }

    static onValueClick(event) {
        if ($(event.currentTarget).closest('.control-select').is('[disabled]')) {
            event.stopPropagation();
        }
    }

    /**
     * Moves focus from a VirtualSelect control. VirtualSelect handles Tab
     * internally, which can make navigation depend on its focusable children
     * (for example, the clear button is only available for selected values).
     */
    static onKeyDown(event) {
        if (event.key !== 'Tab') return;

        let $element = $(event.data.element);
        let $next;
        if (event.shiftKey) {
            $next = Select.getAdjacentControl($element, -1);
        } else {
            let $trailing = Select.getTrailingFocusable($element);
            $next = $trailing.length
                ? $trailing.first()
                : Select.getAdjacentControl($element, 1);
        }
        if (!$next.length) {
            $next = Select.getAdjacentFocusable($element, event.shiftKey ? -1 : 1);
        }
        if (!$next.length) return;

        event.preventDefault();
        event.stopPropagation();
        Component.setFocus($next, true);
    }

    static onActionKeyDown(event) {
        if (event.key !== 'Tab' || !event.shiftKey) return;

        let $action = $(event.currentTarget);
        let $actions = $action.closest('.input-group')
            .children('.component-link:not([disabled])')
            .filter(':visible');
        if (!$action.is($actions.first())) return;

        let $select = $action.closest('.input-group')
            .children('[data-21-control="Select"]:not([disabled])');
        if (!$select.length) return;

        event.preventDefault();
        event.stopPropagation();
        $select.trigger('focus');
    }

    /**
     * Restores the reverse tab order from a field help button to its Select.
     * The focusable element created by VirtualSelect is not a DOM sibling of
     * the help button, so native Shift+Tab navigation is not reliable here.
     */
    static onHelpKeyDown(event) {
        if (event.key !== 'Tab' || !event.shiftKey) return;

        let $group = $(event.currentTarget).closest('.input-group');
        let $actions = $group
            .children('.component-link:not([disabled])')
            .filter(':visible');
        let $select = $group
            .closest('.input-group')
            .children('[data-21-control="Select"]:not([disabled])');
        if (!$select.length) return;

        event.preventDefault();
        event.stopPropagation();
        ($actions.length ? $actions.last() : $select).trigger('focus');
    }

    /**
     * Handles reverse navigation when the previous enabled form control is a
     * Select. This keeps Shift+Tab symmetric with onKeyDown when one or more
     * disabled controls occur between the Select and the current control.
     */
    static onFormKeyDown(event) {
        if (event.key !== 'Tab' || !event.shiftKey) return;

        let $control = $(event.target).closest('[data-21-control]');
        if (!$control.length || $control.data('21-control') === 'Select') return;

        let $previous = Select.getAdjacentControl($control, -1);
        if ($previous.data('21-control') !== 'Select') return;

        event.preventDefault();
        event.stopPropagation();
        let $trailing = Select.getTrailingFocusable($previous);
        ($trailing.length ? $trailing.last() : $previous).trigger('focus');
    }

    static getTrailingFocusable($element) {
        return $element.closest('.input-group')
            .children('.component-link:not([disabled]), .component-help:not(:disabled)')
            .filter(':visible');
    }

    /**
     * Returns the nearest native focus target outside the Select. This is the
     * fallback for a Select at the boundary of a form, where there is no next
     * Dueuno control but the page still contains links or buttons.
     */
    static getAdjacentFocusable($element, direction) {
        let $scope = $element.closest('[data-21-component="PageContent"]');
        if (!$scope.length) $scope = $(document.body);

        let element = $element[0];
        let $focusable = $scope.find(
            'a[href], button:not(:disabled), input:not([type="hidden"]):not(:disabled), ' +
            'select:not(:disabled), textarea:not(:disabled), [tabindex]:not([tabindex="-1"])'
        ).filter(':visible').filter(function () {
            return this !== element && !element.contains(this);
        });

        let candidates = $focusable.get().filter(function (candidate) {
            let position = element.compareDocumentPosition(candidate);
            return direction > 0
                ? position & Node.DOCUMENT_POSITION_FOLLOWING
                : position & Node.DOCUMENT_POSITION_PRECEDING;
        });

        return $(direction > 0 ? candidates[0] : candidates.at(-1));
    }

    /**
     * Returns the nearest visible, enabled form control in the requested
     * direction, skipping readonly, disabled, and hidden controls.
     */
    static getAdjacentControl($element, direction) {
        let $scope = $element.closest('[data-21-component="PageContent"]');
        if (!$scope.length) $scope = $element.closest('form');

        let $controls = $scope.find('[data-21-control]');
        let index = $controls.index($element);

        for (let i = index + direction; i >= 0 && i < $controls.length; i += direction) {
            let $control = $controls.eq(i);
            let control = Control.getByElement($control);
            let isVisible = Elements.callMethod($control, control, 'getDisplay');
            let isReadonly = Elements.callMethod($control, control, 'getReadonly');
            if (isVisible && !isReadonly && $control.is(':visible')) return $control;
        }

        return $();
    }

    static setValue($element, valueMap, trigger = true) {
        valueMap = TypedValue.require(valueMap);
        let element = $element[0];
        if (!element.setValue) return;

        if (!trigger) $element.off('change', Select.onChange);

        let searchEvent = Component.getEvent($element, 'search');
        let loadEvent = Component.getEvent($element, 'load');
        if (searchEvent && loadEvent && !Select.hasOptions($element)) {
            Select.setTemporaryOptions($element, valueMap);
            if (trigger) Transition.submit(loadEvent);
        }

        element.setValue(valueMap.value, !trigger);

        if (!trigger) $element.on('change', Select.onChange);
    }

    static getValue($element) {
        let properties = Component.getProperties($element);
        let value = $element[0].value;

        if (value == null || value === '' || (Array.isArray(value) && value.length == 0)) {
            return TypedValue.empty(Select.getValueType($element));
        } else if (!properties.multiple) {
            return TypedValue.string(value);
        }
        return TypedValue.list(Array.isArray(value) ? value : [value]);
    }

    static hasOptions($element) {
        return ($element[0].options?.length ?? 0) > 0;
    }

    /**
     * Prevents VirtualSelect from applying its literal client-side filter to
     * results already filtered by the server with wildcard search semantics.
     */
    static setServerOptions(virtualSelect, options, searchValue) {
        let currentSearchValue = virtualSelect.searchValue;
        if (searchValue.includes('*')) virtualSelect.searchValue = '';

        virtualSelect.setServerOptions(Select.toVirtualOptions(options));
        virtualSelect.searchValue = currentSearchValue;
    }

    static setOptions($element, options) {
        let element = $element[0];
        let valueMap = TypedValue.require(Select.getValue($element));
        let selectedValues = Select.valueList(valueMap.value);
        let newOptions = options ?? [];
        let optionValues = newOptions.map(option => String(option.id));
        let validValues = selectedValues.filter(value => optionValues.includes(value));
        let properties = Component.getProperties($element);

        if (!validValues.length && properties.autoSelect && !properties.nullable && newOptions.length == 1) {
            validValues = [String(newOptions[0].id)];
        }

        element.setOptions(Select.toVirtualOptions(newOptions), false);
        let value = properties.multiple ? validValues : (validValues[0] ?? null);
        element.setValue(value, true);
    }

    static setTemporaryOptions($element, valueMap) {
        let options = Select.valueList(valueMap.value).map(value => ({value: value, label: '...'}));
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

    static valueList(value) {
        if (value == null) return [];
        let values = Array.isArray(value) ? value : [value];
        return values.map(value => String(value));
    }

    static toVirtualOptions(options) {
        return (options ?? []).map(option => ({
            value: String(option.id),
            label: option.text,
        }));
    }
}

Control.register(Select);
