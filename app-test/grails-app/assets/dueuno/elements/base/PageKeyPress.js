//= require Component

let PageKeyPress_hotkeys = new Map();
let PageKeyPress_bufferElement = null;
let PageKeyPress_bufferState = null;

class PageKeyPress extends Component {

    static get hotkeys() { return PageKeyPress_hotkeys }
    static set hotkeys(value) { PageKeyPress_hotkeys = value }
    static get bufferElement() { return PageKeyPress_bufferElement }
    static set bufferElement(value) { PageKeyPress_bufferElement = value }
    static get bufferState() { return PageKeyPress_bufferState }
    static set bufferState(value) { PageKeyPress_bufferState = value }
    static get READING_SPEED_MS() { return 20 }
    static get BUFFER_TIMEOUT_MS() { return 500 }

    static get $self() { return $('#page-keypress') }

    static finalize($element, $root) {
        PageKeyPress.initializeKeyboardEvents();
    }

    static initializeKeyboardEvents() {
        // Adding the same listener again is ignored by the browser. Keep its
        // original capture order so hotkeys run before other window handlers.
        window.addEventListener('keydown', PageKeyPress.onKeyDownCapture, true);

        $(document)
            .off('keydown.pageKeyPress keyup.pageKeyPress')
            .on('keydown.pageKeyPress', PageKeyPress.onKeyPress)
            .on('keyup.pageKeyPress', PageKeyPress.onKeyUp);
        $(window).off('blur.pageKeyPress').on('blur.pageKeyPress', PageKeyPress.onWindowBlur);

        if (PageKeyPress.bufferElement && !document.documentElement.contains(PageKeyPress.bufferElement)) {
            PageKeyPress.bufferElement = null;
            PageKeyPress.bufferState = null;
        }

        PageKeyPress.hotkeys.clear();
        $('.component-link[data-21-hotkey-active]').removeAttr('data-21-hotkey-active');
        $('.component-link[data-21-hotkey]:visible').each(function () {
            PageKeyPress.registerHotkey($(this));
        });

        PageKeyPress.setHotkeysVisible(false);
    }

    static onKeyDownCapture(event) {
        PageKeyPress.setHotkeysVisible(PageKeyPress.isHotkeyModifierPressed(event));

        if (PageKeyPress.onHotkey(event)) {
            event.stopImmediatePropagation();
        }
    }

    static onKeyPress(event) {
        PageKeyPress.setHotkeysVisible(PageKeyPress.isHotkeyModifierPressed(event));

        let $element = PageKeyPress.$self;
        if (PageKeyPress.onHotkey(event)) {
            return;
        }

        if (!Component.getProperty($element, 'enabled')) {
            return;
        }

        event.stopPropagation();

        let triggerKey = Component.getProperty($element, 'triggerKey');
        if (triggerKey == null) {
            return;
        }

        let hideInput = Component.getProperty($element, 'hideInput') === true;
        let reading = PageKeyPress.readDevice(event);
        if (reading.isDevice) {
            if (triggerKey.length > 0 && reading.deviceId.length > 0 &&
                (event.target.tagName == 'A' || event.target.tagName == 'BUTTON')) {
                event.preventDefault();
            }

            let triggerEvent = Component.getEvent($element, 'keypress');
            if (triggerEvent && (event.target.tagName != 'INPUT' || !reading.printable || reading.checkMinTime || (hideInput && reading.scannerInput))) {
                if (!triggerEvent.params) triggerEvent.params = {};
                triggerEvent.params['_21KeyPressed'] = reading.deviceId;
                Transition.submit(triggerEvent);
            }
        }
    }

