class Login extends Page {

    static finalize() {
        let $loginButton = $('[data-21-id="login"] > a');
        $loginButton.off('click').on('click', Login.onClick);

        $(document).off('keydown.login').on('keydown.login', Login.onKeyPress);
        $(document).off('focusin.login').on('focusin.login', '[data-21-id="username"], [data-21-id="password"]', function () {
            Login.resetPhysicalBuffer($('[data-21-id="loginKeyPress"]'));
        });
    }

    static onKeyPress(event) {
        let $element = $('[data-21-id="loginKeyPress"]');
        event.stopPropagation();

        let triggerKey = Component.getProperty($element, 'triggerKey');
        if (!Component.getProperty($element, 'enabled')) {
            let fieldId = $(event.target).data('21-id');
            if (event.key == triggerKey && ['username', 'password'].includes(fieldId)) {
                event.preventDefault();
                Login.onClick(event);
            }
            return;
        }

        let $search = $element.find('input');
        let readingSpeed = Component.getProperty($element, 'readingSpeed');
        let bufferTimeout = Component.getProperty($element, 'bufferTimeout');
        let now = performance.now();
        let printable = Control.isPrintable(event.keyCode);
        let buffer = $search.val() || '';
        let lastCharacterAt = Component.getProperty($element, 'lastCharacterAt');
        let firstCharacterAt = Component.getProperty($element, 'firstCharacterAt');
        let targetValues = Component.getProperty($element, 'targetValues') || [];

        if (buffer.length > 0 && bufferTimeout > 0 && lastCharacterAt && now - lastCharacterAt > bufferTimeout) {
            Login.resetPhysicalBuffer($element);
            buffer = '';
            firstCharacterAt = null;
            targetValues = [];
        }

        if (['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Home', 'End'].includes(event.key)) {
            Login.resetPhysicalBuffer($element);
            buffer = '';
            firstCharacterAt = null;
            targetValues = [];
        }

        if (printable && !event.ctrlKey && !event.altKey && !event.metaKey) {
            if (buffer.length == 0) {
                firstCharacterAt = now;
                targetValues = [];
            }

            Login.rememberLoginFieldValue(event.target, targetValues);
            buffer += event.key;
            $search.val(buffer);
            Component.setProperty($element, 'firstCharacterAt', firstCharacterAt);
            Component.setProperty($element, 'lastCharacterAt', now);
            Component.setProperty($element, 'targetValues', targetValues);
        }

        if (event.key == triggerKey) {
            // Compare the average interval over the complete code, not only the last key.
            let duration = buffer.length > 1 ? lastCharacterAt - firstCharacterAt : Infinity;
            let averageCharacterInterval = duration / (buffer.length - 1);
            // At least two characters are needed to estimate an inter-character interval.
            let isPhysicalToken = buffer.length >= 2 &&
                (readingSpeed == 0 || averageCharacterInterval < readingSpeed);

            if (isPhysicalToken) {
                event.preventDefault();
                Login.restoreLoginFieldValues(targetValues);
                Login.resetPhysicalBuffer($element);
                Login.onLogin(null, {
                    processUrl: 'api/auth/physical',
                    physicalId: buffer,
                });
            } else {
                Login.resetPhysicalBuffer($element);
                if (event.target.tagName == 'INPUT') {
                    event.preventDefault();
                    Login.onClick(event);
                }
            }
        }
    }

    static rememberLoginFieldValue(target, targetValues) {
        if (!target || !['username', 'password'].includes($(target).data('21-id'))) {
            return;
        }

        if (targetValues.some(value => value.target == target)) {
            return;
        }

        targetValues.push({
            target: target,
            value: target.value,
            selectionStart: target.selectionStart,
            selectionEnd: target.selectionEnd,
        });
    }

    static restoreLoginFieldValues(targetValues) {
        targetValues.forEach(value => {
            value.target.value = value.value;
            if (value.selectionStart != null && value.selectionEnd != null) {
                value.target.setSelectionRange(value.selectionStart, value.selectionEnd);
            }
        });
    }

    static resetPhysicalBuffer($element) {
        $element.find('input').val('');
        Component.setProperty($element, 'firstCharacterAt', null);
        Component.setProperty($element, 'lastCharacterAt', null);
        Component.setProperty($element, 'targetValues', []);
    }

    static onClick(event) {
        event.preventDefault();

        Login.resetPhysicalBuffer($('[data-21-id="loginKeyPress"]'));

        let $username = $('[data-21-id="username"]');
        let $password = $('[data-21-id="password"]');

        let data = {
            processUrl: 'authentication/authenticate',
            username: $username.val(),
            password: $password.val(),
        };

        Login.onLogin(null, data);
    }

    static onLogin($element, data) {

        let call = {
            type: 'POST',
            url: _21_.app.url + data.processUrl,
            data: data,
        }

        let $username = $('[data-21-id="username"]');
        let $password = $('[data-21-id="password"]');
        let $loginError = $('.page-login-error');
        let isPhysicalLogin = data.processUrl == 'api/auth/physical';
        $loginError.addClass('d-none');
        LoadingScreen.show(true, 500);

        $.ajax(call)
        .done(function (data, textStatus, request) {
            if (data.success) {
                let params = $.getQueryParameters();
                let url = _21_.app.url.substr(0, _21_.app.url.length - 1);
                if (params.landingPage) url = url + params.landingPage;
                else if (data.redirect) url = url + data.redirect;
                else url = url + '/';
                window.location.replace(url);
            }

            if (data.error) {
                LoadingScreen.show(false);
                $loginError.removeClass('d-none');
                if (!isPhysicalLogin) {
                    $username.val('').focus();
                    $password.val('');
                }
            }

            if (data.customError) {
                LoadingScreen.show(false);
                PageMessageBox.info(null, data.customError);
                if (!isPhysicalLogin) {
                    $username.val('').focus();
                    $password.val('');
                }
            }
        })
        .fail(function (request, textStatus, errorThrown) {
            LoadingScreen.show(false);
            PageMessageBox.error(null, {infoMessage: request.status + ': ' + request.responseText});
        });
    }
}

Page.register(Login);
