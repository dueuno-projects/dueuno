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

import dueuno.core.PrettyPrinter
import dueuno.core.PrettyPrinterProperties
import dueuno.elements.controls.QuantityField
import dueuno.exceptions.ElementsException
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.GormEntity

/**
 * A GORM-persistent value type that represents a physical quantity — a numeric amount
 * paired with a {@link QuantityUnit}.
 * <p>
 * {@code Quantity} implements {@link CustomType} so it participates in the Elements
 * typed-value serialisation protocol, and extends {@link Number} so it can be used
 * wherever a numeric value is expected. Arithmetic operators ({@code +}, {@code -},
 * {@code *}, {@code /}) are provided for {@code Quantity × Number} operations, and
 * {@code +}/{@code -} for {@code Quantity × Quantity} (with automatic unit conversion).
 * Cross-dimension operations are checked and throw
 * {@link dueuno.exceptions.ElementsException} on incompatibility.
 * </p>
 * <p>
 * The associated UI control is {@link dueuno.elements.controls.QuantityField}.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 * @author Alessandro Stecca
 */

@CompileDynamic
class Quantity extends Number implements CustomType, GormEntity {

    /** The Elements type name used to identify this custom type in the serialisation protocol. */
    static final TYPE_NAME = 'QUANTITY'

    /** The UI control class used to render and edit {@code Quantity} values. */
    static final TYPE_FIELD = QuantityField

    /** The Java type of the primary value property ({@link #amount}). */
    static final TYPE_VALUE_PROPERTY_TYPE = Number

    /** The name of the primary value property. */
    static final TYPE_VALUE_PROPERTY_NAME = 'amount'

    /** The numeric amount, stored with up to 6 decimal places. */
    BigDecimal amount

    /** The unit of measurement for this quantity. */
    QuantityUnit unit

    static constraints = {
        amount scale: 6
        unit maxSize: 3
    }

    /** Creates a zero-amount {@code Quantity} with the default unit {@link QuantityUnit#PCS}. */
    Quantity() {
        this(0)
    }

    /**
     * Creates a {@code Quantity} from a {@link Number} amount, converting it to
     * {@link BigDecimal} via its {@code double} representation.
     *
     * @param amount the numeric amount
     * @param unit   the unit of measurement; defaults to {@link QuantityUnit#PCS}
     */
    Quantity(Number amount, QuantityUnit unit = QuantityUnit.PCS) {
        this(new BigDecimal(amount as Double), unit)
    }

    /**
     * Creates a {@code Quantity} from a {@link BigDecimal} amount.
     *
     * @param amount the numeric amount
     * @param unit   the unit of measurement; defaults to {@link QuantityUnit#PCS}
     */
    Quantity(BigDecimal amount, QuantityUnit unit = QuantityUnit.PCS) {
        this.amount = amount
        this.unit = unit
    }

    /**
     * Serialises this instance to the typed-value map protocol expected by the Elements frontend.
     * The {@code value} entry contains {@code amount} and {@code unit} sub-keys.
     *
     * @return a map with {@code type} ({@code "QUANTITY"}) and {@code value} keys
     */
    Map serialize() {
        return [
                type : TYPE_NAME,
                value: [
                        amount: amount,
                        unit: unit as String,
                ]
        ]
    }

    /**
     * Populates this instance from a typed-value map previously produced by {@link #serialize()}.
     * Reads {@code unit} and {@code amount} from the nested {@code value} map.
     * Does nothing if the {@code value} entry is absent or empty.
     *
     * @param valueMap the typed-value map to deserialise
     */
    void deserialize(Map valueMap) {
        Map value = valueMap.value as Map
        if (!value) {
            return
        }

        unit = value.unit ? (QuantityUnit) value.unit : QuantityUnit.NR
        amount = Types.deserializeBigDecimal(
                value.amount as String,
                value.decimals as Integer
        )
    }

    /**
     * Returns a human-readable representation of this quantity.
     * The unit token is placed before or after the formatted amount depending on
     * {@link PrettyPrinterProperties#prefixedUnit} (defaults to suffix).
     * Returns an empty string when {@link #amount} is {@code null}.
     *
     * @param properties formatting options (decimal format, locale, unit display mode, etc.)
     * @return the formatted quantity string (e.g. {@code "1.234,56 KG"} or {@code "kg 1,234.56"})
     */
    String prettyPrint(PrettyPrinterProperties properties) {
        if (amount == null)
            return ''

        String unit = printUnit(properties)
        String amount = PrettyPrinter.printDecimal(amount, properties)

        Boolean prefixedUnit = properties.prefixedUnit == null ? false : properties.prefixedUnit
        return prefixedUnit
                ? unit + ' ' + amount
                : amount + ' ' + unit
    }

    /**
     * Returns the string representation of the {@link #amount} (without unit).
     *
     * @return the amount as a string
     */
    String toString() {
        return amount
    }