    static readDevice(event, options = {}) {
        let $element = PageKeyPress.$self;
        let triggerKey = options.triggerKey != null ? options.triggerKey : Component.getProperty($element, 'triggerKey');
        let hideInput = options.hideInput != null ? options.hideInput : Component.getProperty($element, 'hideInput') === true;
        let averageSpeed = options.averageSpeed == null ? true : options.averageSpeed === true;
        let minimumCharacters = options.minimumCharacters == null ? 2 : options.minimumCharacters;
        let resetOnControlKeys = options.resetOnControlKeys == null ? true : options.resetOnControlKeys === true;
        let restoreInputOnRead = options.restoreInputOnRead == null ? true : options.restoreInputOnRead === true;

        if (triggerKey == null) {
            return {triggered: false, isDevice: false};
        }

        let now = performance.now();
        let printable = Control.isPrintable(event.keyCode);
        let state = PageKeyPress.getBufferState();
        let timeInterval = state.lastKeyAt == null ? Infinity : now - state.lastKeyAt;
        if (timeInterval > PageKeyPress.BUFFER_TIMEOUT_MS) {
            PageKeyPress.resetBuffer();
            state = PageKeyPress.getBufferState();
        }

        if (resetOnControlKeys && ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Home', 'End'].includes(event.key)) {
            PageKeyPress.resetBuffer();
            state = PageKeyPress.getBufferState();
        }

        let previousKeyAt = state.lastKeyAt;
        let isModifierPressed = Control.isModifierPressed(event, state.lastKey);
        let checkMinTime = timeInterval < PageKeyPress.READING_SPEED_MS;
        let currentInputState = hideInput && event.target.tagName == 'INPUT' && printable && !isModifierPressed
            ? PageKeyPress.captureInputState(event.target)
            : null;

        if (triggerKey.length == 0) {
            state.buffer = event.key;
        } else if (printable && !event.ctrlKey && !event.altKey && !event.metaKey) {
            if (state.buffer.length == 0) {
                state.firstCharacterAt = now;
                state.targetValues = [];
            }

            if (restoreInputOnRead && currentInputState && !state.targetValues.some(value => value.target == event.target)) {
                state.targetValues.push(currentInputState);
            }
            state.buffer += event.key;
        }

        if (hideInput && triggerKey.length > 0 && !state.scannerInput && checkMinTime &&
            state.lastInterval < PageKeyPress.READING_SPEED_MS && event.target.tagName == 'INPUT' &&
            state.priorInputState && state.previousInputState &&
            state.priorInputState.target === event.target && state.previousInputState.target === event.target) {
            PageKeyPress.restoreInputState(state.priorInputState);
            state.scannerInput = true;
        }

        if (hideInput && state.scannerInput && event.target.tagName == 'INPUT' && printable && !isModifierPressed) {
            event.preventDefault();
        }

        state.lastKeyAt = now;
        state.lastKey = event.key;
        state.priorInputState = currentInputState ? state.previousInputState : state.priorInputState;
        state.previousInputState = currentInputState || state.previousInputState;
        state.lastInterval = timeInterval;

        let triggered = triggerKey.length == 0 || event.key == triggerKey;
        if (!triggered) {
            return {triggered: false, isDevice: false, printable: printable, checkMinTime: checkMinTime, scannerInput: state.scannerInput};
        }

        let duration = state.buffer.length > 1 ? previousKeyAt - state.firstCharacterAt : Infinity;
        let averageCharacterInterval = duration / (state.buffer.length - 1);
        let isDevice = triggerKey.length == 0 || (averageSpeed
            ? state.buffer.length >= minimumCharacters && averageCharacterInterval < PageKeyPress.READING_SPEED_MS
            : state.buffer.length > 0 && (checkMinTime || (hideInput && state.scannerInput)));
        let deviceId = state.buffer;

        if (isDevice && hideInput && event.target.tagName == 'INPUT') {
            if (restoreInputOnRead) {
                state.targetValues.forEach(inputState => PageKeyPress.restoreInputState(inputState));
            }
            if (state.scannerInput) {
                event.preventDefault();
            }
        }

        PageKeyPress.resetBuffer();
        return {
            triggered: true,
            isDevice: isDevice,
            deviceId: deviceId,
            printable: printable,
            checkMinTime: checkMinTime,
            scannerInput: state.scannerInput,
        };
    }

    static getBufferState() {
        let $element = PageKeyPress.$self;
        let element = $element[0];
        if (PageKeyPress.bufferElement !== element || !PageKeyPress.bufferState) {
            PageKeyPress.bufferElement = element;
            PageKeyPress.bufferState = PageKeyPress.createBufferState();
        }
        return PageKeyPress.bufferState;
    }

