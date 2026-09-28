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
    for (const disabled of [false, true]) {
        for (const target of ['area', 'text', 'input', 'label', 'a', 'button']) {
            test(`${moduleName}: checkbox click on ${target}, disabled=${disabled}`, () => {
                const element = { checked: false };
                const handlers = new Map();
                let focused = false;
                let changes = 0;
                let loads = 0;
                const area = {
                    off(name) { handlers.delete(name); return this; },
                    on(name, data, handler) { handlers.set(name, { data, handler }); return this; },
                };
                const clickArea = clickTarget => {
                    const { data, handler } = handlers.get('click.checkbox');
                    handler({ data, target: clickTarget });
                };
                const $element = {
                    0: element,
                    off() { return this; },
                    on(name, handler) { this.change = handler; return this; },
                    closest(selector) {
                        assert.equal(selector, '.control-checkbox, .control-checkbox-simple');
                        return area;
                    },
                    is(selector) {
                        assert.equal(selector, ':disabled');
                        return disabled;
                    },
                    trigger(name) {
                        if (name === 'focus') focused = true;
                        if (name === 'click') {
                            element.checked = !element.checked;
                            // A checkbox click bubbles back to the area without toggling twice.
                            clickArea('input');
                            this.change({ currentTarget: element });
                        }
                    },
                };
                const context = vm.createContext({
                    Control: class { static register() {} },
                    $: value => value === element ? $element : {
                        closest: selector => ({ length: selector.split(', ').includes(value) ? 1 : 0 }),
                    },
                    Transition: {
                        triggerEvent(control, name) {
                            assert.equal(control, $element);
                            if (name === 'change') changes++;
                            if (name === 'load') loads++;
                        },
                    },
                });
                const source = path.resolve(__dirname, '../../../..', moduleName,
                    'grails-app/assets/dueuno/elements/controls/Checkbox.js');
                vm.runInContext(fs.readFileSync(source, 'utf8'), context);
                const Checkbox = vm.runInContext('Checkbox', context);
                Checkbox.finalize($element);
                Checkbox.finalize($element);
                assert.equal(loads, 2);
                assert.equal(handlers.size, 1);

                const shouldToggle = !disabled && ['area', 'text'].includes(target);
                clickArea(target);
                assert.equal(element.checked, shouldToggle);
                assert.equal(changes, shouldToggle ? 1 : 0);
                assert.equal(focused, shouldToggle);
                clickArea(target);
                assert.equal(element.checked, false);
                assert.equal(changes, shouldToggle ? 2 : 0);
            });
        }
    }
}
