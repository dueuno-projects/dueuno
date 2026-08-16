// Must be kept aligned with the server class 'dueuno.types.Type'.
const Type = Object.freeze({
    NA: 'NA',
    BOOL: 'BOOL',
    NUMBER: 'NUMBER',
    STRING: 'STRING',
    MAP: 'MAP',
    LIST: 'LIST',
    DATETIME: 'DATETIME',
    DATE: 'DATE',
    TIME: 'TIME',
});

/**
 * Creates and validates values exchanged by the Elements client and server.
 *
 * A typed value always has exactly two semantic fields:
 * {type: 'STRING', value: 'example'}
 *
 * The client accepts custom type names as strings. Their interpretation is
 * deliberately left to the control that handles them.
 */
class TypedValue {

    static of(type, value) {
        if (typeof type != 'string' || !type) {
            throw new Error('A typed value requires a non-empty string type');
        }

        return {type: type, value: value};
    }

    static empty(type = Type.NA) {
        return TypedValue.of(type, null);
    }

    static bool(value) {
        return TypedValue.of(Type.BOOL, value);
    }

    static number(value) {
        return TypedValue.of(Type.NUMBER, value);
    }

    static string(value) {
        return TypedValue.of(Type.STRING, value);
    }

    static map(value = {}) {
        return TypedValue.of(Type.MAP, value);
    }

    static list(value = []) {
        return TypedValue.of(Type.LIST, value);
    }

    static custom(type, value) {
        return TypedValue.of(type, value);
    }

    static is(value) {
        return value != null
            && typeof value == 'object'
            && typeof value.type == 'string'
            && value.type.length > 0
            && Object.prototype.hasOwnProperty.call(value, 'value');
    }

    static require(value) {
        if (!TypedValue.is(value)) {
            throw new Error('Invalid Dueuno typed value: expected {type, value}');
        }

        return value;
    }

    static isType(value, type) {
        return TypedValue.is(value) && value.type == type;
    }
}
