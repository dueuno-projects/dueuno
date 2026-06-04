package dueuno.validation

import groovy.transform.CompileStatic

@CompileStatic
class ValidationError {

    static final String IS_REQUIRED = 'nullable'
    static final String IS_NOT_UNIQUE = 'unique'
    static final String IS_BLANK = 'blank'

    static final String RANGE_TOO_SMALL = 'range.toosmall'
    static final String RANGE_TOO_BIG = 'range.toobig'

    static final String MATCHES_INVALID = 'matches.invalid'

    static final String NOT_EQUAL = 'notEqual'
    static final String NOT_IN_LIST = 'not.inList'

    static final String MAX_EXCEEDED = 'max.exceeded'
    static final String MAX_SIZE_EXCEEDED = 'maxSize.exceeded'

    static final String MIN_NOT_MET = 'min.notmet'
    static final String MIN_SIZE_NOT_MET = 'minSize.notmet'

    static final String URL_INVALID = 'url.invalid'
    static final String EMAIL_INVALID = 'email.invalid'
    static final String CREDIT_CARD_INVALID = 'creditCard.invalid'

}