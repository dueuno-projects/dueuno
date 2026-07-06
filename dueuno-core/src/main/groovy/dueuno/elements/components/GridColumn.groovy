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
import groovy.contracts.Requires
import groovy.transform.CompileStatic

/**
 * A single column within a {@link Grid} container.
 * <p>
 * Column-width breakpoints ({@code xs}, {@code sm}, {@code md}, {@code lg}, {@code xl},
 * {@code xxl}) can be set on the column itself to override the parent {@link Grid}'s
 * breakpoints on a per-column basis. When a column-level breakpoint is {@code null},
 * the corresponding value from the parent grid is used instead.
 * {@link #getBreakpoints()} assembles the final Bootstrap column CSS classes that
 * reflect this fallback logic.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class GridColumn extends Component {

    /** The parent {@link Grid} that owns this column. */
    Grid grid

    /** Column-level override for the {@code xs} breakpoint, or {@code null} to inherit from {@link #grid}. */
    Integer xs

    /** Column-level override for the {@code sm} breakpoint, or {@code null} to inherit from {@link #grid}. */
    Integer sm

    /** Column-level override for the {@code md} breakpoint, or {@code null} to inherit from {@link #grid}. */
    Integer md

    /** Column-level override for the {@code lg} breakpoint, or {@code null} to inherit from {@link #grid}. */
    Integer lg

    /** Column-level override for the {@code xl} breakpoint, or {@code null} to inherit from {@link #grid}. */
    Integer xl

    /** Column-level override for the {@code xxl} breakpoint, or {@code null} to inherit from {@link #grid}. */
    Integer xxl

    /**
     * Creates a {@code GridColumn} instance configured from the supplied argument map.
     * The {@code grid} argument is required.
     *
     * @param args initialisation arguments; required key: {@code grid} ({@link Grid});
     *             optional keys: {@code xs}, {@code sm}, {@code md}, {@code lg}, {@code xl},
     *             {@code xxl} ({@link Integer}), plus all keys accepted by {@link Component#Component(Map)}
     */
    @Requires({ args.grid })
    GridColumn(Map args) {
        super(args)

        grid = args.grid as Grid

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
     * Builds the Bootstrap column CSS class string for this column, resolving each breakpoint
     * by preferring the column-level value and falling back to the parent {@link Grid}'s value.
     * Only breakpoints with a non-{@code null} value (either on the column or the grid) emit a
     * class token.
     *
     * @return a space-separated string of Bootstrap column classes (e.g. {@code "col-12 col-sm-6 col-md-4 "})
     */
    String getBreakpoints() {
        String result = ""

        if (xs) result += "col-${xs} " else if (grid.xs) result += "col-${grid.xs} "
        if (sm) result += "col-sm-${sm} " else if (grid.sm) result += "col-sm-${grid.sm} "
        if (md) result += "col-md-${md} " else if (grid.md) result += "col-md-${grid.md} "
        if (lg) result += "col-lg-${lg} " else if (grid.lg) result += "col-lg-${grid.lg} "
        if (xl) result += "col-xl-${xl} " else if (grid.xl) result += "col-xl-${grid.xl} "
        if (xxl) result += "col-xxl-${xxl} " else if (grid.xxl) result += "col-xxl-${grid.xxl} "

        return result
    }

}
