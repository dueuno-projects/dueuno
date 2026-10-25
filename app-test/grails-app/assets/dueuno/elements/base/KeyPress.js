//= require Component

let KeyPress_hotkeys = new Map();

class KeyPress extends Component {

    static get hotkeys() { return KeyPress_hotkeys }
    static set hotkeys(value) { KeyPress_hotkeys = value }
    static get READING_SPEED_MS() { return 20 }
    static get BUFFER_TIMEOUT_MS() { return 500 }

    static finalize($element, $root) {
        KeyPress.initializeKeyboardEvents();
    }

    static initializeKeyboardEvents() {
        // Adding the same listener again is ignored by the browser. Keep its
        // original capture order so hotkeys run before other window handlers.
        window.addEventListener('keydown', KeyPress.onKeyDownCapture, true);

        $(document)
            .off('keydown.keyPress keyup.keyPress')
            .on('keydown.keyPress', KeyPress.onKeyPress)
            .on('keyup.keyPress', KeyPress.onKeyUp);
        $(window).off('blur.keyPress').on('blur.keyPress', KeyPress.onWindowBlur);

        KeyPress.hotkeys.clear();
        $('.component-link[data-21-hotkey-active]').removeAttr('data-21-hotkey-active');
        $('.component-link[data-21-hotkey]:visible').each(function () {
            KeyPress.registerHotkey($(this));
        });

        KeyPress.setHotkeysVisible(false);
    }

    static onKeyDownCapture(event) {
        KeyPress.setHotkeysVisible(KeyPress.isHotkeyModifierPressed(event));

        if (KeyPress.onHotkey(event)) {
            event.stopImmediatePropagation();
        }
    }

