//= require Component

let KeyPress_hotkeys = new Map();

class KeyPress extends Component {

    static get hotkeys() { return KeyPress_hotkeys }
    static set hotkeys(value) { KeyPress_hotkeys = value }

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
        let $search = $element.find('input');

        if (KeyPress.onHotkey(event)) {
            return;
        }

        if (!Component.getProperty($element, 'enabled')) {
            return;
        }

        event.stopPropagation();

        let triggerKey = Component.getProperty($element, 'triggerKey');
        let readingSpeed = Component.getProperty($element, 'readingSpeed');
        let bufferTimeout = Component.getProperty($element, 'bufferTimeout');
        let hideInput = Component.getProperty($element, 'hideInput');
        let timer = Component.getProperty($element, 'timer');
        let lastKey = Component.getProperty($element, 'lastKey');

        if (triggerKey == null) {
            return;
        }

        if (isNaN(timer)) timer = 0;
        let now = Date.now();
        let timeInterval = now - timer;
        Component.setProperty($element, 'timer', now);
        Component.setProperty($element, 'lastKey', event.key);

        let checkMinTime = (readingSpeed == 0 || timeInterval < readingSpeed);
        let checkMaxTime = (bufferTimeout == 0 || timeInterval > bufferTimeout);

        if (checkMaxTime) {
            $search.val('');
        }

        let printable = Control.isPrintable(event.keyCode);
        let isModifierPressed = Control.isModifierPressed(event, lastKey);

        if (triggerKey.length == 0) {
            $search.val(event.key);
        } else {
            if (printable) {
                $search.val($search.val() + event.key);
            }
        }

        if (hideInput && !isModifierPressed && checkMinTime && event.target.tagName == 'INPUT') {
            event.preventDefault();
            $(event.target).val('');
        }

        if ((event.key == triggerKey && $search.val().length > 0 && checkMinTime) || triggerKey.length == 0) {
            if (triggerKey.length > 0 && $search.val().length > 0 &&
                (event.target.tagName == 'A' || event.target.tagName == 'BUTTON')) {
                event.preventDefault();
            }

            let triggerEvent = Component.getEvent($element, 'keypress');
            if (triggerEvent && (event.target.tagName != 'INPUT' || !printable || checkMinTime)) {
                if (!triggerEvent.params) triggerEvent.params = {};
                triggerEvent.params['_21KeyPressed'] = $search.val();
                Transition.submit(triggerEvent);
            }
            $search.val('');
        }
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
