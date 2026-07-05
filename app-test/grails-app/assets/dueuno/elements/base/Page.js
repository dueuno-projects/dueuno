//= require Transition

class Page {

    static get $self() { return $('body') }

    static register(clazz) {
        let clazzName = clazz.name;
        log.trace("REGISTERING PAGE '" + clazzName + "'");
        Elements.pages.set(
            clazzName,
            clazz
        );
    }

    static render() {
        Page.detectMobileDevice();
        Page.initializePage();
        Page.initializeContent(Page.$self);
        $(window).on('load', Page.onLoad);
        LoadingScreen.show(false);
    }

    static detectMobileDevice() {
        let url = new URL(window.location.href);

        if (Elements.isMobileDevice && !url.searchParams.has('mobile')) {
            url.searchParams.set('mobile', 'true');
            window.location.replace(url);

        } if (!Elements.isMobileDevice && url.searchParams.has('mobile')) {
            url.searchParams.delete('mobile');
            window.location.replace(url);
        }
    }

    static onLoad(event) {
        Page.finalizeContent(Page.$self);
        Page.finalizePage();
        Page.triggerContentChange();
    }

    static initializeContent($root, reinitialize = false, deactivate = false) {
        // Do not change the sequence unless you know what you are doing
        PageStickyBox.initialize();
        PageTooltips.initialize();
        if (deactivate) {
            Page.deactivateComponents();
        }
        Page.initializeComponents($root, reinitialize);
        Page.initializeControls($root, reinitialize);
        Page.initializeControlValues($root, reinitialize);
    }

    static finalizeContent($root, reinitialize = false) {
        // Do not change the sequence unless you know what you are doing
        PageStickyBox.finalize();
        PageTooltips.finalize();
        Page.finalizeControls($root, reinitialize);
        Page.finalizeComponents($root, reinitialize);
        PageContent.finalize();
    }

    static reinitializeContent($root, reinitialize = false, deactivate = false) {
        Page.initializeContent($root, reinitialize, deactivate);
        Page.finalizeContent($root, reinitialize);
        Page.triggerContentChange();
    }

    static initializePage() {
        if (Elements.isMobileDevice) {
            $(':root').css('--elements-font-size', '16px');
        }

        PageMessageBox.initialize();
        PageModal.initialize();

        let page = Page.get();
        try {
            if (Elements.hasMethod(page, 'initialize')) {
                log.trace("INITIALIZING PAGE '" + page.name + "'");
                Elements.callMethod(Page.$self, page, 'initialize');
            }
        } catch (e) {
            log.error('Error initializing page "' + page.name + '": ' + e);
        }
    }

    static finalizePage() {
        let page = Page.get();
        try {
            if (Elements.hasMethod(page, 'finalize')) {
                log.trace("FINALIZING PAGE '" + page.name + "'");
                Elements.callMethod(Page.$self, page, 'finalize');
            }
        } catch (e) {
            log.error('Error finalizing page "' + page.name + '": ' + e);
        }

        Page.$self.css('visibility', 'visible');
    }

    static triggerContentChange() {
        let page = Page.get();
        try {
            if (Elements.hasMethod(page, 'onContentChange')) {
                log.trace("FINALIZING CONTENT '" + page.name + "'");
                Elements.callMethod(Page.$self, page, 'onContentChange');
            }
        } catch (e) {
            log.error('Error finalizing content for "' + page.name + '": ' + e);
        }
    }

    static deactivateComponents() {
        let $elements = Page.$self.find('[data-21-component][deactivate],[data-21-control][deactivate]');
        for (let element of $elements) {
            let $element = $(element);
            let component = Elements.getByElement($element);

            try {
                if (Elements.hasMethod(component, 'deactivate')) {
                    log.trace("DEACTIVATING COMPONENT '" + component.name + "'");
                    Elements.callMethod($element, component, 'deactivate');
                }
            } catch (e) {
                log.error('Error deactivating component "' + component.name + '": ' + e);
            }

            Elements.callMethod($element, component, 'setDeactivate', false);
        }
    }

    static initializeComponents($root, reinitialize) {
        let $elements = $root.find('[data-21-component]');
        for (let element of $elements) {
            let $element = $(element);
            let component = Component.getByElement($element);
            let properties = Component.getProperties($element);

            let isInitialized = Elements.callMethod($element, component, 'isInitialized');
            if (isInitialized && !reinitialize) {
                continue; // Process next
            }

            try {
                if (Elements.hasMethod(component, 'initialize')) {
                    log.trace("INITIALIZING COMPONENT '" + component.name + "'");
                    Elements.callMethod($element, component, 'initialize', $root);
                }
                if (Elements.hasMethod(component, 'deactivate')) {
                    Component.setDeactivate($element, true);
                }
                Elements.callMethod($element, component, 'setDisplay', properties['display']);
                Elements.callMethod($element, component, 'setVisible', properties['visible']);
                Elements.callMethod($element, component, 'setReadonly', properties['readonly']);
            } catch (e) {
                log.error('Error initializing component "' + component.name + '": ' + e);
            }
        }
    }