    static onKeyPress(event) {
        KeyPress.setHotkeysVisible(KeyPress.isHotkeyModifierPressed(event));

        let $element = $('[data-21-id="keyPress"]');
        if (KeyPress.onHotkey(event)) {
            return;
        }

        if (!Component.getProperty($element, 'enabled')) {
            return;
        }

        event.stopPropagation();

        let triggerKey = Component.getProperty($element, 'triggerKey');
        let hideInput = Component.getProperty($element, 'hideInput') === true;
        let buffer = Component.getProperty($element, 'buffer') || '';
        let lastKeyAt = Component.getProperty($element, 'lastKeyAt');
        let lastKey = Component.getProperty($element, 'lastKey');
        let scannerInput = Component.getProperty($element, 'scannerInput') || false;
        let priorInputState = Component.getProperty($element, 'priorInputState');
        let previousInputState = Component.getProperty($element, 'previousInputState');
        let lastInterval = Component.getProperty($element, 'lastInterval');

        if (triggerKey == null) {
            return;
        }

        let now = performance.now();
        let timeInterval = lastKeyAt == null ? Infinity : now - lastKeyAt;
        if (timeInterval > KeyPress.BUFFER_TIMEOUT_MS) {
            buffer = '';
            scannerInput = false;
            priorInputState = null;
            previousInputState = null;
            lastInterval = Infinity;
        }

        let printable = Control.isPrintable(event.keyCode);
        let isModifierPressed = Control.isModifierPressed(event, lastKey);
        let checkMinTime = timeInterval < KeyPress.READING_SPEED_MS;
        let currentInputState = hideInput && event.target.tagName == 'INPUT' && printable && !isModifierPressed
            ? KeyPress.captureInputState(event.target)
            : null;

        if (triggerKey.length == 0) {
            buffer = event.key;
        } else {
            if (printable) {
                buffer += event.key;
            }
        }

        if (hideInput && triggerKey.length > 0 && !scannerInput && checkMinTime &&
            lastInterval < KeyPress.READING_SPEED_MS && event.target.tagName == 'INPUT' &&
            priorInputState && previousInputState &&
            priorInputState.target === event.target && previousInputState.target === event.target) {
            KeyPress.restoreInputState(priorInputState);
            scannerInput = true;
        }

        if (hideInput && scannerInput && event.target.tagName == 'INPUT' && printable && !isModifierPressed) {
            event.preventDefault();
        }

        Component.setProperty($element, 'buffer', buffer);
        Component.setProperty($element, 'lastKeyAt', now);
        Component.setProperty($element, 'lastKey', event.key);
        Component.setProperty($element, 'scannerInput', scannerInput);
        Component.setProperty($element, 'priorInputState', currentInputState ? previousInputState : priorInputState);
        Component.setProperty($element, 'previousInputState', currentInputState || previousInputState);
        Component.setProperty($element, 'lastInterval', timeInterval);

        if ((event.key == triggerKey && buffer.length > 0 && (checkMinTime || (hideInput && scannerInput))) || triggerKey.length == 0) {
            if (hideInput && scannerInput && event.target.tagName == 'INPUT') {
                event.preventDefault();
            }

            if (triggerKey.length > 0 && buffer.length > 0 &&
                (event.target.tagName == 'A' || event.target.tagName == 'BUTTON')) {
                event.preventDefault();
            }

            let triggerEvent = Component.getEvent($element, 'keypress');
            if (triggerEvent && (event.target.tagName != 'INPUT' || !printable || checkMinTime || (hideInput && scannerInput))) {
                if (!triggerEvent.params) triggerEvent.params = {};
                triggerEvent.params['_21KeyPressed'] = buffer;
                Transition.submit(triggerEvent);
            }
            KeyPress.resetBuffer($element);
        }
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

    static resetBuffer($element) {
        Component.setProperty($element, 'buffer', '');
        Component.setProperty($element, 'scannerInput', false);
        Component.setProperty($element, 'priorInputState', null);
        Component.setProperty($element, 'previousInputState', null);
        Component.setProperty($element, 'lastInterval', Infinity);
    }

    static onKeyUp(event) {
        KeyPress.setHotkeysVisible(KeyPress.isHotkeyModifierPressed(event));
    }

    static onWindowBlur() {
        KeyPress.setHotkeysVisible(false);
    }

    static setHotkeysVisible(visible) {
        $('body').toggleClass('hotkeys-visible', visible && !PageMessageBox.isActive);
    }

    static isHotkeyModifierPressed(event) {
        return event.ctrlKey || event.metaKey;
    }

    static registerHotkey($element) {
        let element = $element[0];

        for (let [registeredHotkey, registeredElement] of KeyPress.hotkeys) {
            if (registeredElement === element) {
                KeyPress.hotkeys.delete(registeredHotkey);
            }
        }

        $element.removeAttr('data-21-hotkey-active');

        let hotkey = Component.getProperty($element, 'hotkey');
        if (typeof hotkey != 'string' || hotkey.length == 0) {
            return;
        }

        hotkey = hotkey.toLowerCase();
        let previousElement = KeyPress.hotkeys.get(hotkey);
        if (previousElement && previousElement !== element) {
            $(previousElement).removeAttr('data-21-hotkey-active');
        }

        KeyPress.hotkeys.set(hotkey, element);
        $element.attr('data-21-hotkey-active', '');
    }

    static onHotkey(event) {
        if (PageMessageBox.isActive) {
            return false;
        }

        let hotkey = KeyPress.normalizeKey(event.key);
        let element = KeyPress.hotkeys.get(hotkey);
        if (!element) {
            return false;
        }

        if (!document.documentElement.contains(element)) {
            KeyPress.hotkeys.delete(hotkey);
            return false;
        }

        let $link = $(element);
        let registeredHotkey = Component.getProperty($link, 'hotkey');
        if (typeof registeredHotkey != 'string' || registeredHotkey.toLowerCase() != hotkey ||
            !Component.getDisplay($link) || !Component.getVisible($link) ||
            Component.getProperty($link, 'readonly') === true || $link.is('[disabled]') ||
            !KeyPress.matchesHotkey(hotkey, event)) {
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

        let key = KeyPress.normalizeKey(event.key);
        return KeyPress.isHotkeyModifierPressed(event) && key == hotkey.toLowerCase();
    }

    static normalizeKey(key) {
        let normalized = key.toLowerCase();
        let aliases = {
            return: 'enter',
        };
        return aliases[normalized] || normalized;
    }
}

Component.register(KeyPress);
