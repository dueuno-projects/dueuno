/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

let TableStickyHeader_states = new Set();
let TableStickyHeader_observer = null;

class TableStickyHeader {

    static get states() { return TableStickyHeader_states }
    static get observer() { return TableStickyHeader_observer }
    static set observer(value) { TableStickyHeader_observer = value }

    static initialize($table, fixedOffset, zIndex) {
        zIndex = zIndex == 0 ? 1 : zIndex;

        for (let i = 0; i < $table.length; i++) {
            TableStickyHeader.initializeTable($($table[i]), fixedOffset, zIndex);
        }
    }

    static initializeTable($table, fixedOffset, zIndex) {
        TableStickyHeader.destroy($table);

        let table = $table[0];
        let header = table ? table.querySelector('thead') : null;
        if (!header) {
            return;
        }

        let placeholder = header.cloneNode(true);
        placeholder.classList.add('table-floating-header-placeholder');
        placeholder.style.display = 'none';
        placeholder.style.opacity = '0';
        placeholder.setAttribute('aria-hidden', 'true');
        placeholder.querySelectorAll('input, select, textarea, button').forEach(function (control) {
            control.disabled = true;
        });
        placeholder.querySelectorAll('a, input, select, textarea, button, [tabindex]').forEach(function (element) {
            element.setAttribute('tabindex', '-1');
        });
        header.after(placeholder);

        let $dataset = $(table).closest('.component-table').find('.component-table-dataset').first();
        let state = {
            $table: $table,
            table: table,
            header: header,
            placeholder: placeholder,
            dataset: $dataset[0],
            fixedOffset: Number(fixedOffset) || 0,
            zIndex: zIndex == null ? 1 : zIndex,
            useScrollTimeline: !!$dataset[0] &&
                CSS.supports('animation-timeline: --table-horizontal-scroll') &&
                CSS.supports('scroll-timeline-name: --table-horizontal-scroll') &&
                CSS.supports('scroll-timeline-axis: x'),
            headerStyle: header.getAttribute('style'),
            cellStyles: Array.from(header.querySelectorAll('th, td')).map(function (cell) {
                return cell.getAttribute('style');
            }),
            frame: 0,
            forceSizeSync: false,
            isSticky: false
        };

        state.scheduleUpdate = function (event) {
            state.forceSizeSync = state.forceSizeSync || (event && event.type === 'resize');
            if (state.frame) {
                return;
            }

            state.frame = window.requestAnimationFrame(function () {
                state.frame = 0;
                let forceSizeSync = state.forceSizeSync;
                state.forceSizeSync = false;
                if (!document.documentElement.contains(state.table)) {
                    TableStickyHeader.destroy(state.$table);
                    return;
                }
                TableStickyHeader.update(state, forceSizeSync);
            });
        };

        state.update = function () {
            TableStickyHeader.update(state, false);
        };

        state.destroy = function () {
            TableStickyHeader.destroy($table);
        };

        $(window).on('scroll', state.scheduleUpdate);
        $(window).on('resize', state.scheduleUpdate);
        if (state.dataset && !state.useScrollTimeline) {
            $(state.dataset).on('scroll', state.update);
        }
        $table.on('destroyed.TableStickyHeader', state.destroy);
        $table.data('tableStickyHeader', state);
        TableStickyHeader.states.add(state);

        if (!TableStickyHeader.observer) {
            TableStickyHeader.observer = new MutationObserver(function () {
                Array.from(TableStickyHeader.states).forEach(function (instance) {
                    if (!document.documentElement.contains(instance.table)) {
                        TableStickyHeader.destroy(instance.$table);
                    }
                });
            });
            TableStickyHeader.observer.observe(document.documentElement, {childList: true, subtree: true});
        }

        TableStickyHeader.update(state, true);
    }

