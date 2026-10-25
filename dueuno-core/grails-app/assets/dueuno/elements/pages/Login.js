class Login extends Page {

    static finalize() {
        let $loginButton = $('[data-21-id="login"] > a');
        $loginButton.off('click').on('click', Login.onClick);

        $(document).off('keydown.login').on('keydown.login', Login.onKeyPress);
        $(document).off('focusin.login').on('focusin.login', '[data-21-id="username"], [data-21-id="password"]', function () {
            PageKeyPress.resetBuffer();
        });
    }

    static onKeyPress(event) {
        event.stopPropagation();

        let triggerKey = Component.getProperty(PageKeyPress.$self, 'triggerKey');
        let fieldId = $(event.target).data('21-id');
        if (Component.getProperty(Page.$self, 'physicalIdEnabled') !== true) {
            if (event.key == triggerKey && ['username', 'password'].includes(fieldId)) {
                event.preventDefault();
                Login.onClick(event);
            }
            return;
        }

        let reading = PageKeyPress.readDevice(event, {
            triggerKey: triggerKey,
            hideInput: Component.getProperty(PageKeyPress.$self, 'hideInput') === true,
            averageSpeed: true,
            minimumCharacters: 2,
            resetOnControlKeys: true,
            restoreInputOnRead: true,
        });

        if (reading.isDevice) {
            event.preventDefault();
            Login.onLogin(null, {
                processUrl: 'api/auth/physical',
                physicalId: reading.deviceId,
            });
        } else if (reading.triggered && event.target.tagName == 'INPUT') {
            event.preventDefault();
            Login.onClick(event);
        }
    }

    static onClick(event) {
        event.preventDefault();

        PageKeyPress.resetBuffer();

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
