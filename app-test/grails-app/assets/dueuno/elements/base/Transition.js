class Transition {

    static wsConnect() {
        const wsClient = new StompJs.Client();
        if (_21_.log.debug) {
            wsClient.debug = console.log;
        }
        wsClient.brokerURL = 'ws' + _21_.app.url.replace('http', '') + 'stomp';
        wsClient.onConnect = Transition.wsOnConnect;
        wsClient.activate();
    }

    static wsOnConnect(frame) {
        let wsClient = this;
        let username = frame.headers['user-name'];
        Transition.wsSubscribe(wsClient, "/queue/username/" + username);

        $.ajax({url: _21_.app.url + "transition/channels"})
        .done(function(channels) {
            for (let channel of channels) {
                Transition.wsSubscribe(wsClient, "/queue/channel/" + channel);
            }
        });
    }

    static wsOnError(frame) {
        log.debug(frame);
    }

    static wsSubscribe(wsClient, channel) {
        wsClient.subscribe(channel, Transition.executeFromWebsocket);
        log.debug('Subscribed to ' + channel);
    }

    static async executeFromWebsocket(message) {
        // We cannot send $components in a Web Socket transition since
        // Components can only exists in the context of a web request
        let commands = JSON.parse(message.body);
        let transition = {
            commands: commands,
        };

        log.debug('');
        log.debug('%c>>> WEB SOCKET', 'font-weight: bold');
        Transition.log(transition);

        for (let command of transition.commands) {
            await Transition.executeCommand(transition, command, null);
        }
    }

    static async execute(transition, componentEvent) {
        log.debug('');
        log.debug('%c<<< RESPONSE /' + componentEvent.controller + '/' + componentEvent.action, 'font-weight: bold');
        Transition.log(transition);

        if (!transition.commands) {
            log.error('Server side error, please verify the server logs!');
            return;
        }

        if (!transition.commands.length) {
            LoadingScreen.show(false);
        }

        for (let command of transition.commands) {
            await Transition.executeCommand(transition, command, componentEvent);
        }
    }

    static async executeCommand(transition, command, componentEvent) {
        let $element = Transition.getTargetElement(command.component);
        let component = Elements.getByElement($element);
        let componentId = command.component;
        let method = command.method;
        let property = command.property;
        let valueMap = command.value;
        let trigger = command.trigger;
        let $components = transition.$components;

        log.trace('EXECUTING: ' + componentId + ' ' + method + ' ' + property);

        switch (method) {
            case TransitionCommand.REDIRECT:
                return TransitionCommand.redirect(valueMap.value);

            case TransitionCommand.RENDER_CONTENT:
                return TransitionCommand.renderContent($components, componentEvent);

            case TransitionCommand.LOADING:
                TransitionCommand.loading(valueMap.value);
                break;

            case TransitionCommand.DELAY:
                await TransitionCommand.delay(valueMap.value);
                break;

            case TransitionCommand.APPEND:
                TransitionCommand.append($element, componentId, valueMap.value, $components);
                break;

            case TransitionCommand.REPLACE:
                TransitionCommand.replace($element, componentId, valueMap.value, $components);
                break;

            case TransitionCommand.REMOVE:
                TransitionCommand.remove($element, componentId);
                break;

            case TransitionCommand.TRIGGER:
                TransitionCommand.trigger($element, componentId, property);
                break;

            case TransitionCommand.CALL:
                TransitionCommand.call($element, componentId, component, property, valueMap.value, $components);
                break;

            case TransitionCommand.SET:
                TransitionCommand.set($element, componentId, component, property, valueMap, trigger);
                break;

            default:
                log.error('Invalid transition command "' + command.method + '"');
                log.error(JSON.stringify(command));
        }
    }

    static getTargetElement(componentId) {
        if (!componentId) {
            return $();
        }

        const roots = {
            page: Page.$self,
            messagebox: PageMessageBox.$self,
            modal: PageModal.$self,
            content: $.merge(PageContent.$self, PageStickyBox.$self),
        };

        const parts = componentId.split('.');

        let $root;

        // A recognized root is handled separately.
        if (roots[parts[0]]) {
            $root = roots[parts.shift()];
        } else {
            $root = PageModal.isActive
                ? PageModal.$self
                : $.merge(PageContent.$self, PageStickyBox.$self);
        }

        for (let i = 0; i < parts.length; i++) {
            const id = parts.slice(i).join('.');

            // First try the complete remaining ID.
            const $component = $root.find('[data-21-id="' + CSS.escape(id) + '"]');

            if ($component.length) {
                return $component;
            }

            // Move into the first component and continue searching.
            $root = $root.find('[data-21-id="' + CSS.escape(parts[i]) + '"]');

            if (!$root.length) {
                break;
            }
        }

        log.error(`Cannot find component "${componentId}"`);
        return $();
    }

    static triggerEvent($element, eventName, async = true) {
        let componentEvent = Component.getEvent($element, eventName);
        Transition.submit(componentEvent, async);
    }

    static submit(componentEvent, async = true, silentFail = false) {
        if (!componentEvent) {
            return;
        }

        let url = Transition.buildUrl(componentEvent);
        if (!url) {
            return;
        }

        if (componentEvent['loading'] != null) {
            LoadingScreen.show(componentEvent['loading']);
        }

        if (componentEvent['direct']) {
            let queryString = Transition.buildQueryString(componentEvent)
            window.location.assign(url + queryString);
            return;
        }

        let renderProperties = componentEvent['renderProperties'];
        if (renderProperties) {
            // We don't update the URL on mobile devices to avoid
            // the display of the browser toolbar
            renderProperties['updateUrl'] = !Elements.isMobileDevice;
        }

        let values = Transition.build21Params(componentEvent);
        Transition.call(url, values, componentEvent, async, silentFail);
    }

    static buildUrl(componentEvent, values = null) {
        if (!componentEvent)
            return null;

        if (componentEvent.url) {
            return componentEvent.url;

        } else {
            let uri = componentEvent.controller + '/' + componentEvent.action;
            return _21_.app.url + uri;
        }
    }

    static buildQueryString(componentEvent) {
        let values = Transition.build21Params(componentEvent);
        let queryString = '?' + new URLSearchParams(values).toString();
        let params = new URLSearchParams(componentEvent['params']).toString();
        if (params) queryString += '&' + params;

        return queryString
    }

    static build21Params(componentEvent) {
        let _21Params = {};
        let components = {};  // Component controls value
        let params = {};      // Strings, Numbers, Boolean or eg. {type: 'ID', value: 1}

        if (componentEvent) {
            // CONTROL OR COMPONENT CONTROLS VALUES
            if (componentEvent['submit']) {
                let componentIds = componentEvent['submit'];
                for (let componentId of componentIds) {
                    let $component = Transition.getTargetElement(componentId);
                    let control = Control.getByElement($component)
                    let component = Component.getByElement($component);

                    if (control) {
                        components[componentId] = { [componentId]: control.getValue($component) };
                        components[componentId]._21SubmittedName = componentId;

                    } else if (component) {
                        components[componentId] = component.getValues($component);
                        components[componentId]._21SubmittedName = componentId;
                    }
                }
            }

            // PARAMS
            if (componentEvent['params']) {
                let eventParams = Object.entries(componentEvent['params']);
                for (let [key, value] of eventParams) {
                    params[key] = value;
                }
            }

            // Whether to display the whole page or the transition
            if (!componentEvent['direct']) {
                params['_21IsMobileDevice'] = Elements.isMobileDevice;
                params['_21Transition'] = true;
                params['_21Event'] = componentEvent['event'];
            }
        }

        _21Params.components = components;
        _21Params.params = params;

        return {_21Params: JSON.stringify(_21Params)};
    }

    static call(url, values, componentEvent, async, silentFail) {
        log.debug('');
        log.debug('%c>>> REQUEST ' + url, 'font-weight: bold');
        log.debug('VALUES:');
        log.debug(JSON.stringify(JSON.parse(values._21Params), null, 2));

        Transition.ajaxCall(url, values, componentEvent, async, silentFail);
    }

    static ajaxCall(url, values, componentEvent, async, silentFail) {
        let call = {
            type: 'POST',
            url: url,
            data: values,
            async: async,
        }

        $.ajax(call)
            .done(function (data, textStatus, xhr) {
                let $response = $(data);
                let isTransition = $response.data('21-component') == 'Transition';

                if (isTransition) {
                    let transition = Transition.fromHtml(data);
                    Transition.execute(transition, componentEvent);

                } else { // It's a full page
                    TransitionCommand.renderPage($response, componentEvent);
                }
            })
            .fail(function (xhr, textStatus, errorThrown) {
                LoadingScreen.show(false);

                if (silentFail == true) {
                    return;
                }

                if (xhr.readyState === 0) {
                    PageMessageBox.error(null, {infoMessage: 'Unable to connect to server.'});
                    return;
                }

                switch (xhr.status) {
                    case 401: // Unauthorized
                        window.location.replace(_21_.app.url + 'logout');
                        return;

                    case 403: // Access denied
                        PageMessageBox.error(null, {infoMessage: JSON.parse(xhr.responseText).error});
                        return;

                    case 404: // Not Found
                    case 500: // Server Error
                        if (componentEvent.timerId) {
                            PageMessageBox.error(null, {infoMessage: 'Internal server error: ' + xhr.status});
                        } else {
                            let $content = $(xhr.responseText);
                            TransitionCommand.renderContent($content, componentEvent);
                        }
                        return;

                    default:
                        PageMessageBox.error(null, {infoMessage: 'Cannot execute call: ' + xhr.status});
                }
            });
    }

    static fromHtml(html, componentEvent = null) {
        let $html = $(html);
        if ($html.data('21-component') != 'Transition') {
            log.error("BUG: No transition found!");
            return;
        }

        let commands = $html.find('[data-21-commands]').data('21-commands');
        let $components = $html.find('[data-21-components]');
        let transition = {
            commands: commands,
            $components: $components,
        }

        return transition;
    }

    static log(transition) {
        if (!_21_.log?.debug) {
            return;
        }

        let hasComponents = transition.$components && transition.$components.children().length;
        if (hasComponents) {
            log.debug('COMPONENTS:');
            for (let element of transition.$components.children().children()) {
                log.debug(element.cloneNode(true));
            }
        }

        let commands = JSON.stringify(transition.commands, null, 2)
        log.debug('COMMANDS:');
        log.debug(commands);
    }
}