    static update(state, forceSizeSync) {
        let tableRect = state.table.getBoundingClientRect();
        let headerHeight = state.header.getBoundingClientRect().height;
        let shouldStick = tableRect.top < state.fixedOffset &&
            tableRect.bottom > state.fixedOffset + headerHeight;

        if (!shouldStick) {
            if (state.isSticky) {
                TableStickyHeader.unstick(state);
            }
            return;
        }

        let isFirstStickyUpdate = !state.isSticky;
        state.placeholder.style.display = '';
        if (isFirstStickyUpdate) {
            TableStickyHeader.syncPlaceholderDisplay(state);
        }
        state.header.style.position = 'fixed';
        state.header.style.top = state.fixedOffset + 'px';
        let headerLeft = tableRect.left;
        if (state.useScrollTimeline) {
            headerLeft += state.dataset.scrollLeft;
        }
        state.header.style.left = headerLeft + 'px';
        state.header.style.width = state.placeholder.getBoundingClientRect().width + 'px';
        state.header.style.marginTop = '0';
        state.header.style.zIndex = state.zIndex;
        state.header.classList.add('sticky-header');
        if (state.useScrollTimeline) {
            state.header.classList.add('table-sticky-header-scroll-timeline');
            TableStickyHeader.updateScrollDistance(state);
        }
        state.isSticky = true;

        if (isFirstStickyUpdate || forceSizeSync) {
            TableStickyHeader.syncCellWidths(state);
        }
    }

    static refresh($table) {
        let state = $table.data('tableStickyHeader');
        if (!state || !state.isSticky) {
            return;
        }

        TableStickyHeader.syncPlaceholderDisplay(state);
        TableStickyHeader.syncCellWidths(state);
        if (state.useScrollTimeline) {
            TableStickyHeader.updateScrollDistance(state);
        }
    }

    static syncPlaceholderDisplay(state) {
        let headerElements = state.header.querySelectorAll('*');
        let placeholderElements = state.placeholder.querySelectorAll('*');

        for (let i = 0; i < headerElements.length && i < placeholderElements.length; i++) {
            placeholderElements[i].classList.toggle('d-none', headerElements[i].classList.contains('d-none'));
        }
    }

    static syncCellWidths(state) {
        let headerCells = state.header.querySelectorAll('th, td');
        let placeholderCells = state.placeholder.querySelectorAll('th, td');
        placeholderCells.forEach(function (cell, index) {
            let width = cell.getBoundingClientRect().width;
            if (headerCells[index]) {
                headerCells[index].style.minWidth = width + 'px';
                headerCells[index].style.maxWidth = width + 'px';
            }
        });
    }

    static updateScrollDistance(state) {
        if (!state.useScrollTimeline) {
            return;
        }
        state.header.style.setProperty(
            '--table-sticky-scroll-distance',
            Math.max(0, state.dataset.scrollWidth - state.dataset.clientWidth) + 'px'
        );
    }

    static unstick(state) {
        state.placeholder.style.display = 'none';
        state.header.classList.remove('sticky-header');
        state.header.classList.remove('table-sticky-header-scroll-timeline');
        TableStickyHeader.restoreStyles(state);
        state.isSticky = false;
    }

    static restoreStyles(state) {
        if (state.headerStyle === null) {
            state.header.removeAttribute('style');
        } else {
            state.header.setAttribute('style', state.headerStyle);
        }

        state.header.querySelectorAll('th, td').forEach(function (cell, index) {
            let style = state.cellStyles[index];
            if (style === null || style === undefined) {
                cell.removeAttribute('style');
            } else {
                cell.setAttribute('style', style);
            }
        });
    }

    static destroy($table) {
        let state = $table.data('tableStickyHeader');
        if (!state) {
            return;
        }

        $(window).off('scroll', state.scheduleUpdate);
        $(window).off('resize', state.scheduleUpdate);
        if (state.dataset && !state.useScrollTimeline) {
            $(state.dataset).off('scroll', state.update);
        }
        $table.off('destroyed.TableStickyHeader', state.destroy);

        if (state.frame) {
            window.cancelAnimationFrame(state.frame);
        }
        if (state.isSticky) {
            TableStickyHeader.unstick(state);
        }
        state.placeholder.remove();
        $table.removeData('tableStickyHeader');
        TableStickyHeader.states.delete(state);
        if (!TableStickyHeader.states.size && TableStickyHeader.observer) {
            TableStickyHeader.observer.disconnect();
            TableStickyHeader.observer = null;
        }
    }
}
