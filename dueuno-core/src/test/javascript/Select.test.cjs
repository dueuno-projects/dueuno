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
    for (const shiftKey of [false, true]) {
        for (const hasNext of [false, true]) {
            test(`${moduleName}: ${shiftKey ? 'Shift+Tab' : 'Tab'} closes before navigation (target: ${hasNext})`, () => {
                let opened = true;
                let focused;
                let prevented = false;
                let stopped = false;
                const element = { close() { opened = false; } };
                const next = hasNext ? [{}] : [];
                next.data = () => 'TextField';
                const context = vm.createContext({
                    Control: class { static register() {} },
                    $: value => [value],
                    Component: {
                        setFocus(target) {
                            assert.equal(opened, false);
                            focused = target;
                        },
                    },
                });
                const source = path.resolve(__dirname, '../../../..', moduleName,
                    'grails-app/assets/dueuno/elements/controls/Select.js');
                vm.runInContext(fs.readFileSync(source, 'utf8'), context);
                const Select = vm.runInContext('Select', context);
                Select.getTrailingFocusable = () => [];
                Select.getAdjacentControl = (_, direction) => {
                    assert.equal(opened, false);
                    assert.equal(direction, shiftKey ? -1 : 1);
                    return next;
                };
                Select.getAdjacentFocusable = () => [];

                Select.onKeyDown({
                    key: 'Tab', shiftKey, data: { element },
                    preventDefault() { prevented = true; },
                    stopPropagation() { stopped = true; },
                });

                assert.equal(opened, false);
                assert.equal(focused, hasNext ? next : undefined);
                assert.equal(prevented, hasNext);
                assert.equal(stopped, hasNext);
            });
        }
    }
}

for (const moduleName of ['dueuno-core', 'app-test']) {
    for (const hasActions of [true, false]) {
        test(`${moduleName}: Shift+Tab enters the previous Select at its last action (${hasActions})`, () => {
            let closed = false;
            let focused;
            const current = { close() { closed = true; } };
            const previous = { length: 1, data: () => 'Select' };
            const lastAction = { length: 1 };
            const context = vm.createContext({
                Control: class { static register() {} },
                $: value => [value],
                Component: { setFocus(target, enabled) {
                    assert.equal(closed, true);
                    assert.equal(enabled, true);
                    focused = target;
                } },
            });
            const source = path.resolve(__dirname, '../../../..', moduleName,
                'grails-app/assets/dueuno/elements/controls/Select.js');
            vm.runInContext(fs.readFileSync(source, 'utf8'), context);
            const Select = vm.runInContext('Select', context);
            Select.getAdjacentControl = () => previous;
            Select.getTrailingFocusable = target => {
                assert.equal(target, previous);
                return { length: hasActions ? 2 : 0, last: () => lastAction };
            };
            let prevented = false;
            Select.onKeyDown({
                key: 'Tab', shiftKey: true, data: { element: current },
                preventDefault() { prevented = true; },
                stopPropagation() {},
            });
            assert.equal(focused, hasActions ? lastAction : previous);
            assert.equal(prevented, true);
        });
    }
}
