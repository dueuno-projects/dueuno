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
    function setup(globals = {}) {
        const source = path.resolve(__dirname, '../../../..', moduleName,
            'grails-app/assets/dueuno/utils/utils.js');
        const context = vm.createContext(globals);
        vm.runInContext(fs.readFileSync(source, 'utf8'), context);
        return { Utils: vm.runInContext('Utils', context), context };
    }

    test(`${moduleName}: Utils exposes helpers without global functions`, async () => {
        let scheduled;
        let delay;
        const { Utils, context } = setup({
            setTimeout(callback, ms) { scheduled = callback; delay = ms; },
        });
        for (const name of ['sleep', 'getOS', 'mountSimpleBar', 'unmountSimpleBar', 'capitalize', 'isEmpty']) {
            assert.equal(typeof Utils[name], 'function');
            assert.equal(context[name], undefined);
        }
        let resolved = false;
        const sleeping = Utils.sleep(250).then(() => { resolved = true; });
        assert.equal(delay, 250);
        assert.equal(resolved, false);
        scheduled();
        await sleeping;
        assert.equal(resolved, true);
        assert.equal(Utils.capitalize('dueuno'), 'Dueuno');
        assert.equal(Utils.capitalize(''), '');
        for (const value of [null, undefined, '', [], {}, 0]) assert.equal(Utils.isEmpty(value), true);
        for (const value of ['text', [0], { key: 0 }]) assert.equal(Utils.isEmpty(value), false);
        assert.equal(Utils.isEmpty(Object.create({ inherited: true })), true);
        assert.equal(context.key, undefined, 'Object iteration must not create globals');
    });

    test(`${moduleName}: Utils detects mobile and desktop operating systems`, () => {
        for (const [ua, touch, expected] of [
            ['iPhone', false, 'iOS'], ['iPad', false, 'iOS'], ['iPod', false, 'iOS'],
            ['Macintosh', true, 'iOS'], ['Macintosh', false, 'Mac'],
            ['Windows NT', false, 'Windows'], ['Android Linux', false, 'Android'],
            ['Linux', false, 'Linux'], ['Unix', false, 'Unix'],
            ['BlackBerry', false, 'BlackBerry'], ['unknown', false, undefined],
        ]) {
            const { Utils } = setup({ navigator: { userAgent: ua }, document: touch ? { ontouchend: null } : {} });
            assert.equal(Utils.getOS(), expected, ua);
        }
    });

    test(`${moduleName}: Utils parses query parameters without jQuery`, () => {
        const { Utils } = setup({ document: { location: { search: '?landingPage=/orders&mode=edit' } } });
        assert.equal(Utils.getQueryParameters().landingPage, '/orders');
        assert.equal(Utils.getQueryParameters('?a=1&b=2').b, '2');
        assert.equal(Utils.getQueryParameters('/test?a=1').a, '1');
    });

    test(`${moduleName}: Utils extracts URI strings without jQuery`, () => {
        const { Utils } = setup({ document: { location: { search: '?a=1' } } });
        assert.equal(Utils.getUriString('/orders?a=1'), '/orders');
        assert.equal(Utils.getUriString('/orders'), '/orders');
        assert.equal(Utils.getUriString(), '');
    });

    test(`${moduleName}: Utils manages SimpleBar instances`, () => {
        const element = {};
        let mounted;
        let unmounted = 0;
        class SimpleBar {
            constructor(target) { mounted = target; }
            static instances = new Map();
        }
        const { Utils } = setup({ SimpleBar });
        Utils.mountSimpleBar(element);
        assert.equal(mounted, element);
        Utils.unmountSimpleBar(element);
        assert.equal(unmounted, 0);
        SimpleBar.instances.set(element, { unMount() { unmounted++; } });
        Utils.unmountSimpleBar(element);
        assert.equal(unmounted, 1);
    });
}
