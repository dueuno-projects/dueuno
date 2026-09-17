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
const os = require('node:os');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { test } = require('node:test');

const chrome = process.env.CHROME_BIN || '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';

for (const moduleName of ['dueuno-core', 'app-test']) {
    test(`${moduleName}: detached dropdown keyboard navigation stays with its owner`, {
        skip: !fs.existsSync(chrome) && 'Set CHROME_BIN to run the browser regression test',
    }, () => {
        const directory = fs.mkdtempSync(path.join(os.tmpdir(), 'dueuno-dropdown-'));
        try {
            const assets = path.resolve(__dirname, '../../../..', moduleName, 'grails-app/assets/dueuno');
            const read = file => fs.readFileSync(path.join(assets, file), 'utf8');
            const button = id => `<div class="component-button btn-group"><button id="${id}" class="btn dropdown-toggle" data-bs-toggle="dropdown">Actions</button>
                <ul class="dropdown-menu"><li><a class="dropdown-item" href="#" id="${id}-first">First</a></li>
                <li><a class="dropdown-item disabled" href="#">Disabled</a></li>
                <li><a class="dropdown-item" style="display:none" href="#">Hidden</a></li>
                <li><a class="dropdown-item" href="#" id="${id}-second">Second</a></li></ul></div>`;
            const html = `<style>${read('libs/bootstrap-5.3.2-dist/css/bootstrap.min.css')}</style>
                <style>${read('elements/components/Button.css')}
                :root { --elements-secondary-bg: 210, 225, 240; --elements-secondary-text: 20, 30, 40; }
                * { transition: none !important; }</style>
                <div id="page">${button('back')}<table><tr><td>${button('row')}</td></tr></table></div>
                <div id="modal">${button('modal-row')}</div><pre id="result">PENDING</pre>
                <script>${read('libs/jquery-4.0.0.min.js')}</script>
                <script>${read('libs/bootstrap-5.3.2-dist/js/bootstrap.bundle.min.js')}</script>
                <script>class Component { static register() {} }
                const Page = { $self: $('#page') };
                const PageModal = { isActive: false, $self: $('#modal') };</script>
                <script>${read('elements/components/Button.js')}</script>
                <script>
                window.addEventListener('load', () => {
                    try {
                        const check = (value, message) => { if (!value) throw new Error(message); };
                        const press = key => document.activeElement.dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true, cancelable: true }));
                        const focused = id => check(document.activeElement.id === id, 'Expected focus on ' + id + ', got ' + document.activeElement.id);
                        $('.component-button').each(function () { Button.finalize($(this)); });
                        for (const id of ['row', 'modal-row', 'row']) {
                            PageModal.isActive = id === 'modal-row';
                            const toggle = document.getElementById(id);
                            const menu = toggle.nextElementSibling;
                            toggle.focus();
                            const background = getComputedStyle(toggle).backgroundColor;
                            toggle.click(); // Native activation of a focused button by Enter.
                            check(menu.parentElement.id === (PageModal.isActive ? 'modal' : 'page'), 'Menu must be detached');
                            press('ArrowDown'); focused(id + '-first');
                            check(getComputedStyle(toggle).backgroundColor === background, 'Open toggle background changed after focus entered menu');
                            press('ArrowDown'); focused(id + '-second');
                            press('ArrowDown'); focused(id + '-second');
                            check(document.getElementById('back').getAttribute('aria-expanded') !== 'true', 'Back menu opened');
                            press('ArrowUp'); focused(id + '-first');
                            press('ArrowUp'); focused(id + '-first');
                            const tab = new KeyboardEvent('keydown', { key: 'Tab', bubbles: true, cancelable: true });
                            document.activeElement.dispatchEvent(tab);
                            check(!tab.defaultPrevented, 'Tab must remain native');
                            press('Escape'); focused(id);
                            check(!menu.classList.contains('show'), 'Escape must close menu');
                            check(toggle.nextElementSibling === menu && !Button.dropdown.isDetached, 'Menu must be reattached');
                            press('ArrowDown'); focused(id + '-first');
                            document.activeElement.click();
                            check(!menu.classList.contains('show') && !Button.dropdown.isDetached, 'Action activation must close menu');
                        }
                        document.getElementById('result').textContent = 'PASS';
                    } catch (error) { document.getElementById('result').textContent = error.stack; }
                });</script>`;
            const fixture = path.join(directory, 'test.html');
            fs.writeFileSync(fixture, html);
            const result = spawnSync(chrome, ['--headless', '--no-sandbox', '--disable-gpu',
                '--no-first-run', '--no-default-browser-check', '--timeout=5000', '--disable-dev-shm-usage',
                '--user-data-dir=' + path.join(directory, 'profile'), '--dump-dom', 'file://' + fixture],
                { encoding: 'utf8', timeout: 30000, maxBuffer: 5 * 1024 * 1024 });
            assert.equal(result.status, 0, result.error?.message || result.stderr);
            assert.match(result.stdout, /<pre id="result">PASS<\/pre>/,
                result.stdout.match(/<pre id="result">([\s\S]*?)<\/pre>/)?.[1] || result.stderr);
        } finally {
            fs.rmSync(directory, { recursive: true, force: true });
        }
    });
}
