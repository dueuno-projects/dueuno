// Temporary solutions until we get support for static fields
// See: https://github.com/google/closure-compiler/issues/2731
let Elements_pages = new Map();
let Elements_components = new Map();
let Elements_controls = new Map();
let Elements_isMobileDevice = false;

class Elements {

    static get pages() { return Elements_pages }
    static get components() { return Elements_components }
    static get controls() { return Elements_controls }
    static get isMobileDevice() { return Elements_isMobileDevice }

    static main() {
        log.debug('-');
        log.debug('URL: ' + _21_.app.url);
        log.debug('Path: ' + _21_.app.path);
        log.debug('Tenant: ' + _21_.app.tenant);

        let os = getOS();
        Elements_isMobileDevice = (os == 'iOS' || os == 'Android');
        log.debug('Operating System: ' + os);
        log.debug('Is Mobile Device: ' + Elements_isMobileDevice);
        log.debug('-');

        Page.render();
    }

    static getComponentId($element) {
        return $element.data('21-id');
    }

    static getComponentById(componentId) {
        let $element = Elements.getElementById(componentId);
        return Elements.getComponent($element);
    }

    static getElementById(componentId, $root) {
        if (!$root) {
            $root = Page.$self;
        }

        return $root.find('[data-21-id="' + componentId + '"]');
    }

    static getElement(componentId) {
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

            // The component ID is exactly the root.
            if (parts.length === 0) {
                return $root;
            }

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

    static getComponent($element) {
        return Control.getByElement($element)
            ?? Component.getByElement($element)
            ?? Page.get();
    }

    static getClassName($element) {
        return Control.getClassName($element)
            ?? Component.getClassName($element)
            ?? Page.getClassName();
    }

    static hasMethod(clazz, methodName) {
        if (!clazz || !methodName) {
//            log.error('Elements.hasMethod(' + [...arguments] + '), class or method not specified');
            return false;
        }

        let exists = (typeof clazz[methodName] === 'function');
        return exists;
    }

    static callMethod($element, clazz, methodName, ...args) {
        if (!Elements.hasMethod(clazz, methodName)) {
            return;
        }

        args.unshift($element);
        log.trace('CALLING "' + clazz.name + '.' + methodName + '(' + Elements.displayArgs(args) + ')"');
        return clazz[methodName].apply(clazz, args);
    }

    static displayArgs(args) {
        let result = [];

        for (let obj of args) {
            if (obj === undefined) {
                continue;
            }

            if (obj instanceof jQuery) {
                result.push(obj.data('21-id'));

            } else if (obj !== null && typeof obj === 'object') {
                result.push(JSON.stringify(obj));

            } else {
                result.push(obj);
            }
        }

        return result.join(', ');
    }

}
