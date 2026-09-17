class ShellMenu extends Component {

    static initialize($element, $root) {
        let $menu = $('#shell-menu-items');
        enableSimpleBar($menu[0]);
    }

    static finalize($element, $root) {
        let $search = $('#shell-menu-search input');
        $search.on('input', ShellMenu.onSearch);

        if (!_21_.user.animations) {
            $element.off('shown.bs.offcanvas').on('shown.bs.offcanvas', ShellMenu.onShown);
        }
    }

    static updateActiveItem() {
        let controller = Component.getProperty(PageContent.$self, 'controller');
        let $links = $('#shell-menu-items .nav-link');
        $links.removeClass('active').removeAttr('aria-current');

        if (!controller) return;

        $links.filter(function () {
            let clickEvent = Component.getEvent($(this), 'click');
            return clickEvent && clickEvent.controller === controller;
        }).first().addClass('active').attr('aria-current', 'page');
    }

    static onShown(event) {
        let $element = $(event.currentTarget);
        let $offcanvas = Page.$self.find('.offcanvas-backdrop');
        $offcanvas.removeClass('fade');
    }

    static onSearch(event) {
        let $searchbox = $('#shell-menu-search input');
        let search = $searchbox.val().trim().toLowerCase();

        $('#shell-menu li.nav-item:has(a)').each(function () {
            let itemText = $(this).find('a span')?.html()?.trim()?.toLowerCase();
            let itemFound = itemText && itemText.indexOf(search) >= 0
            if (itemFound) {
                $(this).removeClass('d-none');

            } else {
                $(this).addClass('d-none');
            }
        });

        if (isEmpty(search)) $('.sidebar-nav li').removeClass('hide');
    }
}

Component.register(ShellMenu);
