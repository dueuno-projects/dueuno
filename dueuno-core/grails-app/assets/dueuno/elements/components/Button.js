// Temporary solutions until we get support for static fields
// See: https://github.com/google/closure-compiler/issues/2731
let Button_dropdown = {
    isDetached: false,
    $toggleButton: null,
    $menu: null,
};

class Button extends Component {

    static get dropdown() { return Button_dropdown }
    static set dropdown(value) { Button_dropdown = value }

    static finalize($element, $root) {
        let $toggleButton = $element.find('.dropdown-toggle');
        $toggleButton.off('show.bs.dropdown').on('show.bs.dropdown', Button.onDropdownShow);
        $toggleButton.off('hidden.bs.dropdown').on('hidden.bs.dropdown', Button.onDropdownHidden);
    }

    static onDropdownShow(event) {
        if (Button.dropdown.isDetached) {
            Button.dropdown.$toggleButton.off('hidden.bs.dropdown');
            Button.dropdown.$toggleButton.trigger('hide');
            Button.dropdown.$toggleButton.on('hidden.bs.dropdown');
            Button.reattachDropdown();
        }

        let $element = $(event.currentTarget);
        Button.detachDropdown($element);
    }

    static onDropdownHidden(event) {
        if (Button.dropdown.isDetached) {
            Button.reattachDropdown();
        }
    }

    static onDropdownKeyDown(event) {
        if (!Button.dropdown.isDetached || !Button.dropdown.$menu[0].contains(event.target)) {
            return;
        }

        // Bootstrap resolves detached menus against the page and can pick another button.
        // Route navigation through the owning toggle before its document capture listener.
        bootstrap.Dropdown.dataApiKeydownHandler.call(Button.dropdown.$toggleButton[0], event);
    }

    static detachDropdown($element) {
        let $menu = $element.next();
        let left = $menu.offset().left;
        let top = $menu.offset().top;

        $menu.detach();
        $menu.css.position = 'absolute';
        $menu.css.left = left;
        $menu.css.top = top;

        if (PageModal.isActive) {
            PageModal.$self.append($menu);
        } else {
            Page.$self.append($menu);
        }

        Button.dropdown.isDetached = true;
        Button.dropdown.$toggleButton = $element;
        Button.dropdown.$menu = $menu;
        window.addEventListener('keydown', Button.onDropdownKeyDown, true);
    }

    static reattachDropdown() {
        window.removeEventListener('keydown', Button.onDropdownKeyDown, true);
        Button.dropdown.$menu.detach();
        Button.dropdown.$menu.css.position = false;
        Button.dropdown.$menu.css.left = false;
        Button.dropdown.$menu.css.top = false;
        Button.dropdown.$toggleButton.after(Button.dropdown.$menu);

        Button.dropdown.isDetached = false;
        Button.dropdown.$toggleButton = null;
        Button.dropdown.$menu = null;
    }

    static setDisplay($element, value) {
        let $link = $element.find('a');
        Component.setDisplay($element, value);
        Component.setDisplay($link, value);
    }

    static setVisible($element, value) {
        let $link = $element.find('a');
        Component.setVisible($element, value);
        Component.setVisible($link, value);
    }

    static setReadonly($element, value) {
        let $link = $element.find('a');
        Component.setReadonly($element, value);
        Component.setReadonly($link, value);
    }

    static setBackgroundColor($element, value) {
        let $link = $element.find('> a');
        $link.css('background-color', value);
    }

    static setTextColor($element, value) {
        let $link = $element.find('> a');
        $link.css('color', value);
    }

    static setText($element, value) {
        let $text = $element.find('span');
        $text.html(value);
    }

    static setIcon($element, value) {
        let $icon = $element.find('i');
        $icon.removeClassesStartingWith('fa-');
        $icon.addClass(value);
    }
}

Component.register(Button);
