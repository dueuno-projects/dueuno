class ShellMenu extends Component {

    static initialize($element, $root) {
        let $menu = $('#shell-menu-items');
        enableSimpleBar($menu[0]);
    }

    static finalize($element, $root) {
        let $search = $('#shell-menu-search input');
        $search.on('input', ShellMenu.onSearch);

        $element.off('click.shellMenuNavigation', '#shell-menu-items a.nav-link')
            .on('click.shellMenuNavigation', '#shell-menu-items a.nav-link', ShellMenu.onNavigate);

        $('#shell-menu-items').off('focusin.shellMenuNavigation')
            .on('focusin.shellMenuNavigation', ShellMenu.onMenuFocusIn);

        if (!_21_.user.animations) {
            $element.off('shown.bs.offcanvas').on('shown.bs.offcanvas', ShellMenu.onShown);
        }
    }

    static onMenuFocusIn(event) {
        let $items = $(event.currentTarget);
        if ($items.is(event.relatedTarget) || $items.has(event.relatedTarget).length) return;
        if (!$(event.target).is('a.nav-link:focus-visible')) return;

        let $active = $items.find('a.nav-link[aria-current="page"]:not([disabled])')
            .filter(':visible').first();
        if (!$active.length) return;

        if ($active[0] === event.target) return;

        Component.setFocus($active, true);
    }

    static prepareInitialNavigation() {
        let $content = PageContent.$self;
        if (!$content.length || PageModal.isActive || $content.data('menuNavigationInitialized')) return;

        $content.data('menuNavigationInitialized', true).data('initialMenuNavigation', true);
        // Listen on focus targets too: custom controls can stop keydown propagation.
        $content.find('input, select, textarea, button, a, [tabindex]').addBack()
            .on('keydown.shellMenuInitialNavigation', ShellMenu.onInitialContentInteraction);
        $(document).off('keydown.shellMenuInitialNavigation pointerdown.shellMenuInitialNavigation')
            .on('keydown.shellMenuInitialNavigation pointerdown.shellMenuInitialNavigation', ShellMenu.onInitialContentInteraction);
    }

    static onInitialContentInteraction(event) {
        let $content = PageContent.$self;
        if (!$content.data('initialMenuNavigation')) return;
        if (event.type === 'keydown' && ['Shift', 'Control', 'Alt', 'Meta'].includes(event.key)) return;

        $content.removeData('initialMenuNavigation');
        $(document).off('keydown.shellMenuInitialNavigation pointerdown.shellMenuInitialNavigation');
        $content.find('input, select, textarea, button, a, [tabindex]').addBack()
            .off('keydown.shellMenuInitialNavigation');

        if (PageModal.isActive || event.type !== 'keydown' || event.key !== 'Tab' || !event.shiftKey
            || !$(event.target).closest('#page-content').length) return;

        let $menu = $('#shell-menu');
        if ($menu.css('visibility') === 'hidden') return;
        let $items = $('#shell-menu-items');
        let $links = $items.find('a.nav-link:not([disabled])').filter(':visible');
        let index = $links.index($links.filter('[aria-current="page"]').first());
        if (index < 0) return;

        event.preventDefault();
        event.stopImmediatePropagation();
        Component.setFocus($links.eq(index), true);
    }

    static onNavigate(event) {
        let $link = $(event.target).closest('#shell-menu-items a.nav-link');
        if (!$link.length || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;

        let navigation = Component.getEvent($link, 'click');
        if (!navigation || Component.getReadonly($link)
            || (navigation.target && navigation.target !== '_self')
            || navigation.renderProperties?.modal) return;

        $('#shell-menu').data('focusContentAfterNavigation', PageContent.$self[0]);
    }

    static focusContentAfterNavigation() {
        let $menu = $('#shell-menu');
        let previousContent = $menu.data('focusContentAfterNavigation');
        let $content = PageContent.$self;
        if (!previousContent || !$content.length || previousContent === $content[0] || PageModal.isActive) return;

        $menu.removeData('focusContentAfterNavigation');
        let focusContent = function () {
            requestAnimationFrame(function () {
                if (PageModal.isActive || PageContent.$self[0] !== $content[0]) return;
                ShellMenu.focusContent($content);
            });
        };

        if ($menu.is('.show, .hiding')) {
            $menu.one('hidden.bs.offcanvas', focusContent);
        } else {
            focusContent();
        }
    }

    static focusContent($content) {
        for (let element of $content.find('[data-21-control]').filter(':visible')) {
            let $control = $(element);
            let control = Control.getByElement($control);
            if (!Elements.callMethod($control, control, 'getDisplay')
                || Elements.callMethod($control, control, 'getReadonly')) continue;

            Component.setFocus($control, true);
            if (document.activeElement === element || element.contains(document.activeElement)) return;
        }

        $content.attr('tabindex', '-1');
        Component.setFocus($content, true);
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
