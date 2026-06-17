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
import dueuno.elements.controls.MoneyField
import dueuno.exceptions.ElementsException
import grails.gorm.MultiTenant
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.GormEntity

/**
 * A GORM-persistent value type that represents a monetary amount paired with an ISO 4217
 * currency code.
 * <p>
 * {@code Money} implements {@link CustomType} so it participates in the Elements typed-value
 * serialisation protocol, and extends {@link Number} so it can be used wherever a numeric
 * value is expected. Arithmetic operators ({@code +}, {@code -}, {@code *}, {@code /}) are
 * provided for both {@code Money × Money} and {@code Money × Number} operations; mixed-currency
 * arithmetic throws an {@link dueuno.exceptions.ElementsException}.
 * </p>
 * <p>
 * The associated UI control is {@link dueuno.elements.controls.MoneyField}.
 * </p>
 *
 * @author Gianluca Sartori
 * @author Francesco Piceghello
 */

@Entity
@CompileDynamic
class Money extends Number implements CustomType, GormEntity, MultiTenant<Money> {

    /** The Elements type name used to identify this custom type in the serialisation protocol. */
    static final TYPE_NAME = 'MONEY'

    /** The UI control class used to render and edit {@code Money} values. */
    static final TYPE_FIELD = MoneyField

    /** The Java type of the primary value property ({@link #amount}). */
    static final TYPE_VALUE_PROPERTY_TYPE = Number

    /** The name of the primary value property. */
    static final TYPE_VALUE_PROPERTY_NAME = 'amount'

    /** The monetary amount, stored with up to 6 decimal places. */
    BigDecimal amount

    /** The ISO 4217 currency code (e.g. {@code "EUR"}, {@code "USD"}); max 3 characters. */
    String currency

    static constraints = {
        amount scale: 6
        currency maxSize: 3
    }

    /** Creates a zero-amount {@code Money} instance with the default currency {@code "EUR"}. */
    Money() {
        this(0)
    }

    /**
     * Creates a {@code Money} instance from a {@link Number} amount, converting it to
     * {@link BigDecimal} via its {@code double} representation.
     *
     * @param amount   the monetary amount
     * @param currency the ISO 4217 currency code; defaults to {@code "EUR"}
     */
    Money(Number amount, String currency = 'EUR') {
        this(new BigDecimal(amount as Double), currency)
    }

    /**
     * Creates a {@code Money} instance from a {@link BigDecimal} amount.
     *
     * @param amount   the monetary amount
     * @param currency the ISO 4217 currency code; defaults to {@code "EUR"}
     */
    Money(BigDecimal amount, String currency = 'EUR') {
        this.amount = amount
        this.currency = currency
    }

    /**
     * Returns the Elements type name for this custom type ({@link #TYPE_NAME}).
     *
     * @return {@code "MONEY"}
     */
    static String getCustomTypeName() {
        return TYPE_NAME
    }

    /**
     * Returns the UI control class associated with this type ({@link dueuno.elements.controls.MoneyField}).
     *
     * @return the {@link dueuno.elements.controls.MoneyField} class
     */
    static Class getCustomTypeField() {
        return MoneyField
    }

    /**
     * Serialises this instance to the typed-value map protocol expected by the Elements frontend.
     * The {@code value} entry contains {@code amount} and {@code currency} sub-keys.
     *
     * @return a map with {@code type} ({@code "MONEY"}) and {@code value} keys
     */
    Map serialize() {
        return [
                type : TYPE_NAME,
                value: [
                        amount: amount,
                        currency: currency,
                ]
        ]
    }

    /**
     * Populates this instance from a typed-value map previously produced by {@link #serialize()}.
     * Reads {@code currency} and {@code amount} from the nested {@code value} map.
     * Does nothing if the {@code value} entry is absent or empty.
     *
     * @param valueMap the typed-value map to deserialise
     */
    void deserialize(Map valueMap) {
        Map value = valueMap.value as Map
        if (!value) {
            return
        }

        currency = value.currency ?: 'EUR'
        amount = Types.deserializeBigDecimal(
                value.amount as String,
                value.decimals as Integer
        )
    }

    /**
     * Returns a human-readable representation of this monetary value.
     * The currency token is placed before or after the formatted amount depending on
     * {@link PrettyPrinterProperties#prefixedUnit} (defaults to suffix).
     * Returns an empty string when {@link #amount} is {@code null}.
     *
     * @param properties formatting options (decimal format, locale, currency display mode, etc.)
     * @return the formatted monetary string (e.g. {@code "1.234,56 EUR"} or {@code "$ 1,234.56"})
     */
    String prettyPrint(PrettyPrinterProperties properties) {
        if (amount == null)
            return ''

        String currency = printCurrency(properties)
        String amount = PrettyPrinter.printDecimal(amount, properties)

        Boolean prefixedUnit = properties.prefixedUnit == null ? false : properties.prefixedUnit
        return prefixedUnit
                ? currency + ' ' + amount
                : amount + ' ' + currency
    }

