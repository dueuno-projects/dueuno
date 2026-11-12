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
    test(`${moduleName}: reconnect subscribes to the tenant user queue and tenant channels`, () => {
        const source = fs.readFileSync(path.resolve(__dirname,
            `../../../../${moduleName}/grails-app/assets/dueuno/elements/base/Transition.js`), 'utf8');
        const subscriptions = [];
        const context = {
            _21_: { app: { tenant: 'tenant-a', url: '/app/' } },
            log: { debug() {} },
            $: { ajax({ url }) {
                assert.equal(url, '/app/transition/channels');
                return { done(callback) { callback(['updates']); } };
            } },
        };
        vm.createContext(context);
        vm.runInContext(source + '\nthis.Transition = Transition;', context);
        const client = { subscribe(destination) { subscriptions.push(destination); } };

        context.Transition.wsOnConnect.call(client, { headers: { 'user-name': 'alice' } });
        context._21_.app.tenant = 'tenant-b';
        context.Transition.wsOnConnect.call(client, { headers: { 'user-name': 'alice' } });

        assert.deepEqual(subscriptions, [
            '/queue/tenant/tenant-a/username/alice', '/queue/tenant/tenant-a/channel/updates',
            '/queue/tenant/tenant-b/username/alice', '/queue/tenant/tenant-b/channel/updates',
        ]);
    });
}