    static createBufferState() {
        return {
            buffer: '',
            lastKeyAt: null,
            lastKey: null,
            scannerInput: false,
            priorInputState: null,
            previousInputState: null,
            lastInterval: Infinity,
            firstCharacterAt: null,
            targetValues: [],
        };
    }

    static captureInputState(input) {
        let selectionStart = null;
        let selectionEnd = null;
        try {
            selectionStart = input.selectionStart;
            selectionEnd = input.selectionEnd;
        } catch (error) {
            // Some input types do not expose a text selection.
        }
        return {
            target: input,
            value: input.value,
            selectionStart: selectionStart,
            selectionEnd: selectionEnd,
        };
    }

    static restoreInputState(state) {
        if (!state || !state.target || !document.documentElement.contains(state.target)) {
            return;
        }
        state.target.value = state.value;
        if (state.selectionStart != null && state.selectionEnd != null) {
            try {
                state.target.setSelectionRange(state.selectionStart, state.selectionEnd);
            } catch (error) {
                // Some input types do not support text selections.
            }
        }
    }

    static resetBuffer() {
        let $element = PageKeyPress.$self;
        if (!$element || !$element.length) {
            return;
        }
        PageKeyPress.bufferElement = $element[0];
        PageKeyPress.bufferState = PageKeyPress.createBufferState();
    }

    static onKeyUp(event) {
        PageKeyPress.setHotkeysVisible(PageKeyPress.isHotkeyModifierPressed(event));
    }

    static onWindowBlur() {
        PageKeyPress.setHotkeysVisible(false);
    }

    static setHotkeysVisible(visible) {
        $('body').toggleClass('hotkeys-visible', visible && !PageMessageBox.isActive);
    }

    static isHotkeyModifierPressed(event) {
        return event.ctrlKey || event.metaKey;
    }

    static registerHotkey($element) {
        let element = $element[0];

        for (let [registeredHotkey, registeredElement] of PageKeyPress.hotkeys) {
            if (registeredElement === element) {
                PageKeyPress.hotkeys.delete(registeredHotkey);
            }
        }

        $element.removeAttr('data-21-hotkey-active');

        let hotkey = Component.getProperty($element, 'hotkey');
        if (typeof hotkey != 'string' || hotkey.length == 0) {
            return;
        }

        hotkey = hotkey.toLowerCase();
        let previousElement = PageKeyPress.hotkeys.get(hotkey);
        if (previousElement && previousElement !== element) {
            $(previousElement).removeAttr('data-21-hotkey-active');
        }

        PageKeyPress.hotkeys.set(hotkey, element);
        $element.attr('data-21-hotkey-active', '');
    }

    static onHotkey(event) {
        if (PageMessageBox.isActive) {
            return false;
        }

        let hotkey = PageKeyPress.normalizeKey(event.key);
        let element = PageKeyPress.hotkeys.get(hotkey);
        if (!element) {
            return false;
        }

        if (!document.documentElement.contains(element)) {
            PageKeyPress.hotkeys.delete(hotkey);
            return false;
        }

        let $link = $(element);
        let registeredHotkey = Component.getProperty($link, 'hotkey');
        if (typeof registeredHotkey != 'string' || registeredHotkey.toLowerCase() != hotkey ||
            !Component.getDisplay($link) || !Component.getVisible($link) ||
            Component.getProperty($link, 'readonly') === true || $link.is('[disabled]') ||
            !PageKeyPress.matchesHotkey(hotkey, event)) {
            return false;
        }

        event.preventDefault();
        event.stopPropagation();
        if (!event.repeat) {
            $link.trigger('click');
        }
        return true;
    }

    static matchesHotkey(hotkey, event) {
        if (typeof hotkey != 'string') {
            return false;
        }

        let key = PageKeyPress.normalizeKey(event.key);
        return PageKeyPress.isHotkeyModifierPressed(event) && key == hotkey.toLowerCase();
    }

    static normalizeKey(key) {
        let normalized = key.toLowerCase();
        let aliases = {
            return: 'enter',
        };
        return aliases[normalized] || normalized;
    }
}

Component.register(PageKeyPress);
