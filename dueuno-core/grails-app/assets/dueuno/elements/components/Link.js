//= require Label

class Link extends Label {

    static finalize($element, $root) {
        $element.off('click').on('click', Link.onClick);
        KeyPress.registerHotkey($element);
    }

    static hasTarget(componentEvent) {
        return componentEvent && componentEvent['target'] && componentEvent['target'] != '_self';
    }

    static executeCall(call) {
        let context = window;
        let path = call.split('.');
        let functionName = path.pop();

        for (let property of path) {
            context = context?.[property];
            if (context == null) {
                break;
            }
        }

        let functionToCall = context?.[functionName];
        if (typeof functionToCall === 'function') {
            functionToCall.call(context);
        } else {
            log.error("Cannot find client-side function '" + call + "'.");
        }
    }

    static onClick(event) {
        event.preventDefault();

        let $element = $(event.currentTarget);
        if (Component.getReadonly($element)) {
            return;
        }

        // No event defined
        let componentEventNotToBeAltered = Component.getEvent($element, 'click');
        if (!componentEventNotToBeAltered) {
            return;
        }

        // We copy the defined event to not alter the DOM
        let componentEvent = { ...componentEventNotToBeAltered };

        // CRTL + Click to open on a new tab
        if (event.metaKey || event.ctrlKey) {
            componentEvent['target'] = '_blank';
            componentEvent['direct'] = true;
        }

        // Open a on a different target tab
        if (!componentEvent['call'] && Link.hasTarget(componentEvent)) {
            let url = Transition.buildUrl(componentEvent);
            let queryString = Transition.buildQueryString(componentEvent);
            window.open(url + queryString, componentEvent['target']);
            return;
        }

        if (componentEvent['confirmMessage']) {
            let confirmEventData = Object.assign({}, componentEvent);
            PageMessageBox.confirm(null, {
                confirmMessage: confirmEventData.confirmMessage,
                clickConfirm: confirmEventData,
            });

        } else if (componentEvent['infoMessage']) {
            PageMessageBox.info(null, componentEvent);

        } else if (componentEvent['call']) {
            Link.executeCall(componentEvent['call']);

        } else {
            if (componentEvent.renderProperties) {
                componentEvent.renderProperties['updateUrl'] = true;
            }

            componentEvent['loading'] = componentEvent['loading'] == null ? true : componentEvent['loading'];
            componentEvent['direct'] = componentEvent['direct'] == null ? false : componentEvent['direct'];
            Transition.submit(componentEvent);
        }
    }

}

Component.register(Link);
