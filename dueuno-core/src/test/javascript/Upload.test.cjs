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

const context = vm.createContext({
    Control: class {
        static register() {}
        static getProperties($element) { return $element[0].properties; }
    },
    Component: {
        setReadonly($element, value) { $element[0].readonly = value; },
    },
});
vm.runInContext(fs.readFileSync(path.join(__dirname,
    '../../../grails-app/assets/dueuno/elements/controls/Upload.js'), 'utf8'), context);
const Upload = vm.runInContext('Upload', context);

test('readonly switches the upload message and restores the configured idle label', () => {
    const properties = {
        labelIdle: '<i class="fa-solid fa-cloud-arrow-up fa-fw me-1"></i>Custom upload text',
        messages: { disabled: '<i class="fa-solid fa-ban fa-fw me-1"></i>Click or drop a file' },
    };
    const pond = { disabled: false, labelIdle: properties.labelIdle };
    const $element = [{ properties, _filePond: pond }];

    Upload.setReadonly($element, true);
    assert.equal($element[0].readonly, true);
    assert.equal(pond.disabled, true);
    assert.equal(pond.labelIdle, properties.messages.disabled);

    Upload.setReadonly($element, false);
    assert.equal($element[0].readonly, false);
    assert.equal(pond.disabled, false);
    assert.equal(pond.labelIdle, properties.labelIdle);
});

test('readonly can be set before FilePond exists', () => {
    const $element = [{}];
    Upload.setReadonly($element, true);
    assert.equal($element[0].readonly, true);
});