    /**
     * Returns the formatted currency token for this instance.
     * When {@link PrettyPrinterProperties#symbolicCurrency} is {@code true} (default), the
     * currency code is resolved through the i18n message source under the
     * {@code money.currency} prefix (e.g. {@code money.currency.EUR} → {@code "€"}).
     * Otherwise the raw ISO 4217 code is returned.
     * Returns an empty string when {@link #currency} is {@code null}.
     *
     * @param properties formatting options (locale, symbolic currency flag)
     * @return the formatted currency token (e.g. {@code "€"} or {@code "EUR"})
     */
    String printCurrency(PrettyPrinterProperties properties) {
        if (currency == null)
            return ''

        Boolean symbolicCurrency = properties.symbolicCurrency == null ? true : properties.symbolicCurrency
        if (symbolicCurrency) {
            PrettyPrinterProperties renderProperties = new PrettyPrinterProperties()
            renderProperties.locale = properties.locale
            renderProperties.textPrefix = 'money.currency'
            return PrettyPrinter.printString(currency.toString(), renderProperties)

        } else {
            return currency.toString()
        }
    }

    /**
     * Returns the string representation of the {@link #amount} (without currency).
     *
     * @return the amount as a string
     */
    String toString() {
        return amount
    }

    /** @return the {@link #amount} as an {@code int} */
    int intValue() { return amount.intValue() }

    /** @return the {@link #amount} as a {@code long} */
    long longValue() { return amount.longValue() }

    /** @return the {@link #amount} as a {@code float} */
    float floatValue() { return amount.floatValue() }

    /** @return the {@link #amount} as a {@code double} */
    double doubleValue() { return amount.doubleValue() }


    /**
     * Returns {@code true} if both the amount and currency of this instance equal those
     * of {@code money}.
     *
     * @param money the instance to compare with
     * @return {@code true} if amount and currency are equal
     */
    Boolean equals(Money money) {
        return (amount == money.amount && currency == money.currency)
    }

    /**
     * Throws {@link dueuno.exceptions.ElementsException} if {@code money}'s currency differs
     * from this instance's currency.
     */
    private void checkOperandsCompatibility(Money money) {
        if (currency != money.currency)
            throw new ElementsException("Cannot operate on money with different currencies: $currency + $money.currency")
    }

    /**
     * Returns a new {@code Money} whose amount is the sum of this and {@code money}'s amounts.
     * Both operands must share the same currency.
     *
     * @param money the addend
     * @return the sum as a new {@code Money} instance
     * @throws dueuno.exceptions.ElementsException if the currencies differ
     */
    Money plus(Money money) {
        checkOperandsCompatibility(money)
        return new Money(this.amount + money.amount, this.currency)
    }

    /**
     * Returns a new {@code Money} whose amount is the difference of this and {@code money}'s amounts.
     * Both operands must share the same currency.
     *
     * @param money the subtrahend
     * @return the difference as a new {@code Money} instance
     * @throws dueuno.exceptions.ElementsException if the currencies differ
     */
    Money minus(Money money) {
        checkOperandsCompatibility(money)
        return new Money(this.amount - money.amount, this.currency)
    }

    /**
     * Returns a new {@code Money} whose amount is the product of this and {@code money}'s amounts.
     * Both operands must share the same currency.
     *
     * @param money the multiplier
     * @return the product as a new {@code Money} instance
     * @throws dueuno.exceptions.ElementsException if the currencies differ
     */
    Money multiply(Money money) {
        checkOperandsCompatibility(money)
        return new Money(this.amount * money.amount, this.currency)
    }

    /**
     * Returns a new {@code Money} whose amount is the quotient of this divided by {@code money}'s amount.
     * Both operands must share the same currency.
     *
     * @param money the divisor
     * @return the quotient as a new {@code Money} instance
     * @throws dueuno.exceptions.ElementsException if the currencies differ
     */
    Money div(Money money) {
        checkOperandsCompatibility(money)
        return new Money(this.amount / money.amount, this.currency)
    }


    //
    // Number
    //

    /**
     * Returns {@code true} if this instance's {@link #amount} equals the given number.
     *
     * @param amount the number to compare with
     * @return {@code true} if the amounts are equal
     */
    Boolean equals(Number amount) {
        return (this.amount == amount)
    }

    /**
     * Returns a new {@code Money} whose amount is this amount plus {@code amount}.
     *
     * @param amount the addend
     * @return the sum as a new {@code Money} instance with the same currency
     */
    Money plus(Number amount) {
        return new Money(this.amount + amount, this.currency)
    }

    /**
     * Returns a new {@code Money} whose amount is this amount minus {@code amount}.
     *
     * @param amount the subtrahend
     * @return the difference as a new {@code Money} instance with the same currency
     */
    Money minus(Number amount) {
        return new Money(this.amount - amount, this.currency)
    }

    /**
     * Returns a new {@code Money} whose amount is this amount multiplied by {@code amount}.
     *
     * @param amount the multiplier
     * @return the product as a new {@code Money} instance with the same currency
     */
    Money multiply(Number amount) {
        return new Money(this.amount * amount, this.currency)
    }

    /**
     * Returns a new {@code Money} whose amount is this amount divided by {@code amount}.
     *
     * @param amount the divisor
     * @return the quotient as a new {@code Money} instance with the same currency
     */
    Money div(Number amount) {
        return new Money(this.amount / amount, this.currency)
    }


    //
    // Quantity
    //

    /**
     * Returns a new {@code Money} whose amount is this amount multiplied by
     * the {@link Quantity#amount} of the given {@code quantity}.
     *
     * @param quantity the quantity to multiply by
     * @return the product as a new {@code Money} instance with the same currency
     */
    Money multiply(Quantity quantity) {
        return new Money(this.amount * quantity.amount, this.currency)
    }
}