    static finalizeComponents($root, reinitialize) {
        let $elements = $root.find('[data-21-component]');
        for (let element of $elements) {
            let $element = $(element);
            let component = Component.getByElement($element);

            let isInitialized = Elements.callMethod($element, component, 'isInitialized');
            if (isInitialized && !reinitialize) {
                continue; // Process next
            }

            try {
                if (Elements.hasMethod(component, 'finalize')) {
                    log.trace("FINALIZING COMPONENT '" + component.name + "'");
                    Elements.callMethod($element, component, 'finalize', $root);
                }
            } catch (e) {
                log.error('Error finalizing component "' + component.name + '": ' + e);
            }

            Elements.callMethod($element, component, 'setInitialized', true);
        }
    }

    static initializeControls($root, reinitialize) {
        let $elements = $root.find('[data-21-control]');
        for (let element of $elements) {
            let $element = $(element);
            let control = Control.getByElement($element);
            let properties = Component.getProperties($element);

            let isInitialized = Elements.callMethod($element, control, 'isInitialized');
            if (isInitialized && !reinitialize) {
                continue; // Process next
            }

            try {
                if (Elements.hasMethod(control, 'initialize')) {
                    log.trace("INITIALIZING CONTROL '" + control.name + "'");
                    Elements.callMethod($element, control, 'initialize', $root);
                }
                if (Elements.hasMethod(control, 'deactivate')) {
                    Component.setDeactivate($element, true);
                }
                Elements.callMethod($element, control, 'setDisplay', properties['display']);
                Elements.callMethod($element, control, 'setVisible', properties['visible']);
                Elements.callMethod($element, control, 'setReadonly', properties['readonly']);
            } catch (e) {
                log.error('Error initializing control "' + control.name + '": ' + e);
            }
        }
    }

    static finalizeControls($root, reinitialize) {
        let $elements = $root.find('[data-21-control]');
        for (let element of $elements) {
            let $element = $(element);
            let control = Control.getByElement($element);

            let isInitialized = Elements.callMethod($element, control, 'isInitialized');
            if (isInitialized && !reinitialize) {
                continue; // Process next
            }

            try {
                if (Elements.hasMethod(control, 'finalize')) {
                    log.trace("FINALIZING CONTROL '" + control.name + "'");
                    Elements.callMethod($element, control, 'finalize', $root);
                }
            } catch (e) {
                log.error('Error finalizing control "' + control.name + '": ' + e);
            }

            Elements.callMethod($element, control, 'setInitialized', true);
        }
    }

    static initializeControlValues($root, reinitialize) {
        let $elements = $root.find('[data-21-control]');
        for (let element of $elements) {
            let $element = $(element);
            let control = Control.getByElement($element);
            let properties = Component.getProperties($element);

            let isInitialized = Elements.callMethod($element, control, 'isInitialized');
            if (isInitialized && !reinitialize) {
                continue; // Process next
            }

            try {
                let value = Elements.callMethod($element, control, 'getServerValue');
                if (value) {
                    Elements.callMethod($element, control, 'setValue', value, false);
                }
            } catch (e) {
                log.error('Error setting value to control "' + control.name + '": ' + e);
            }
        }
    }

    static get() {
        let className = Page.getClassName();
        if (!className) {
            log.error('This is not an Elements page. Please add <body data-21-page="MyPageClass"> ... </body> to make it available.');
            return;
        }

        let page = Elements.pages.get(className);
        if (!page) {
            log.error("No Page registered for class '" + className +
                "'. Register a page class before using it (Eg. Page.register("
                + className + ");");
        }
        return page;
    }

    static getClassName() {
        return Page.$self.data('21-page');
    }

    static getActiveContent() {
        let $content;
        if (PageModal.isActive) {
            $content = PageModal.$self;
        } else {
            $content = PageContent.$self;
        }

        return $content;
    }

    static getController() {
        let $content = Page.getActiveContent();

        let controller = Component.getProperty($content, 'controller');
        return controller;
    }

    static getAction() {
        let $content = Page.getActiveContent();

        let action = Component.getProperty($content, 'action');
        return action;
    }
}