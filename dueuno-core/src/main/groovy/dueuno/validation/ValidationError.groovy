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
package dueuno.validation

import groovy.transform.CompileStatic

/**
 * Centralises the Grails/GORM constraint-violation error codes used throughout the
 * Elements framework.
 * <p>
 * Each constant holds the i18n message key that GORM places in a
 * {@link org.springframework.validation.Errors} object when a constraint fails.
 * Using these constants instead of raw string literals prevents typos and makes
 * constraint-check code self-documenting.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
class ValidationError {

    /** Error code produced when a {@code nullable: false} constraint is violated (field is required). */
    static final String IS_REQUIRED = 'nullable'

    /** Error code produced when a {@code unique} constraint is violated (duplicate value). */
    static final String IS_NOT_UNIQUE = 'unique'

    /** Error code produced when a {@code blank: false} constraint is violated (empty string). */
    static final String IS_BLANK = 'blank'

    /** Error code produced when a value falls below the lower bound of a {@code range} constraint. */
    static final String RANGE_TOO_SMALL = 'range.toosmall'

    /** Error code produced when a value exceeds the upper bound of a {@code range} constraint. */
    static final String RANGE_TOO_BIG = 'range.toobig'

    /** Error code produced when a {@code notEqual} constraint is violated. */
    static final String NOT_EQUAL = 'notEqual'

    /** Error code produced when a value is absent from an {@code inList} constraint. */
    static final String NOT_IN_LIST = 'not.inList'

    /** Error code produced when a value exceeds the {@code max} constraint. */
    static final String MAX_EXCEEDED = 'max.exceeded'

    /** Error code produced when a string or collection exceeds the {@code maxSize} constraint. */
    static final String MAX_SIZE_EXCEEDED = 'maxSize.exceeded'

    /** Error code produced when a value does not meet the {@code min} constraint. */
    static final String MIN_NOT_MET = 'min.notmet'

    /** Error code produced when a string or collection does not meet the {@code minSize} constraint. */
    static final String MIN_SIZE_NOT_MET = 'minSize.notmet'

    /** Error code produced when a {@code url} constraint is violated (malformed URL). */
    static final String INVALID_URL = 'url.invalid'

    /** Error code produced when an {@code email} constraint is violated (malformed e-mail address). */
    static final String INVALID_EMAIL = 'email.invalid'

    /** Error code produced when a {@code creditCard} constraint is violated (invalid card number). */
    static final String INVALID_CREDIT_CARD = 'creditCard.invalid'

    /** Error code produced when a {@code matches} constraint is violated (regex mismatch). */
    static final String INVALID_MATCH = 'matches.invalid'

}