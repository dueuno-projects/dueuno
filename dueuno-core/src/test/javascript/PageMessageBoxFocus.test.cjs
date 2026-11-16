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
    test(`${moduleName}: keeps initial focus on the dialog and hides page hotkeys`, () => {
        const root = {};
        let tabindex;
        let focused;
        let hotkeysVisible = true;
        const dialog = { 0: root, attr(name, value) {
            assert.equal(name, 'tabindex');
            tabindex = value;
            return this;
        } };
        const context = vm.createContext({
            Component: class {
                static register() {}
                static setFocus(target, enabled) {
                    assert.equal(tabindex, '-1');
                    assert.equal(enabled, true);
                    focused = target;
                }
            },
            PageKeyPress: {
                setHotkeysVisible(visible) {
                    hotkeysVisible = visible;
                },
                isHotkeyModifierPressed(event) {
                    return event.metaKey;
                },
                initializeKeyboardEvents() {},
            },
            $: () => dialog,
        });
        const source = path.resolve(__dirname, '../../../..', moduleName,
            'grails-app/assets/dueuno/elements/base/PageMessageBox.js');
        vm.runInContext(fs.readFileSync(source, 'utf8'), context);
        const PageMessageBox = vm.runInContext('PageMessageBox', context);
        PageMessageBox.dialog = { show() {} };
        PageMessageBox.show();
        assert.equal(focused, dialog);
        assert.equal(hotkeysVisible, false);
        tabindex = undefined;
        PageMessageBox.onShown({});
        assert.equal(focused, dialog);
        PageMessageBox.isActive = true;
        PageMessageBox.onHidden({});
        assert.equal(PageMessageBox.isActive, false);
        for (const [key, target, metaKey, ctrlKey, blocked] of [
            ['Enter', root, false, false, true],
            ['Enter', {}, false, false, false],
            ['Enter', {}, true, false, true],
            ['Enter', {}, false, true, false],
            ['Tab', root, false, false, false],
        ]) {
            let prevented = false;
            PageMessageBox.onKeyDown({ key, target, metaKey, ctrlKey,
                preventDefault() { prevented = true; }, stopImmediatePropagation() {},
            });
            assert.equal(prevented, blocked);
        }
    });

    test(`${moduleName}: uses platform-specific hotkey modifiers`, () => {
        for (const isLinux of [false, true]) {
            let hotkeysVisible;
            const context = vm.createContext({
                Component: class { static register() {} },
                PageMessageBox: { isActive: false },
                Utils: { getOS: () => isLinux ? 'Linux' : 'Mac' },
                $: () => ({ toggleClass(name, visible) { hotkeysVisible = visible; } }),
            });
            const source = path.resolve(__dirname, '../../../..', moduleName,
                'grails-app/assets/dueuno/elements/base/PageKeyPress.js');
            vm.runInContext(fs.readFileSync(source, 'utf8'), context);
            const PageKeyPress = vm.runInContext('PageKeyPress', context);

            for (const key of ['F1', 'F12', 'Enter']) {
                for (const modifiers of [
                    {}, { ctrlKey: true }, { altKey: true }, { shiftKey: true },
                    { metaKey: true }, { ctrlKey: true, altKey: true },
                    { metaKey: true, ctrlKey: true, altKey: true },
                ]) {
                    const event = { key, metaKey: false, ctrlKey: false, altKey: false, ...modifiers };
                    const expected = isLinux
                        ? event.ctrlKey && event.altKey
                        : event.metaKey;
                    assert.equal(PageKeyPress.matchesHotkey(key, event), expected);
                    PageKeyPress.onKeyUp(event);
                    assert.equal(hotkeysVisible, expected);
                }
            }
        }
    });

    test(`${moduleName}: labels F13–F24 as SHIFT + F1–F12`, () => {
        let hotkey;
        const attributes = {};
        const element = {
            0: {},
            removeAttr(name) { delete attributes[name]; },
            attr(name, value) { attributes[name] = value; },
        };
        const context = vm.createContext({
            Component: class {
                static register() {}
                static getProperty() { return hotkey; }
            },
        });
        const source = path.resolve(__dirname, '../../../..', moduleName,
            'grails-app/assets/dueuno/elements/base/PageKeyPress.js');
        vm.runInContext(fs.readFileSync(source, 'utf8'), context);
        const PageKeyPress = vm.runInContext('PageKeyPress', context);
        for (let i = 1; i <= 24; i++) {
            hotkey = 'F' + i;
            PageKeyPress.registerHotkey(element);
            assert.equal(attributes['data-21-hotkey-label'],
                i > 12 ? 'SHIFT + F' + (i - 12) : hotkey);
            assert.equal(PageKeyPress.hotkeys.get(hotkey.toLowerCase()), element[0]);
        }
        hotkey = 'Enter';
        PageKeyPress.registerHotkey(element);
        assert.equal(attributes['data-21-hotkey-label'], 'Enter');
        const css = path.resolve(__dirname, '../../../..', moduleName,
            'grails-app/assets/dueuno/elements/components/Link.css');
        assert.match(fs.readFileSync(css, 'utf8'), /content: attr\(data-21-hotkey-label\);/);
    });

    test(`${moduleName}: Ctrl + Alt + Shift + F1–F12 dispatches F13–F24 on Linux`, () => {
        let registeredHotkey;
        let clicks = 0;
        let prevented = 0;
        const link = {
            is() { return false; },
            trigger(name) { assert.equal(name, 'click'); clicks++; },
        };
        const context = vm.createContext({
            Component: class {
                static register() {}
                static getProperty(element, name) {
                    return name == 'hotkey' ? registeredHotkey : false;
                }
                static getDisplay() { return true; }
                static getVisible() { return true; }
            },
            PageMessageBox: { isActive: false },
            Utils: { getOS: () => 'Linux' },
            document: { documentElement: { contains() { return true; } } },
            $: () => link,
        });
        const source = path.resolve(__dirname, '../../../..', moduleName,
            'grails-app/assets/dueuno/elements/base/PageKeyPress.js');
        vm.runInContext(fs.readFileSync(source, 'utf8'), context);
        const PageKeyPress = vm.runInContext('PageKeyPress', context);

        for (let i = 1; i <= 12; i++) {
            const event = { key: 'F' + i, ctrlKey: true, altKey: true, shiftKey: true,
                preventDefault() { prevented++; }, stopPropagation() {},
            };
            registeredHotkey = 'F' + (i + 12);
            PageKeyPress.hotkeys.clear();
            PageKeyPress.hotkeys.set(registeredHotkey.toLowerCase(), {});
            // Register both ranges so dispatch must select the shifted action.
            PageKeyPress.hotkeys.set(event.key.toLowerCase(), {});
            assert.equal(PageKeyPress.matchesHotkey(event.key, event), false);
            assert.equal(PageKeyPress.matchesHotkey(registeredHotkey, event), true);
            assert.equal(PageKeyPress.onHotkey(event), true);
            assert.equal(PageKeyPress.onHotkey({ ...event, repeat: true }), true);
            assert.equal(PageKeyPress.onHotkey({ ...event, ctrlKey: false }), false);
            assert.equal(PageKeyPress.matchesHotkey(registeredHotkey,
                { ...event, shiftKey: false }), false);
            assert.equal(PageKeyPress.matchesHotkey(registeredHotkey,
                { ...event, key: registeredHotkey, shiftKey: false }), true);
        }
        assert.equal(clicks, 12);
        assert.equal(prevented, 24);
        assert.equal(PageKeyPress.matchesHotkey('Enter',
            { key: 'Enter', ctrlKey: true, altKey: true, shiftKey: true }), true);
    });

    test(`${moduleName}: hides and disables page hotkeys while a messagebox is active`, () => {
        let hotkeysVisible;
        const context = vm.createContext({
            Component: class {
                static register() {}
            },
            PageMessageBox: { isActive: true },
            $: selector => ({
                toggleClass(name, visible) {
                    assert.equal(selector, 'body');
                    assert.equal(name, 'hotkeys-visible');
                    hotkeysVisible = visible;
                },
            }),
        });
        const source = path.resolve(__dirname, '../../../..', moduleName,
            'grails-app/assets/dueuno/elements/base/PageKeyPress.js');
        vm.runInContext(fs.readFileSync(source, 'utf8'), context);
        const PageKeyPress = vm.runInContext('PageKeyPress', context);

        PageKeyPress.setHotkeysVisible(true);
        assert.equal(hotkeysVisible, false);
        assert.equal(PageKeyPress.onHotkey({repeat: false}), false);
    });
}
