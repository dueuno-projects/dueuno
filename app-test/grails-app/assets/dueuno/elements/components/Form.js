class Form extends Component {

    static finalize($element, $root) {
        let properties = Component.getProperties($element);
        if (properties.autofocus) {
            Form.setFocusOnFirstInput($element);
        }
    }

    static setFocusOnFirstInput($element) {
        let $form = $('[data-21-id="' + $element.prop('id') + '"]');
        let $controls = $form.find('[data-21-control]');

        $controls.each(function() {
            let $control = $(this);
            let control = Control.getByElement($control);
            let className = Control.getClassName($control);
            let readonly = Elements.callMethod($control, control, 'getReadonly');

            let excludedControls = [
                'HiddenField',
                'PasswordField',
                'Button',
                'Link',
                'Label',
                'Separator',
            ];
            if (!Elements.isMobileDevice && !excludedControls.includes(className) && !readonly) {
                $control.css('visibility', 'visible');
                $control.triggerHandler('focus');
                return false;
            }
        });
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
