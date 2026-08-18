class Form extends Component {

    static finalize($element, $root) {
        let properties = Component.getProperties($element);
        if (!Elements.isMobileDevice && properties.autofocus) {
            requestAnimationFrame(function() { Form.setFocusOnFirstField($element) });
        }
    }

    static setFocusOnFirstField($element) {
        let $controls = $element.find('[data-21-control]');
        for (let element of $controls) {
            let $control = $(element);
            let control = Control.getByElement($control);
            let isVisible = Elements.callMethod($control, control, 'getDisplay');
            let isReadonly = Elements.callMethod($control, control, 'getReadonly');

            if (isVisible && !isReadonly) {
                Component.setFocus($control, true);
                break;
            }
        }
    }

    static setErrors($element, value) {
        LoadingScreen.show(false);
        if (PageModal.isActive) {
            PageModal.show();
        }

        Form.resetErrors($element);

        let errors = value.errors;
        for (let error of errors) {
            let $field;
            let message;

            if (error.field) {
                // let controlIdParts = error.field.split('.');
                // let controlId = controlIdParts.shift();
                let fieldId = error.field + 'Field';

                $field = Elements.getElementById(fieldId, $element);
                message = error.message;

            } else {
                message = error.message;
                PageMessageBox.info(null, {infoMessage: message});
                return;
            }

            FormField.setError($field, message);
        }
    }

    static resetErrors($element) {
        $element.find('.error-message').addClass('d-none');
        $element.find('.input-group').removeClass('error-highlight');
    }
}

Component.register(Form);
