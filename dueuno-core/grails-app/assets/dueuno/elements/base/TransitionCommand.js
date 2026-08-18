class TransitionCommand {

    // Temporary solutions until we get support for static fields
    // See: https://github.com/google/closure-compiler/issues/2731
    static get REDIRECT() { return 'REDIRECT' }
    static get RENDER_CONTENT() { return 'RENDER_CONTENT' }
    static get APPEND() { return 'APPEND' }
    static get REPLACE() { return 'REPLACE' }
    static get REMOVE() { return 'REMOVE' }
    static get TRIGGER() { return 'TRIGGER' }
    static get LOADING() { return 'LOADING' }
    static get DELAY() { return 'DELAY' }
    static get CALL() { return 'CALL' }
    static get SET() { return 'SET' }

    static async redirect(componentEvent) {
        if (PageModal.isActive && !componentEvent.renderProperties['modal']) {
            PageModal.close();
            await sleep(100); // We give time for the animations to start
        }

        Transition.submit(componentEvent, false);
    }

    static renderPage($newPageContent, componentEvent) {
        LoadingScreen.show(false);

        let $newPage = $newPageContent.find('[data-21-component="PageContent"]');
        if (!$newPage.exists()) {
            return;
        }

        PageMessageBox.hide();
        PageModal.close();

        let $page = $('[data-21-component="PageContent"]');
        TransitionCommand.render($page, $newPage, componentEvent);
    }

    static renderContent($components, componentEvent) {
        LoadingScreen.show(false);

        let $content = $components.find('#page-content');
        if (!$content) {
            log.error("Cannot find a 'Content' component in transition.");
            return;
        }

        let contentRenderProperties = Component.getProperty($content, 'renderProperties');
        componentEvent.renderProperties = TransitionCommand.mergeRenderProperties(componentEvent.renderProperties, contentRenderProperties);

        let rendered;
        if (componentEvent.renderProperties['modal']) {
            rendered = PageModal.open($content, componentEvent);

        } else {
            PageModal.close();
            TransitionCommand.render(PageContent.$self, $content, componentEvent);
        }

        if (PageMessageBox.isActive) {
            PageMessageBox.hide();
        }

        return rendered;
    }

    static render($component, $newComponent, componentEvent) {
        let focusPath = TransitionCommand.getFocusPath();
        let animation = componentEvent.renderProperties['animate'];
        TransitionCommand.animate(animation, $component, $newComponent)

        Page.deactivateComponents();
        $component.replaceWith($newComponent);
        Page.reinitializeContent($newComponent);
        TransitionCommand.restoreFocus(focusPath);

        TransitionCommand.scrollTo($newComponent, componentEvent);
        if (componentEvent.renderProperties['updateUrl']) {
            let url = Transition.buildUrl(componentEvent);
            TransitionCommand.setBrowserUrl(url);
        }
    }

    static scrollTo($content, componentEvent) {
        let scroll = componentEvent.renderProperties['scroll'];
        if (scroll == 'reset') {
            window.scrollTo({top:0, left:0, behavior: 'instant'});

        } else if (scroll == 'top') {
            window.scrollTo({
                top:0,
                left:0,
                behavior: _21_.user.animations ? 'smooth' : 'instant',
            });

        } else { // Scroll to top of the specified component
            let $element = $content.find('[data-21-id="' + scroll + '"]');
            if ($element.exists()) {
                let position = $element.position();
                window.scrollTo({
                    top: position.top,
                    left: position.left,
                    behavior: _21_.user.animations ? 'smooth' : 'instant',
                });
            }
        }
    }

    static loading(show) {
        LoadingScreen.show(show);
    }

    static append($element, componentId, newComponentId, $components) {
        let $component = $components.find('[data-21-id="' + newComponentId + '"]');
        if (!$component) {
            log.error("Cannot find component '" + newComponentId + "' in transition components payload.")
            return;
        }

        if (!$element.exists()) {
            log.error('Cannot append to component "' + componentId + '" (element not found)');
            return;
        }

        let $parent = $element.closest('[data-21-component]').parent();
        $parent.append($component);
        Page.reinitializeContent($component);
    }

    static replace($element, componentId, newComponentId, $components) {
        let $component = $components.find('[data-21-id="' + newComponentId + '"]');
        if (!$component) {
            log.error("Cannot find component '" + newComponentId + "' in transition components payload.")
            return;
        }

        if (!$element.exists()) {
            log.error('Cannot replace component "' + componentId + '" (element not found)');
            return;
        }

        let focusPath = TransitionCommand.getFocusPath();
        $element.replaceWith($component);
        Page.reinitializeContent($component);
        TransitionCommand.restoreFocus(focusPath);
    }

    static getFocusPath() {
        let $pageContent = PageContent.$self;
        let $activeElement = $pageContent.find(':focus').addBack(':focus');
        if (!$activeElement.exists()) {
            return null;
        }

        let path = [];
        let $element = $activeElement.closest('[data-21-id]');
        while ($element.exists()) {
            if (!$pageContent.is($element[0]) && !$pageContent.has($element[0]).length) {
                break;
            }

            path.unshift($element.data('21-id'));
            if ($pageContent.is($element[0])) {
                break;
            }

            $element = $element.parent().closest('[data-21-id]');
        }

        return path;
    }

    static restoreFocus(path) {
        if (!path || !path.length) {
            return;
        }

        let $element = Transition.getTargetElement(path.join('.'));
        Component.setFocus($element, true);
    }

    static remove($element, componentId) {
        if (!$element.exists()) {
            log.error('Cannot remove component "' + componentId + '" (element not found)');
            return;
        }

        $element.remove();
    }

    static trigger($element, componentId, eventName) {
        if (!$element.exists()) {
            log.error('Cannot trigger event "' + eventName + '" for component "' + componentId + '" (element not found)');
            return;
        }

        Transition.triggerEvent($element, eventName);
    }

    static async call($element, componentId, component, property, value, $components) {
        await sleep(100); // We give time for the animations to start

        let $component
        if ($components && value.component) {
            $component = $components.find('[data-21-id="' + value.component + '"]');
        }

        if (Elements.hasMethod(component, property)) {
            Elements.callMethod($element, component, property, value, $component);
        } else {
            log.error('Cannot find method "' + componentId + '.' + property + '()"');
        }
    }

    static set($element, componentId, component, property, value, trigger) {
        let methodName = 'set' + capitalize(property);
        if (!Elements.hasMethod(component, methodName)) {
            log.error('Cannot find method "' + componentId + '.' + methodName + '()"');
            return;
        }

        if (methodName == 'setValue') {
            Elements.callMethod($element, component, 'setValue', value, trigger);
            if (trigger) Transition.triggerEvent($element, 'change');

        } else {
            Elements.callMethod($element, component, methodName, value.value);
        }
    }

    static mergeRenderProperties(eventRenderProperties, contentRenderProperties) {
        let merged = {};

        if (contentRenderProperties) {
            for (let [key, value] of Object.entries(contentRenderProperties)) {
                if (value != null) merged[key] = value;
            }
        }

        if (eventRenderProperties) {
            for (let [key, value] of Object.entries(eventRenderProperties)) {
                if (value != null) merged[key] = value;
            }
        }

        if (!merged.hasOwnProperty('closeButton')) {
            merged['closeButton'] = true;
        }

        return merged;
    }

    static setBrowserUrl(url) {
        history.pushState({}, '', url);
    }

    static animate(animation, $prevElement, $nextElement) {
        if (!_21_.user.animations || !animation) {
            return;
        }

        switch (animation) {
            case 'fade':
                $prevElement.addClass('fade-out');
                $nextElement.addClass('fade-in');
                break;

            case 'back':
                break;

            case 'next':
                break;
        }
    }
}
