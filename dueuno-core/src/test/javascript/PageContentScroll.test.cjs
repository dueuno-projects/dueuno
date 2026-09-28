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

function setup(moduleName) {
    const content = (controller, action) => ({
        0: {},
        properties: { controller, action },
        exists: () => true,
        find: () => ({ exists: () => false }),
        replaceWith(next) { current = next; window.scrollY = 0; },
    });
    let current = content('book', 'index');
    const scrollCalls = [];
    const animationFrames = [];
    const window = {
        scrollX: 12,
        scrollY: 480,
        scrollTo({ top, left, behavior }) {
            scrollCalls.push({ top, left, behavior });
            this.scrollY = top;
            this.scrollX = left;
        },
    };
    const context = vm.createContext({
        window,
        requestAnimationFrame(callback) { animationFrames.push(callback); },
        _21_: { user: { animations: true } },
        Component: class {
            static register() {}
            static getProperties(element) { return element.properties; }
        },
        $: () => current,
        Page: {
            initializeContent() {},
            finalizeContent() {},
            deactivateComponents() {},
            reinitializeContent() { window.scrollY = 0; },
        },
    });
    for (const name of ['PageContent', 'PageModal', 'TransitionCommand']) {
        const source = path.resolve(__dirname, '../../../..', moduleName,
            `grails-app/assets/dueuno/elements/base/${name}.js`);
        vm.runInContext(fs.readFileSync(source, 'utf8'), context);
    }
    const classes = vm.runInContext('({ PageContent, PageModal, TransitionCommand, Page })', context);
    const { PageModal, TransitionCommand } = classes;
    PageModal.renderContent = () => { window.scrollY = 0; };
    PageModal.renderDialog = () => {};
    PageModal.isReady = true;
    TransitionCommand.animate = () => {};
    TransitionCommand.getFocusPath = () => [];
    TransitionCommand.restoreFocus = () => { window.scrollY = 90; };
    TransitionCommand.setBrowserUrl = () => {};
    return { ...classes, window, scrollCalls, animationFrames, content, current: () => current };
}

for (const moduleName of ['dueuno-core', 'app-test']) {
    test(`${moduleName}: restores scroll after modal and Content rendering, including repeated loads`, async () => {
        const { PageModal, TransitionCommand, window, content, current } = setup(moduleName);
        await PageModal.open(content('book', 'create'), { renderProperties: { modal: true } });
        // A validation response inside the modal must not overwrite the saved position.
        await PageModal.open(content('book', 'create'), { renderProperties: { modal: true } });
        const event = { renderProperties: {} };
        for (let i = 0; i < 2; i++) {
            TransitionCommand.render(current(), content('book', 'index'), event);
            assert.equal(window.scrollY, 480);
            assert.equal(window.scrollX, 12);
        }
    });

    test(`${moduleName}: restores saved scroll when rendering without an explicit position`, async () => {
        const { PageModal, TransitionCommand, window, scrollCalls, content, current } = setup(moduleName);
        // Saving must depend on opening the modal, not on the event's render properties.
        await PageModal.open(content('book', 'create'), { renderProperties: {} });
        assert.deepEqual(scrollCalls, []);
        PageModal.isActive = false;
        TransitionCommand.render(current(), content('book', 'index'), { renderProperties: {} });
        assert.equal(window.scrollY, 480);
        assert.deepEqual(scrollCalls, [{ top: 480, left: 12, behavior: 'instant' }]);
    });

    for (const [scroll, top, left, behavior] of [
        ['reset', 0, 0, 'instant'],
        ['top', 0, 0, 'smooth'],
        ['table', 250, 20, 'smooth'],
    ]) {
        test(`${moduleName}: explicit ${scroll} overrides the saved position`, () => {
            const { PageContent, TransitionCommand, scrollCalls, animationFrames, current } = setup(moduleName);
            PageContent.saveScrollPosition();
            current().find = selector => {
                assert.equal(selector, '[data-21-id="table"]');
                return { exists: () => true, position: () => ({ top: 250, left: 20 }) };
            };
            TransitionCommand.scrollTo({ renderProperties: { scroll } }, current());
            assert.deepEqual(scrollCalls, [{ top, left, behavior }]);
            assert.equal(animationFrames.length, 0);
        });
    }

    test(`${moduleName}: an explicit missing target does not restore the saved position`, () => {
        const { PageContent, TransitionCommand, scrollCalls, animationFrames, current } = setup(moduleName);
        PageContent.saveScrollPosition();
        TransitionCommand.scrollTo({ renderProperties: { scroll: 'missing' } }, current());
        assert.deepEqual(scrollCalls, []);
        assert.equal(animationFrames.length, 0);
    });

    test(`${moduleName}: null scroll restores the saved position`, () => {
        const { PageContent, TransitionCommand, window, current } = setup(moduleName);
        PageContent.saveScrollPosition();
        window.scrollY = 0;
        TransitionCommand.scrollTo({ renderProperties: { scroll: null } }, current());
        assert.equal(window.scrollY, 480);
    });

    test(`${moduleName}: restores after deferred form autofocus and ignores replaced Content`, () => {
        const { PageContent, Page, TransitionCommand, window, animationFrames, content, current } = setup(moduleName);
        PageContent.saveScrollPosition();
        Page.reinitializeContent = () => {
            animationFrames.push(() => { window.scrollY = 0; });
        };
        TransitionCommand.render(current(), content('book', 'index'), { renderProperties: {} });
        assert.equal(animationFrames.length, 2);
        animationFrames.shift()();
        assert.equal(window.scrollY, 0);
        animationFrames.shift()();
        assert.equal(window.scrollY, 480);

        PageContent.restoreScrollPosition(current());
        current().replaceWith(content('author', 'index'));
        window.scrollY = 50;
        animationFrames.shift()();
        assert.equal(window.scrollY, 50);
    });

    test(`${moduleName}: isolates saved positions by controller and action`, () => {
        const { PageContent, TransitionCommand, window, content, current } = setup(moduleName);
        PageContent.saveScrollPosition();
        for (const other of [content('author', 'index'), content('book', 'details')]) {
            window.scrollY = 100;
            TransitionCommand.scrollTo({ renderProperties: { scroll: 'reset' } }, other);
            assert.equal(window.scrollY, 0);
        }
        TransitionCommand.scrollTo({ renderProperties: {} }, content('book', 'index'));
        assert.equal(window.scrollY, 480);
    });

    test(`${moduleName}: a later modal opening replaces the saved position, including zero`, async () => {
        const { PageModal, TransitionCommand, window, content, current } = setup(moduleName);
        await PageModal.open(content('book', 'create'), { renderProperties: { modal: true } });
        PageModal.isActive = false;
        window.scrollY = 0;
        await PageModal.open(content('book', 'edit'), { renderProperties: { modal: true } });
        window.scrollY = 250;
        TransitionCommand.scrollTo({ renderProperties: {} }, current());
        assert.equal(window.scrollY, 0);
    });

    test(`${moduleName}: preserves existing scroll behavior without a saved position`, () => {
        const { TransitionCommand, window, current } = setup(moduleName);
        TransitionCommand.scrollTo({ renderProperties: {} }, current());
        assert.equal(window.scrollY, 480);
        TransitionCommand.scrollTo({ renderProperties: { scroll: 'reset' } }, current());
        assert.equal(window.scrollY, 0);
        window.scrollY = 480;
        TransitionCommand.scrollTo({ renderProperties: { scroll: 'top' } }, current());
        assert.equal(window.scrollY, 0);
    });
}