    /**
     * Returns the formatted unit token for this instance.
     * When {@link PrettyPrinterProperties#symbolicQuantity} is {@code true} (default), the
     * unit name is resolved through the i18n message source under the
     * {@code quantity.unit} prefix (e.g. {@code quantity.unit.KG} → {@code "kg"}).
     * Otherwise the raw enum name is returned.
     * Returns an empty string when {@link #unit} is {@code null}.
     *
     * @param properties formatting options (locale, symbolic quantity flag)
     * @return the formatted unit token (e.g. {@code "kg"} or {@code "KG"})
     */
    String printUnit(PrettyPrinterProperties properties) {
        if (unit == null)
            return ''

        Boolean symbolicQuantity = properties.symbolicQuantity == null ? true : properties.symbolicQuantity
        if (symbolicQuantity) {
            PrettyPrinterProperties renderProperties = new PrettyPrinterProperties()
            renderProperties.locale = properties.locale
            renderProperties.textPrefix = 'quantity.unit'
            return PrettyPrinter.printString(unit.toString(), renderProperties)

        } else {
            return unit.toString()
        }
    }

    /** @return the {@link #amount} as an {@code int} */
    int intValue() {
        return amount.intValue()
    }

    /** @return the {@link #amount} as a {@code long} */
    long longValue() {
        return amount.longValue()
    }

    /** @return the {@link #amount} as a {@code float} */
    float floatValue() {
        return amount.floatValue()
    }

    /** @return the {@link #amount} as a {@code double} */
    double doubleValue() {
        return amount.doubleValue()
    }

    //
    // Quantities
    //

    /**
     * Returns a new {@code Quantity} whose amount is the sum of this and {@code q}'s amounts.
     * {@code q} is automatically converted to this instance's unit before addition.
     *
     * @param q the addend; must share the same dimension as this instance
     * @return the sum as a new {@code Quantity} in this instance's unit
     * @throws dueuno.exceptions.ElementsException if the dimensions are incompatible
     */
    Quantity plus(Quantity q) {
        return new Quantity((this.amount + q.convert(this.unit).amount), this.unit)
    }

    /**
     * Returns a new {@code Quantity} whose amount is the difference of this and {@code q}'s amounts.
     * {@code q} is automatically converted to this instance's unit before subtraction.
     *
     * @param q the subtrahend; must share the same dimension as this instance
     * @return the difference as a new {@code Quantity} in this instance's unit
     * @throws dueuno.exceptions.ElementsException if the dimensions are incompatible
     */
    Quantity minus(Quantity q) {
        return new Quantity((this.amount - q.convert(this.unit).amount), this.unit)
    }

//    Quantity multiply(Quantity q) {
//        return new Quantity((this.amount * q.convertForMultiply(this.unit).amount), getResultUnit(q))
//    }


    //
    // Numbers
    //

    /**
     * Returns a new {@code Quantity} whose amount is this amount plus {@code n}.
     *
     * @param n the addend
     * @return the sum as a new {@code Quantity} with the same unit
     */
    Quantity plus(Number n) {
        return new Quantity(this.amount + n, this.unit)
    }

    /**
     * Returns a new {@code Quantity} whose amount is this amount minus {@code n}.
     *
     * @param n the subtrahend
     * @return the difference as a new {@code Quantity} with the same unit
     */
    Quantity minus(Number n) {
        return new Quantity(this.amount - n, this.unit)
    }

    /**
     * Returns a new {@code Quantity} whose amount is this amount multiplied by {@code n}.
     *
     * @param n the multiplier
     * @return the product as a new {@code Quantity} with the same unit
     */
    Quantity multiply(Number n) {
        return new Quantity(this.amount * n, this.unit)
    }

    /**
     * Returns a new {@code Quantity} whose amount is this amount divided by {@code n}.
     *
     * @param n the divisor
     * @return the quotient as a new {@code Quantity} with the same unit
     */
    Quantity div(Number n) {
        return new Quantity(this.amount / n, this.unit)
    }


    //
    // Convertions
    //

    /**
     * Converts this quantity to the specified unit within the same dimension group.
     * Supports {@code MASS}, {@code LENGTH}, {@code AREA}, and {@code VOLUME} conversions
     * using SI magnitude exponents. {@code TIME} conversion is not yet implemented.
     *
     * @param toUnit the target unit; must belong to the same dimension group as this instance's unit
     * @return a new {@code Quantity} expressed in {@code toUnit}
     * @throws dueuno.exceptions.ElementsException if the units belong to different dimensions
     */
    Quantity convert(QuantityUnit toUnit) {
        if (unit.parent != toUnit.parent) {
            throw new ElementsException("Cannot convert '${unit.parent}' to '${toUnit.parent}'")
        }

        if (toUnit in [QuantityUnit.MASS, QuantityUnit.LENGTH, QuantityUnit.AREA, QuantityUnit.VOLUME]) {
            Integer startMagnitude = unit.magnitude
            Integer resultMagnitude = toUnit.magnitude
            Integer diff = startMagnitude - resultMagnitude
            return new Quantity(this.amount * (10.power(diff)), toUnit)
        }

        if (toUnit in [QuantityUnit.TIME]) {

        }

        throw new ElementsException("Cannot convert '${unit.parent}' to '${toUnit.parent}'")
    }

