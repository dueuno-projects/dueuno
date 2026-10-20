//= require Component

let KeyPress_shortcuts = new Map();

class KeyPress extends Component {

    static get shortcuts() { return KeyPress_shortcuts }
    static set shortcuts(value) { KeyPress_shortcuts = value }

    static finalize($element, $root) {
        KeyPress.initializeKeyboardEvents();
    }

    static initializeKeyboardEvents() {
        $(document)
            .off('keydown.keyPress keyup.keyPress')
            .on('keydown.keyPress', KeyPress.onKeyPress)
            .on('keyup.keyPress', KeyPress.onKeyUp);
        $(window).off('blur.keyPress').on('blur.keyPress', KeyPress.onWindowBlur);

        KeyPress.shortcuts.clear();
        $('.component-link[data-21-shortcut-active]').removeAttr('data-21-shortcut-active');
        $('.component-link[data-21-shortcut]:visible').each(function () {
            KeyPress.registerShortcut($(this));
        });

        KeyPress.setShortcutsVisible(false);
    }

    static onKeyPress(event) {
        KeyPress.setShortcutsVisible(KeyPress.isShortcutModifierPressed(event));

        let $element = $('[data-21-id="keyPress"]');
        let $search = $element.find('input');

        if (KeyPress.onShortcut(event)) {
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
        KeyPress.setShortcutsVisible(KeyPress.isShortcutModifierPressed(event));
    }

    static onWindowBlur() {
        KeyPress.setShortcutsVisible(false);
    }

    static setShortcutsVisible(visible) {
        $('body').toggleClass('keyboard-shortcuts-visible', visible && !PageMessageBox.isActive);
    }

    static isShortcutModifierPressed(event) {
        return event.ctrlKey || event.metaKey;
    }

    static registerShortcut($element) {
        let element = $element[0];

        for (let [registeredShortcut, registeredElement] of KeyPress.shortcuts) {
            if (registeredElement === element) {
                KeyPress.shortcuts.delete(registeredShortcut);
            }
        }

        $element.removeAttr('data-21-shortcut-active');

        let shortcut = Component.getProperty($element, 'shortcut');
        if (typeof shortcut != 'string' || shortcut.length == 0) {
            return;
        }

        shortcut = shortcut.toLowerCase();
        let previousElement = KeyPress.shortcuts.get(shortcut);
        if (previousElement && previousElement !== element) {
            $(previousElement).removeAttr('data-21-shortcut-active');
        }

        KeyPress.shortcuts.set(shortcut, element);
        $element.attr('data-21-shortcut-active', '');
    }

    static onShortcut(event) {
        if (PageMessageBox.isActive || event.repeat) {
            return false;
        }

        let shortcut = KeyPress.normalizeKey(event.key);
        let element = KeyPress.shortcuts.get(shortcut);
        if (!element) {
            return false;
        }

        if (!document.documentElement.contains(element)) {
            KeyPress.shortcuts.delete(shortcut);
            return false;
        }

        let $link = $(element);
        let registeredShortcut = Component.getProperty($link, 'shortcut');
        if (typeof registeredShortcut != 'string' || registeredShortcut.toLowerCase() != shortcut ||
            !Component.getDisplay($link) || !Component.getVisible($link) || Component.getReadonly($link) ||
            !KeyPress.matchesShortcut(shortcut, event)) {
            return false;
        }

        event.preventDefault();
        event.stopPropagation();
        $link.trigger('click');
        return true;
    }

    static matchesShortcut(shortcut, event) {
        if (typeof shortcut != 'string') {
            return false;
        }

        let key = KeyPress.normalizeKey(event.key);
        return KeyPress.isShortcutModifierPressed(event) && key == shortcut.toLowerCase();
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
