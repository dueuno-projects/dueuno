/*
 * Copyright 2021 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dueuno.elements.components

import dueuno.elements.Component
import groovy.transform.CompileStatic

/**
 * A Bootstrap responsive-grid container that holds one or more {@link GridColumn} children.
 * <p>
 * Column widths at each Bootstrap breakpoint ({@code xs}, {@code sm}, {@code md}, {@code lg},
 * {@code xl}, {@code xxl}) can be set individually or all at once via
 * {@link #setBreakpoints(Integer, Integer, Integer, Integer, Integer, Integer)}.
 * When a breakpoint is not specified ({@code null}), no corresponding Bootstrap column class
 * is emitted for that breakpoint, allowing the browser to fall back to a wider breakpoint's
 * definition.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class Grid extends Component {

    /** Bootstrap column span for the {@code xs} (extra-small) breakpoint, or {@code null} if unset. */
    Integer xs

    /** Bootstrap column span for the {@code sm} (small) breakpoint, or {@code null} if unset. */
    Integer sm

    /** Bootstrap column span for the {@code md} (medium) breakpoint, or {@code null} if unset. */
    Integer md

    /** Bootstrap column span for the {@code lg} (large) breakpoint, or {@code null} if unset. */
    Integer lg

    /** Bootstrap column span for the {@code xl} (extra-large) breakpoint, or {@code null} if unset. */
    Integer xl

    /** Bootstrap column span for the {@code xxl} (extra-extra-large) breakpoint, or {@code null} if unset. */
    Integer xxl

    /** The Bootstrap gutter spacing between columns (maps to Bootstrap {@code g-N} class). Defaults to {@code 1}. */
    Integer spacing

    /** Whether the grid renders as an HTML tag element (as opposed to a CSS-only container). */
    Boolean tag

    /**
     * Creates a {@code Grid} instance configured from the supplied argument map.
     * Breakpoints default to {@code null} (unset) unless explicitly provided.
     *
     * @param args initialisation arguments; recognised keys include:
     *             {@code spacing} ({@link Integer}, default {@code 1}),
     *             {@code tag} ({@link Boolean}, default {@code false}),
     *             {@code xs}, {@code sm}, {@code md}, {@code lg}, {@code xl}, {@code xxl} ({@link Integer}),
     *             plus all keys accepted by {@link Component#Component(Map)}
     */
    Grid(Map args) {
        super(args)

        spacing = args.spacing == null ? 1 : args.spacing as Integer
        tag = args.tag == null ? false : args.tag

        // No default for breakpoints since it would require
        // to set all of them everytime
        xs = args.xs as Integer
        sm = args.sm as Integer
        md = args.md as Integer
        lg = args.lg as Integer
        xl = args.xl as Integer
        xxl = args.xxl as Integer
    }

    /**
     * Adds a new {@link GridColumn} child to this grid.
     * If no {@code id} is provided in {@code args}, one is auto-generated from the grid's
     * own ID and the current column count.
     *
     * @param args column configuration forwarded to {@link GridColumn}; {@code id} is optional
     * @return the newly created {@link GridColumn}
     */
    GridColumn addColumn(Map args = [:]) {
        args['class'] = GridColumn
        if (!args['id']) args['id'] = "${id}-${components.size()}"
        args['grid'] = this

        GridColumn column = addComponent(args)
        return column
    }

    /**
     * Sets all Bootstrap breakpoint column spans at once.
     *
     * @param xs   column span for xs screens (default {@code 12})
     * @param sm   column span for sm screens (default {@code 6})
     * @param md   column span for md screens (default {@code 4})
     * @param lg   column span for lg screens (default {@code 4})
     * @param xl   column span for xl screens (default {@code 3})
     * @param xxl  column span for xxl screens (default {@code 2})
     */
    void setBreakpoints(Integer xs = 12, Integer sm = 6, Integer md = 4, Integer lg = 4, Integer xl = 3, Integer xxl = 2) {
        this.xs = xs
        this.sm = sm
        this.md = md
        this.lg = lg
        this.xl = xl
        this.xxl = xxl
    }

}