    /**
     * Converts a raw number of seconds into a {@code Quantity} expressed in the given time unit.
     *
     * @param seconds the number of seconds to convert
     * @param unit    the target time unit; must belong to the {@code TIME} dimension
     * @return a new {@code Quantity} in the requested time unit
     * @throws dueuno.exceptions.ElementsException if {@code unit} is not a {@code TIME} unit
     *         or if the unit is not one of the supported time units
     */
    private Quantity fromSeconds(Double seconds, QuantityUnit unit) {
        if (unit.parent != QuantityUnit.TIME) {
            throw new ElementsException("Please specify one of the 'TIME' units")
        }

        switch (unit) {
            case QuantityUnit.MSEC: return new Quantity(seconds * 1000, QuantityUnit.MSEC)
            case QuantityUnit.SEC: return new Quantity(seconds, QuantityUnit.SEC)
            case QuantityUnit.MIN: return new Quantity(seconds * 60, QuantityUnit.MIN)
            case QuantityUnit.HOUR: return new Quantity(seconds * 60 * 60, QuantityUnit.HOUR)
        }

        throw new ElementsException("Cannot convert seconds to ${unit}")
    }

    /**
     * Converts a {@code TIME} quantity to its equivalent number of seconds.
     *
     * @param quantity the time quantity to convert; must belong to the {@code TIME} dimension
     * @return the equivalent number of seconds as a {@code Double}
     * @throws dueuno.exceptions.ElementsException if {@code quantity} is not a {@code TIME} quantity
     *         or if the unit is not one of the supported time units
     */
    private Double toSeconds(Quantity quantity) {
        if (quantity.unit.parent != QuantityUnit.TIME) {
            throw new ElementsException("Please specify a 'TIME' quantity")
        }

        switch (quantity.unit) {
            case QuantityUnit.MSEC: return quantity.amount / 1000
            case QuantityUnit.SEC: return quantity.amount
            case QuantityUnit.MIN: return quantity.amount * 60
            case QuantityUnit.HOUR: return quantity.amount * 60 * 60
        }

        throw new ElementsException("Cannot convert ${quantity.unit} to seconds")
    }

    /**
     * Determines the result unit for a multiplication between this quantity and {@code quantity},
     * following dimensional-analysis rules (e.g. LENGTH × LENGTH → AREA, LENGTH × AREA → VOLUME).
     *
     * @param quantity the right-hand operand of the multiplication
     * @return the {@link QuantityUnit} of the result
     * @throws dueuno.exceptions.ElementsException if the dimension combination is not supported
     */
    private QuantityUnit getResultUnit(Quantity quantity) {
        if (unit.parent == quantity.unit.parent) {
            return getUpperUnit(quantity)
        } else if (unit.parent == 'LENGTH' && quantity.unit.parent == 'SQUARE') {
            return (getUpperUnit(getUpperUnit(this)))
        } else if (unit.parent == 'SQUARE' && quantity.unit.parent == 'LENGTH') {
            return (getUpperUnit(this))
        } else if (unit.parent == 'VOLUME' && quantity.unit.parent == 'LENGTH') {
            return unit
        } else if (unit.parent == 'LENGTH' && quantity.unit.parent == 'VOLUME') {
            return (getUpperUnit(getUpperUnit(this)))
        } else {
            throw new ElementsException("Cannot multiply '${unit}' with '${quantity.unit}'")
        }
    }

    /**
     * Converts this quantity to the unit required for a multiplication operation targeting
     * {@code toUnit}, applying cross-dimension rules (e.g. converting a LENGTH to the
     * corresponding linear sub-unit of an AREA or VOLUME target).
     *
     * @param toUnit the unit of the result dimension
     * @return this quantity converted to the appropriate intermediate unit
     * @throws dueuno.exceptions.ElementsException if the dimension combination is not supported
     */
    private Quantity convertForMultiply(QuantityUnit toUnit) {
        if (unit.parent == toUnit.parent) {
            return convert(toUnit)
        } else if (unit.parent == 'LENGTH' && toUnit.parent == 'SQUARE') {
            return convert(getLowerUnit(toUnit))
        } else if (unit.parent == 'SQUARE' && toUnit.parent == 'LENGTH') {
            return convert(getUpperUnit(toUnit))
        } else if (unit.parent == 'VOLUME' && toUnit.parent == 'LENGTH') {
            return convert(getUpperUnit(getUpperUnit(toUnit)))
        } else if (unit.parent == 'LENGTH' && toUnit.parent == 'VOLUME') {
            return convert(getLowerUnit(getLowerUnit(toUnit)))
        } else {
            throw new ElementsException("Cannot multiply '${unit}' with '${toUnit}'")
        }
    }

