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
package dueuno.types

import groovy.transform.CompileStatic

//
// See: https://en.wikipedia.org/wiki/International_System_of_Quantities
//
/**
 * Enumerates all measurement units supported by the {@link Quantity} type, grouped by
 * physical dimension (mass, length, area, volume, time, power, energy) plus generic
 * count units.
 * <p>
 * Each constant carries three metadata fields:
 * </p>
 * <ul>
 *     <li>{@link #parent} — the name of the dimension group (e.g. {@code "MASS"},
 *         {@code "LENGTH"}); {@code null} for dimensionless units.</li>
 *     <li>{@link #desc} — an i18n message key (or description) for the unit label.</li>
 *     <li>{@link #magnitude} — the SI magnitude exponent relative to the base unit of
 *         the group (e.g. {@code KG} has magnitude {@code 3} because
 *         1 kg = 10³ g).</li>
 * </ul>
 * <p>
 * Dimension-header constants ({@code MASS}, {@code LENGTH}, etc.) are declared with the
 * no-arg constructor and act as group markers rather than usable units.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 * @author Alessandro Stecca
 * @see <a href="https://en.wikipedia.org/wiki/International_System_of_Quantities">International System of Quantities</a>
 */

@CompileStatic
enum QuantityUnit {

    /** Undefined / unknown unit. */
    ND(null, 'Not Defined', 0),

    /** Generic dimensionless quantity. */
    QTY(null, 'Quantity', 0),
    /** Dimensionless number. */
    NR(null, 'Number', 0),
    /** Dimensionless piece count. */
    PCS(null, 'Pieces', 0),

    /** Dimension-group marker for mass units. */
    MASS(),
    /** Metric tonne (10⁶ g). */ TON('MASS', 'quantity.unit.tonne', 6),
    /** Quintal / 100 kg (10⁵ g). */ QUI('MASS', 'quantity.unit.quintal', 5),
    /** Kilogram (10³ g). */ KG('MASS', 'quantity.unit.kilogramm', 3),
    /** Hectogram (10² g). */ HG('MASS', 'quantity.unit.hectogram', 2),
    /** Decagram (10 g). */ DAG('MASS', 'quantity.unit.decagram', 1),
    /** Gram (base mass unit). */ G('MASS', 'quantity.unit.gram', 0),
    /** Decigram (10⁻¹ g). */ DG('MASS', 'quantity.unit.decigram', -1),
    /** Centigram (10⁻² g). */ CG('MASS', 'quantity.unit.centigram', -2),
    /** Milligram (10⁻³ g). */ MH('MASS', 'quantity.unit.milligram', -3),

    /** Dimension-group marker for length units. */
    LENGTH(),
    /** Kilometre (10³ m). */ KM('LENGTH', 'quantity.unit.kilometre', 3),
    /** Hectometre (10² m). */ HM('LENGTH', 'quantity.unit.hectometre', 2),
    /** Decametre (10 m). */ DAM('LENGTH', 'quantity.unit.decametre', 1),
    /** Metre (base length unit). */ M('LENGTH', 'quantity.unit.metre', 0),
    /** Decimetre (10⁻¹ m). */ DM('LENGTH', 'quantity.unit.decimetre', -1),
    /** Centimetre (10⁻² m). */ CM('LENGTH', 'quantity.unit.centimetre', -2),
    /** Millimetre (10⁻³ m). */ MM('LENGTH', 'quantity.unit.millimetre', -3),
    /** Micrometre / µm (10⁻⁶ m). */ UM('LENGTH', 'quantity.unit.micrometre', -6),

    /** Dimension-group marker for area units. */
    AREA(),
    /** Square kilometre (10⁶ m²). */ KM2('AREA', 'quantity.unit.square.kilometre', 6),
    /** Square hectometre (10⁴ m²). */ HM2('AREA', 'quantity.unit.square.hectometre', 4),
    /** Square decametre (10² m²). */ DAM2('AREA', 'quantity.unit.square.decametre', 2),
    /** Square metre (base area unit). */ M2('AREA', 'quantity.unit.square.metre', 0),
    /** Square decimetre (10⁻² m²). */ DM2('AREA', 'quantity.unit.square.decimetre', -2),
    /** Square centimetre (10⁻⁴ m²). */ CM2('AREA', 'quantity.unit.square.centimetre', -4),
    /** Square millimetre (10⁻⁶ m²). */ MM2('AREA', 'quantity.unit.square.millimetre', -6),
    /** Square micrometre (10⁻⁸ m²). */ UM2('AREA', 'quantity.unit.square.micrometre', -8),

