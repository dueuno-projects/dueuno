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
    test(`${moduleName}: keeps initial focus on the dialog and hides page shortcuts`, () => {
        const root = {};
        let tabindex;
        let focused;
        let shortcutsVisible = true;
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
            KeyPress: {
                setShortcutsVisible(visible) {
                    shortcutsVisible = visible;
                },
                isShortcutModifierPressed(event) {
                    return event.ctrlKey || event.metaKey;
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
        assert.equal(shortcutsVisible, false);
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
            ['Enter', {}, false, true, true],
            ['Tab', root, false, false, false],
        ]) {
            let prevented = false;
            PageMessageBox.onKeyDown({ key, target, metaKey, ctrlKey,
                preventDefault() { prevented = true; }, stopImmediatePropagation() {},
            });
            assert.equal(prevented, blocked);
        }
    });

    test(`${moduleName}: hides and disables page shortcuts while a messagebox is active`, () => {
        let shortcutsVisible;
        const context = vm.createContext({
            Component: class {
                static register() {}
            },
            PageMessageBox: { isActive: true },
            $: selector => ({
                toggleClass(name, visible) {
                    assert.equal(selector, 'body');
                    assert.equal(name, 'keyboard-shortcuts-visible');
                    shortcutsVisible = visible;
                },
            }),
        });
        const source = path.resolve(__dirname, '../../../..', moduleName,
            'grails-app/assets/dueuno/elements/base/KeyPress.js');
        vm.runInContext(fs.readFileSync(source, 'utf8'), context);
        const KeyPress = vm.runInContext('KeyPress', context);

        KeyPress.setShortcutsVisible(true);
        assert.equal(shortcutsVisible, false);
        assert.equal(KeyPress.onShortcut({repeat: false}), false);
    });
}