    /**
     * Returns the "upper" unit of the given quantity (i.e. the next-higher dimension unit),
     * delegating to {@link #getUpperUnit(QuantityUnit)}.
     *
     * @param quantity the quantity whose unit to promote
     * @return the next-higher {@link QuantityUnit} in the dimension hierarchy
     * @throws dueuno.exceptions.ElementsException if no upper unit is defined
     */
    private QuantityUnit getUpperUnit(Quantity quantity) {
        return quantity.getUpperUnit(quantity.unit)
    }

    /**
     * Returns the "upper" unit for the given unit, following the LENGTH → AREA → VOLUME hierarchy
     * (e.g. {@code M} → {@code M2} → {@code M3}).
     *
     * @param unit the unit to promote
     * @return the next-higher {@link QuantityUnit} in the dimension hierarchy
     * @throws dueuno.exceptions.ElementsException if {@code unit} has no defined upper unit
     */
    private QuantityUnit getUpperUnit(QuantityUnit unit) {
        QuantityUnit result
        if (unit == QuantityUnit.KM) {
            result = QuantityUnit.KM2
        } else if (unit == QuantityUnit.HM) {
            result = QuantityUnit.HM2
        } else if (unit == QuantityUnit.DAM) {
            result = QuantityUnit.DAM2
        } else if (unit == QuantityUnit.M) {
            result = QuantityUnit.M2
        } else if (unit == QuantityUnit.DM) {
            result = QuantityUnit.DM2
        } else if (unit == QuantityUnit.CM) {
            result = QuantityUnit.CM2
        } else if (unit == QuantityUnit.UM) {
            result = QuantityUnit.UM2
        } else if (unit == QuantityUnit.KM2) {
            result = QuantityUnit.KM3
        } else if (unit == QuantityUnit.HM2) {
            result = QuantityUnit.HM3
        } else if (unit == QuantityUnit.DAM2) {
            result = QuantityUnit.DAM3
        } else if (unit == QuantityUnit.M2) {
            result = QuantityUnit.M3
        } else if (unit == QuantityUnit.DM2) {
            result = QuantityUnit.DM3
        } else if (unit == QuantityUnit.CM2) {
            result = QuantityUnit.CM3
        } else if (unit == QuantityUnit.UM2) {
            result = QuantityUnit.UM3
        } else {
            throw new ElementsException("Cannot multiply '${this.unit}' with '${unit}'")
        }
        return result
    }

    /**
     * Returns the "lower" unit for the given unit, following the VOLUME → AREA → LENGTH hierarchy
     * (e.g. {@code M3} → {@code M2} → {@code M}).
     *
     * @param unit the unit to demote
     * @return the next-lower {@link QuantityUnit} in the dimension hierarchy
     * @throws dueuno.exceptions.ElementsException if {@code unit} has no defined lower unit
     */
    private QuantityUnit getLowerUnit(QuantityUnit unit) {
        QuantityUnit result
        switch (unit) {
            case QuantityUnit.KM3:
                result = QuantityUnit.KM2
                break
            case QuantityUnit.HM3:
                result = QuantityUnit.HM2
                break
            case QuantityUnit.DAM3:
                result = QuantityUnit.DAM2
                break
            case QuantityUnit.M3:
                result = QuantityUnit.M2
                break
            case QuantityUnit.DM3:
                result = QuantityUnit.DM2
                break
            case QuantityUnit.CM3:
                result = QuantityUnit.CM2
                break
            case QuantityUnit.MM3:
                result = QuantityUnit.MM2
                break
            case QuantityUnit.UM3:
                result = QuantityUnit.UM2
                break
            case QuantityUnit.KM2:
                result = QuantityUnit.KM
                break
            case QuantityUnit.HM2:
                result = QuantityUnit.HM
                break
            case QuantityUnit.DAM2:
                result = QuantityUnit.DAM
                break
            case QuantityUnit.M2:
                result = QuantityUnit.M
                break
            case QuantityUnit.DM2:
                result = QuantityUnit.DM
                break
            case QuantityUnit.CM2:
                result = QuantityUnit.CM
                break
            case QuantityUnit.MM2:
                result = QuantityUnit.MM
                break
            case QuantityUnit.UM2:
                result = QuantityUnit.UM
                break
            default:
                //result = null
                throw new ElementsException("Cannot multiply '${this.unit}' with '${unit}'")
                break
        }
        return result
    }
}
