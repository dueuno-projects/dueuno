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
    test(`${moduleName}: choosing a unit returns focus to the quantity input`, () => {
        const input = {};
        let selectedUnit;
        let prevented = false;
        let focused = false;
        const item = {
            data: () => 'KM',
            closest: () => ({ find: () => input }),
        };
        const context = vm.createContext({
            NumberField: class {},
            Control: { register() {} },
            $: value => value,
            Component: {
                setFocus(element, enabled) {
                    assert.equal(selectedUnit, 'KM');
                    assert.equal(element, input);
                    assert.equal(enabled, true);
                    focused = true;
                },
            },
        });
        const source = path.resolve(__dirname, '../../../..', moduleName,
            'grails-app/assets/dueuno/elements/controls/QuantityField.js');
        vm.runInContext(fs.readFileSync(source, 'utf8'), context);
        const QuantityField = vm.runInContext('QuantityField', context);
        QuantityField.setUnit = (element, unit) => {
            assert.equal(element, input);
            selectedUnit = unit;
        };

        QuantityField.onSelectUnit.call(item, {
            preventDefault() { prevented = true; },
        });

        assert.equal(prevented, true, 'the unit link must not navigate to #');
        assert.equal(focused, true);
    });
}
