//= require Component

// Temporary solutions until we get support for static fields
// See: https://github.com/google/closure-compiler/issues/2731
let PageMessageBox_isActive = false;
let PageMessageBox_dialog = null;

class PageMessageBox extends Component {

    static get isActive() { return PageMessageBox_isActive }
    static set isActive(value) { PageMessageBox_isActive = value }
    static get dialog() { return PageMessageBox_dialog }
    static set dialog(value) { PageMessageBox_dialog = value }

    static get $self() { return $('#page-messagebox') }

    static initialize() {
        PageMessageBox.isActive = false;
        PageMessageBox.dialog = new bootstrap.Modal('#page-messagebox', {
            backdrop: 'static',
        });

        PageMessageBox.$self.off('show.bs.modal').on('show.bs.modal', PageMessageBox.onShow);
        PageMessageBox.$self.off('hide.bs.modal').on('hide.bs.modal', PageMessageBox.onHide);
    }

    static onShow(event) {
        LoadingScreen.show(false);

        if (PageMessageBox.isActive) {
            PageMessageBox.hide();
        }

        if (PageModal.isActive) {
            PageModal.hide();
        }
    }

    static onHide(event) {
        document.activeElement.blur();

        if (PageModal.isActive) {
            PageModal.show();
        }
    }

    static info($element, componentEvent) {
        PageMessageBox.reset();
        PageMessageBox.setMessage(componentEvent['infoMessage']);

        // Dialog
        let $icon = $('#page-messagebox .message-icon');
        $icon.addClass('fa-solid ' + (componentEvent['icon'] ?? 'fa-circle-info'));

        let $footer = $('#page-messagebox-footer');
        $footer.addClass('justify-content-center');

        // Buttons
        let $cancelButton = PageMessageBox.getButton('cancel');
        let $confirmButton = PageMessageBox.getButton('confirm');

        if (componentEvent.click) {
            let $confirmLink = $confirmButton.find('a');
            Component.setEvent($confirmLink, 'click', componentEvent.click);

        } else {
            $confirmButton.attr('data-bs-dismiss', 'modal');
        }

        Page.reinitializeContent(PageMessageBox.$self, true);
        Component.setDisplay($cancelButton, false);

        PageMessageBox.show();
    }

    static error($element, componentEvent) {
        componentEvent['icon'] = 'fa-circle-xmark';
        PageMessageBox.info(null, componentEvent);
    }

    // args = {confirmMessage: '', clickCancel: eventData1, clickConfirm: eventData2}
    static confirm($element, args) {
        PageMessageBox.reset();
        PageMessageBox.setMessage(args['confirmMessage']);

        // Dialog
        let $verify = PageMessageBox.$self.find('#page-messagebox-verify');
        $verify.removeClass('d-none');

        let $footer = $('#page-messagebox-footer');
        $footer.addClass('justify-content-between');

        // Button 1 - CANCEL
        let $cancelButton = PageMessageBox.getButton('cancel');
        if (args.clickCancel) {
            args.confirmMessage = null;
            let $link = $cancelButton.find('a');
            Component.setEvent($link, 'click', args.clickCancel);
        } else {
            $cancelButton.attr('data-bs-dismiss', 'modal');
        }

        // Button 2 - CONFIRM
        let $confirmButton = PageMessageBox.getButton('confirm');
        if (args.clickConfirm) {
            args.clickConfirm['confirmMessage'] = null;
            let $link = $confirmButton.find('a');
            Component.setEvent($link, 'click', args.clickConfirm);
        } else {
            $confirmButton.attr('data-bs-dismiss', 'modal');
        }

        Page.reinitializeContent(PageMessageBox.$self, true);

        $cancelButton.find('a').on('click', function (event) {
            PageMessageBox.$self.off('hide.bs.modal');
            PageMessageBox.hide();
            PageMessageBox.$self.on('hide.bs.modal', PageMessageBox.onHide);
        });

        $confirmButton.find('a').on('click', function (event) {
            PageMessageBox.$self.off('hide.bs.modal');
            PageMessageBox.hide();
            PageMessageBox.$self.on('hide.bs.modal', PageMessageBox.onHide);
        });

        PageMessageBox.show();
    }

    static reset() {
        let $icon = $('#page-messagebox .message-icon');
        let $footer = $('#page-messagebox-footer');

        // Message
        PageMessageBox.setMessage('');

        // Dialog size
        PageMessageBox.$self.removeClass('modal-lg modal-xl modal-fullscreen');

        // Footer
        $icon.removeClassesStartingWith('fa-');
        let $verify = PageMessageBox.$self.find('#page-messagebox-verify');
        $verify.addClass('d-none');
        $footer.removeClass('justify-content-between');
        $footer.removeClass('justify-content-center');

        // Buttons
        let $cancelButton = PageMessageBox.getButton('cancel');
        $cancelButton.removeAttr('data-bs-dismiss');
        $cancelButton.find('a').removeAttr('data-21-events');
        $cancelButton.find('a').removeData('21-events');
        $cancelButton.find('a').off('click');

        let $confirmButton = PageMessageBox.getButton('confirm');
        $confirmButton.removeAttr('data-bs-dismiss');
        $confirmButton.find('a').removeAttr('data-21-events');
        $confirmButton.find('a').removeData('21-events');
        $confirmButton.find('a').off('click');
    }

    static getButton(id) {
        let $element = PageMessageBox.$self.find('[data-21-id="' + id + '"]');
        return $element;
    }

    static show() {
        PageMessageBox.dialog.show('#page-messagebox');
        PageMessageBox.isActive = true;
    }

    static hide() {
        PageMessageBox.dialog.hide();
        PageMessageBox.isActive = false;
    }

    static setMessage(value) {
        PageMessageBox.$self.find('p.message').html(value);
    }
}

Component.register(PageMessageBox);
