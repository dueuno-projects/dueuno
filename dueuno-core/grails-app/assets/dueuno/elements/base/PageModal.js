//= require Component

// Temporary solutions until we get support for static fields
// See: https://github.com/google/closure-compiler/issues/2731
let PageModal_dialog = null;
let PageModal_isActive = false;
let PageModal_isShowRequested = false;
let PageModal_hasCloseButton = true;

class PageModal extends Component {

    static get dialog() { return PageModal_dialog }
    static set dialog(value) { PageModal_dialog = value }
    static get isActive() { return PageModal_isActive }
    static set isActive(value) { PageModal_isActive = value }
    static get isShowRequested() { return PageModal_isShowRequested }
    static set isShowRequested(value) { PageModal_isShowRequested = value }
    static get hasCloseButton() { return PageModal_hasCloseButton }
    static set hasCloseButton(value) { PageModal_hasCloseButton = value }

    static get $self() { return $('#page-modal') }
    static get $header() { return $('#page-modal-header') }
    static get $body() { return $('#page-modal-body') }
    static get $backdrop() { return $('.modal-backdrop') }

    static initialize() {
        PageModal.isActive = false;
        PageModal.dialog = new bootstrap.Modal('#page-modal', { backdrop: 'static' });

        // Close Button
        let $closeButton = PageModal.$self.find('[data-21-id="closeButton"]');
        $closeButton.attr('data-bs-dismiss', 'modal');
        let $closeButtonLink = $closeButton.find('a')
        $closeButtonLink.removeAttr('data-21-events');

        // Events
        PageModal.$self.off('shown.bs.modal').on('shown.bs.modal', PageModal.onShown);
        PageModal.$self.off('hidden.bs.modal').on('hidden.bs.modal', PageModal.onHidden);
        PageModal.$self.off('keydown').on('keydown', PageModal.onKeyDown);
        $closeButton.off('click').on('click', PageModal.onClose);
    }

    static onShown(event) {
        Page.finalizeContent(PageModal.$self);
    }

    static onClose(event) {
        Page.reinitializeContent(PageContent.$self, false, true);
        PageModal.close();
    }

    static onHidden(event) {
        if (!PageModal.isActive) {
            PageModal.$body.empty();

        } else if (PageModal.isShowRequested && !PageMessageBox.isActive) {
            PageModal.show();
            PageModal.isShowRequested = false;
        }
    }

    static onScroll(event) {
        //no-op
    }

    static onKeyDown(event) {
        if (!PageModal.hasCloseButton) {
            // The dialog cannot be closed with the ESC key
            // if the close button is not present
            return;
        }

        if (event.key == 'Escape') {
            PageModal.onClose(event);
        }
    }

    static renderContent($content, componentEvent) {
        let $contentHeader = $content.find('[data-21-id="header"]');
        let $contentBody = $content.children().not('[data-21-id="header"]');

        let contentProperties = Component.getProperties($content);
        let modalProperties = Component.getProperties(PageModal.$self);
        modalProperties['controller'] = contentProperties['controller'];
        modalProperties['action'] = contentProperties['action'];
        Component.setProperties(PageModal.$self, modalProperties);

        PageModal.$header.children().not('[data-21-id="closeButton"]').remove();
        PageModal.$header.append($contentHeader);
        PageModal.$body.empty();
        PageModal.$body.append($contentBody);
    }

    static renderDialog(componentEvent) {
        // Buttons
        let $closeButton = PageModal.$header.find('[data-21-id="closeButton"]');
        let closeButtonComponent = Component.getByElement($closeButton);

        // Dialog
        PageModal.hasCloseButton = componentEvent.renderProperties['closeButton'];
        Elements.callMethod($closeButton, closeButtonComponent, 'setDisplay', PageModal.hasCloseButton);
        PageModal.dialog._config.keyboard = PageModal.hasCloseButton;

        // Size
        PageModal.$self.find('.modal-dialog').removeClass('modal-sm modal-lg modal-xl modal-fullscreen');
        if (componentEvent.renderProperties['fullscreen']) {
            PageModal.$self.children().addClass('modal-fullscreen');
            PageModal.$self.children().children().removeClass('rounded-4');

        } else if (componentEvent.renderProperties['large']) {
            PageModal.$self.children().addClass('modal-xl');
            PageModal.$self.children().children().addClass('rounded-4');

        } else if (componentEvent.renderProperties['small']) {
            PageModal.$self.children().addClass('modal-sm');
            PageModal.$self.children().children().addClass('rounded-4');

        } else {
            PageModal.$self.children().addClass('modal-lg');
            PageModal.$self.children().children().addClass('rounded-4');
        }

        // Scrollbar
        enableSimpleBar(PageModal.$body[0]);
        PageModal.$body.find('.simplebar-content-wrapper').off('scroll').on('scroll', PageModal.onScroll);
    }

    static open($content, componentEvent) {
        PageModal.isActive = true;
        PageModal.renderContent($content, componentEvent);
        Page.initializeContent(PageModal.$self, true, true);
        PageModal.renderDialog(componentEvent);

        if (PageModal.isShown()) {
            Page.finalizeContent(PageModal.$self);

        } else {
            PageModal.show();
            // Will be finalized onShown()
        }
    }

    static close() {
        PageModal.isActive = false;
        PageModal.isShowRequested = false;
        PageModal.hide();
    }

    static isShown() {
        return PageModal.$self.hasClass('show');
    }

    static show() {
        PageModal.dialog.show('#page-modal');
        PageModal.isShowRequested = true;
    }

    static hide() {
        PageModal.dialog.hide();
    }
}

Component.register(PageModal);