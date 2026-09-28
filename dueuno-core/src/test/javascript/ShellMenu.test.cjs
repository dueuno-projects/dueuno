/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');

for (const moduleName of ['dueuno-core', 'app-test']) {
    test(`${moduleName}: the current controller determines the persistent menu item`, () => {
        const content = {};
        let controller = 'form';
        const links = [{ controller: 'form' }, { controller: 'crud' }, { controller: 'form' }, {}];
        function wrap(items) {
            return {
                items,
                removeClass() { items.forEach(item => item.active = false); return this; },
                removeAttr() { items.forEach(item => delete item.current); return this; },
                filter(predicate) { return wrap(items.filter(item => predicate.call(item))); },
                first() { return wrap(items.slice(0, 1)); },
                addClass() { items.forEach(item => item.active = true); return this; },
                attr(name, value) { items.forEach(item => item.current = value); return this; },
            };
        }
        class Component {
            static register() {}
            static getProperty(element, name) {
                assert.equal(element, content, 'use the main page, not the active modal');
                assert.equal(name, 'controller');
                return controller;
            }
            static getEvent(element) { return element.items[0]; }
        }
        const context = vm.createContext({
            Component,
            PageContent: { $self: content },
            $: value => wrap(typeof value === 'string' ? links : [value]),
        });
        const source = path.resolve(__dirname, '../../../..', moduleName,
            'grails-app/assets/dueuno/elements/pages/ShellMenu.js');
        vm.runInContext(fs.readFileSync(source, 'utf8'), context);
        const ShellMenu = vm.runInContext('ShellMenu', context);

        for (const [nextController, expected] of [['form', 0], ['crud', 1], ['crud', 1], ['missing', -1], [null, -1]]) {
            controller = nextController;
            ShellMenu.updateActiveItem();
            links.forEach((item, index) => {
                assert.equal(item.active, index === expected);
                assert.equal(item.current, index === expected ? 'page' : undefined);
            });
        }
    });
}

for (const moduleName of ['dueuno-core', 'app-test']) {
    test(`${moduleName}: content focus skips unusable controls and falls back to the content`, () => {
        const document = { activeElement: null };
        const disabled = { disabled: true };
        const unfocusable = { contains: () => false };
        const input = { focusable: true, contains: () => false };
        let controls = [disabled, unfocusable, input];
        let tabindex;
        const content = {
            find: () => ({ filter: () => controls }),
            attr(name, value) { tabindex = value; },
        };
        const context = vm.createContext({
            document,
            Component: class {
                static register() {}
                static setFocus(target, enabled) {
                    assert.equal(enabled, true);
                    if (target === content || target.focusable) document.activeElement = target;
                }
            },
            Control: { getByElement: value => value },
            Elements: { callMethod: (element, control, method) => method === 'getDisplay' || !!element.disabled },
            $: value => value,
        });
        const source = path.resolve(__dirname, '../../../..', moduleName,
            'grails-app/assets/dueuno/elements/pages/ShellMenu.js');
        vm.runInContext(fs.readFileSync(source, 'utf8'), context);
        const ShellMenu = vm.runInContext('ShellMenu', context);
        ShellMenu.focusContent(content);
        assert.equal(document.activeElement, input);
        assert.equal(tabindex, undefined);
        controls = [disabled, unfocusable];
        ShellMenu.focusContent(content);
        assert.equal(document.activeElement, content);
        assert.equal(tabindex, '-1');
    });

    for (const closingMenu of [false, true]) {
        test(`${moduleName}: focus follows content replacement and menu dismissal (${closingMenu})`, () => {
            const oldContent = {};
            const content = { 0: {}, length: 1 };
            let pending = oldContent;
            let hidden;
            let frame;
            let focused;
            const menu = {
                data: () => pending,
                removeData() { pending = undefined; },
                is: () => closingMenu,
                one(event, callback) {
                    assert.equal(event, 'hidden.bs.offcanvas');
                    hidden = callback;
                },
            };
            const pageContent = { $self: { 0: oldContent, length: 1 } };
            const modal = { isActive: false };
            const context = vm.createContext({
                Component: class { static register() {} },
                PageContent: pageContent,
                PageModal: modal,
                $: () => menu,
                requestAnimationFrame: callback => frame = callback,
            });
            const source = path.resolve(__dirname, '../../../..', moduleName,
                'grails-app/assets/dueuno/elements/pages/ShellMenu.js');
            vm.runInContext(fs.readFileSync(source, 'utf8'), context);
            const ShellMenu = vm.runInContext('ShellMenu', context);
            ShellMenu.focusContent = value => focused = value;
            ShellMenu.focusContentAfterNavigation();
            assert.equal(frame, undefined, 'do not focus the old page');
            pageContent.$self = content;
            modal.isActive = true;
            ShellMenu.focusContentAfterNavigation();
            assert.equal(frame, undefined, 'do not take focus from a modal');
            modal.isActive = false;
            ShellMenu.focusContentAfterNavigation();
            assert.equal(focused, undefined);
            assert.equal(pending, undefined);
            if (closingMenu) {
                assert.equal(frame, undefined);
                hidden();
            }
            frame();
            assert.equal(focused, content);
        });
    }
}

