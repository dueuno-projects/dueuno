// Temporary solutions until we get support for static fields
// See: https://github.com/google/closure-compiler/issues/2731
let PageStickyBox_top = 0;
let PageStickyBox_offset = 0;
let PageStickyBox_isActive = false;

class PageStickyBox {

    static get top() { return PageStickyBox_top }
    static set top(value) { PageStickyBox_top = value }
    static get offset() { return PageStickyBox_offset }
    static set offset(value) { PageStickyBox_offset = value }
    // On some combinations of font size and resolution the offest is one line bigger then needed,
    // this trick reduces the cases to almost none
    static get offsetAdjustment() { return Elements.isMobileDevice ? 1 : .3 }
    static get isActive() { return PageStickyBox.isStickyBoxActive() }

    static get $self() { return $('#page-sticky-box') }

    static initialize() {
        if (!PageContent.$self.exists()) {
            return;
        }

        if (PageModal.isActive) {
            return;
        }

        PageStickyBox.markStickyComponents();
        PageStickyBox.offset = PageStickyBox.top;

        let isNeeded = PageStickyBox.isStickyBoxNeeded();
        let isActive = PageStickyBox.isStickyBoxActive();
        let hasNewStickyComponents = PageStickyBox.hasNewStickyComponents();

        if (!isNeeded || (isActive && hasNewStickyComponents)) {
            // Loading a new content from a Transition
            // - WITHOUT stickies
            // - With NEW stickies for an already active sticky box
            PageStickyBox.$self.remove();

        } else if (isActive) {
            // A PageModal has been opened and closed. We move the StickyBox into
            // #page-content to let Page.initializeContent() to do its job
            PageContent.$self.prepend(PageStickyBox.$self);
        }
    }

    static finalize() {
        if (!PageContent.$self.exists()) {
            return;
        }

        if (PageModal.isActive) {
            return;
        }

        let isNeeded = PageStickyBox.isStickyBoxNeeded();
        let isActive = PageStickyBox.isStickyBoxActive();
        if (isNeeded && !isActive) {
            // First time or new sticky components have been provided
            let $stickyComponents = PageContent.$self.find('> [data-21-component][sticky]');
            if ($stickyComponents.exists()) {
                let $stickyBox = $('<div id="page-sticky-box"></div>');
                PageContent.$self.before($stickyBox);
                $stickyBox.append($stickyComponents);

                // Making space for the #page-content
                let offsetHeight = PageStickyBox.$self[0].offsetHeight - PageStickyBox.offsetAdjustment;
                PageStickyBox.offset = PageStickyBox.top + offsetHeight;
                PageContent.$self.css('padding-top', 'calc(' + offsetHeight + 'px + .25rem)');
            }

        } else if (isActive) {
            // We move the sticky box back to its sticky position
            PageContent.$self.before(PageStickyBox.$self);
        }

        // We need to manually finalize the sticky box since it is outside
        // of the #page-content element and will not be automatically
        // initialized
        Page.finalizeControls(PageStickyBox.$self, true);
        Page.finalizeComponents(PageStickyBox.$self, true);
    }

    static markStickyComponents() {
        let hasStickyComponents = false;
        let $components = PageContent.$self.find('> [data-21-component]');
        for (let component of $components) {
            let $component = $(component);
            let properties = Component.getProperties($component);
            if (properties['sticky']) {
                PageStickyBox.setSticky($(component));
                hasStickyComponents = true;
            }
        }

        if (PageContent.$self.exists() && hasStickyComponents) {
            PageContent.$self[0].setAttribute('sticky', '');
        }
    }

    static hasNewStickyComponents() {
        let $stickyComponents = PageContent.$self.find('> [data-21-component][sticky]');
        return $stickyComponents.exists();
    }

    static isStickyBoxNeeded() {
        if (!PageContent.$self.exists()) {
            return false;
        }

        return PageContent.$self[0].hasAttribute('sticky');
    }

    static isStickyBoxActive() {
        return PageStickyBox.$self.exists();
    }

    static getContentStickyComponents() {
        let $contentSticky = PageContent.$self.find('> [data-21-component][sticky]');
        return $contentSticky;
    }

    static setSticky($element) {
        $element[0].setAttribute('sticky', '');
    }

    static setTop($element) {
        PageStickyBox.top = $element[0].offsetHeight - PageStickyBox.offsetAdjustment;
    }

}