    /** Dimension-group marker for cubic volume units. */
    VOLUME(),
    /** Cubic kilometre (10⁹ m³). */ KM3('VOLUME', 'quantity.unit.cubic.kilometre', 9),
    /** Cubic hectometre (10⁶ m³). */ HM3('VOLUME', 'quantity.unit.cubic.hectometre', 6),
    /** Cubic decametre (10³ m³). */ DAM3('VOLUME', 'quantity.unit.cubic.decametre', 3),
    /** Cubic metre (base volume unit). */ M3('VOLUME', 'quantity.unit.cubic.metre', 0),
    /** Cubic decimetre (10⁻³ m³). */ DM3('VOLUME', 'quantity.unit.cubic.decimetre', -3),
    /** Cubic centimetre (10⁻⁶ m³). */ CM3('VOLUME', 'quantity.unit.cubic.centimetre', -6),
    /** Cubic millimetre (10⁻⁹ m³). */ MM3('VOLUME', 'quantity.unit.cubic.millimetre', -9),
    /** Cubic micrometre (10⁻¹² m³). */ UM3('VOLUME', 'quantity.unit.cubic.micrometre', -12),

    /** Dimension-group marker for litre-based volume units. */
    VOLUME_LITRE(),
    /** Hectolitre (10³ L). */ HL('VOLUME_LITRE', 'quantity.unit.hectolitre', 3),
    /** Decalitre (10 L). */ DAL('VOLUME_LITRE', 'quantity.unit.decalitre', 0),
    /** Litre (base litre unit). */ L('VOLUME_LITRE', 'quantity.unit.litre', -3),
    /** Decilitre (10⁻¹ L). */ DL('VOLUME_LITRE', 'quantity.unit.decilitre', -6),
    /** Centilitre (10⁻² L). */ CL('VOLUME_LITRE', 'quantity.unit.centilitre', -9),
    /** Millilitre (10⁻³ L). */ ML('VOLUME_LITRE', 'quantity.unit.millilitre', -12),

    /** Dimension-group marker for time units. */
    TIME(),
    /** Calendar year. */ YEAR('TIME', 'quantity.unit.year', 0),
    /** Calendar month. */ MONTH('TIME', 'quantity.unit.month', 0),
    /** Calendar week. */ WEEK('TIME', 'quantity.unit.week', 0),
    /** Calendar day. */ DAY('TIME', 'quantity.unit.day', 0),
    /** Hour. */ HOUR('TIME', 'quantity.unit.hour', 0),
    /** Minute. */ MIN('TIME', 'quantity.unit.minute', 0),
    /** Second. */ SEC('TIME', 'quantity.unit.second', 0),
    /** Millisecond. */ MSEC('TIME', 'quantity.unit.millisecond', 0),

    /** Dimension-group marker for power units. */
    POWER(),
    /** Terawatt (10¹² W). */ TW('POWER', 'quantity.unit.power.terawatt', 12),
    /** Gigawatt (10⁹ W). */ GW('POWER', 'quantity.unit.power.gigawatt', 9),
    /** Megawatt (10⁶ W). */ MW('POWER', 'quantity.unit.power.megawatt', 6),
    /** Kilowatt (10³ W). */ KW('POWER', 'quantity.unit.power.kilowatt', 3),
    /** Watt (base power unit). */ W('POWER', 'quantity.unit.power.watt', 0),

    /** Dimension-group marker for energy units. */
    ENERGY(),
    /** Terawatt-hour (10¹² Wh). */ TWH('ENERGY', 'quantity.unit.power.terawatthour', 12),
    /** Gigawatt-hour (10⁹ Wh). */ GWH('ENERGY', 'quantity.unit.power.gigawatthour', 9),
    /** Megawatt-hour (10⁶ Wh). */ MWH('ENERGY', 'quantity.unit.power.megawatthour', 6),
    /** Kilowatt-hour (10³ Wh). */ KWH('ENERGY', 'quantity.unit.power.kilowatthour', 3),
    /** Watt-hour (base energy unit). */ WH('ENERGY', 'quantity.unit.power.watthour', 0),

    /** The name of the dimension group this unit belongs to (e.g. {@code "MASS"}), or {@code null} for dimensionless units. */
    final String parent

    /** The i18n message key used to look up the human-readable label for this unit. */
    final String desc

    /**
     * The SI magnitude exponent relative to the base unit of this group.
     * For example, {@code KG} has magnitude {@code 3} (1 kg = 10³ g).
     * Dimension-group marker constants carry magnitude {@code 0}.
     */
    final Integer magnitude

    /** No-arg constructor used by dimension-group marker constants. */
    QuantityUnit() {}

    /**
     * Creates a unit constant with the specified group, i18n description key, and magnitude.
     *
     * @param parent    the name of the dimension group, or {@code null} for dimensionless units
     * @param desc      the i18n message key for the unit label
     * @param magnitude the SI magnitude exponent relative to the group's base unit
     */
    QuantityUnit(String parent, String desc, Integer magnitude) {
        this.parent = parent
        this.desc = desc
        this.magnitude = magnitude
    }

    /**
     * Returns the enum constant name (e.g. {@code "KG"}, {@code "M2"}).
     *
     * @return the name of this constant
     */
    String toString() {
        return name()
    }
}