for (const moduleName of ['dueuno-core', 'app-test']) {
    for (const [inside, keyboardFocus, hasActive, alreadyActive, shouldMove] of [
        [false, true, true, false, true],
        [true, true, true, false, false],
        [false, false, true, false, false],
        [false, true, false, false, false],
        [false, true, true, true, false],
    ]) {
        test(`${moduleName}: entering menu (${inside}, ${keyboardFocus}, ${hasActive}, ${alreadyActive})`, () => {
            let focused;
            const target = {};
            const active = { 0: alreadyActive ? target : {}, length: hasActive ? 1 : 0 };
            const items = {
                data: () => false,
                is: () => false,
                has: () => ({ length: inside ? 1 : 0 }),
                find: () => ({ filter: () => ({ first: () => active }) }),
            };
            const context = vm.createContext({
                Component: class {
                    static register() {}
                    static setFocus(element, enabled) {
                        assert.equal(enabled, true);
                        focused = element;
                    }
                },
                $: value => value === target ? { is: () => keyboardFocus } : items,
            });
            const source = path.resolve(__dirname, '../../../..', moduleName,
                'grails-app/assets/dueuno/elements/pages/ShellMenu.js');
            vm.runInContext(fs.readFileSync(source, 'utf8'), context);
            const ShellMenu = vm.runInContext('ShellMenu', context);
            ShellMenu.onMenuFocusIn({ currentTarget: items, target, relatedTarget: {} });
            assert.equal(focused, shouldMove ? active : undefined);
        });
    }
}

for (const moduleName of ['dueuno-core', 'app-test']) {
    for (const [type, key, shiftKey, index, expected] of [
        ['keydown', 'Tab', true, 2, 2],
        ['keydown', 'Tab', true, 0, 0],
        ['keydown', 'Tab', true, -1, undefined],
        ['keydown', 'Tab', false, 2, undefined],
        ['keydown', 'a', false, 2, undefined],
        ['pointerdown', undefined, false, 2, undefined],
    ]) {
        test(`${moduleName}: initial content interaction (${type}, ${key}, ${shiftKey}, ${index})`, () => {
            let pending = true;
            let focused;
            let prevented = false;
            const document = {};
            const chain = { off() {}, addBack() { return this; } };
            const content = {
                data: () => pending,
                removeData() { pending = false; },
                find: () => chain,
            };
            const links = {
                filter() { return this; }, first() { return this; },
                index: () => index, eq: value => value,
            };
            const items = {
                find: () => links,
            };
            const context = vm.createContext({
                document, PageContent: { $self: content }, PageModal: { isActive: false },
                Component: class {
                    static register() {}
                    static setFocus(target) {
                        focused = target;
                    }
                },
                $: value => value === document ? chain
                    : value === '#shell-menu' ? { css: () => 'visible' }
                    : value === '#shell-menu-items' ? items
                    : { closest: () => ({ length: 1 }) },
            });
            const source = path.resolve(__dirname, '../../../..', moduleName,
                'grails-app/assets/dueuno/elements/pages/ShellMenu.js');
            vm.runInContext(fs.readFileSync(source, 'utf8'), context);
            const ShellMenu = vm.runInContext('ShellMenu', context);
            ShellMenu.onInitialContentInteraction({ type: 'keydown', key: 'Shift' });
            assert.equal(pending, true);
            const event = { type, key, shiftKey, target: {},
                preventDefault() { prevented = true; }, stopImmediatePropagation() {} };
            ShellMenu.onInitialContentInteraction(event);
            assert.equal(focused, expected);
            assert.equal(prevented, expected !== undefined);
            assert.equal(pending, false);
            focused = undefined;
            ShellMenu.onInitialContentInteraction(event);
            assert.equal(focused, undefined, 'apply only to the first interaction');
        });
    }
}

