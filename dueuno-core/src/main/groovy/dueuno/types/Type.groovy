package dueuno.types

import groovy.transform.CompileStatic

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Enumerates the primitive value types recognised by the Elements framework.
 * <p>
 * Each constant maps a logical type name to its corresponding Java/Groovy {@link Class},
 * allowing the framework to perform type-safe rendering, serialisation, and form binding
 * without relying on raw {@code Class} comparisons at call sites.
 * </p>
 *
 * @author Gianluca Sartori
 */
@CompileStatic
enum Type {

    /** No type / type not applicable. Maps to {@code null}. */
    NA(null),

    /** Boolean ({@code true}/{@code false}) value. Maps to {@link Boolean}. */
    BOOL(Boolean),

    /** Numeric value (integer or decimal). Maps to {@link Number}. */
    NUMBER(Number),

    /** Plain text string. Maps to {@link String}. */
    TEXT(String),

    /** Key-value map. Maps to {@link Map}. */
    MAP(Map),

    /** Ordered list of values. Maps to {@link List}. */
    LIST(List),

    /** Combined date and time value. Maps to {@link LocalDateTime}. */
    DATETIME(LocalDateTime),

    /** Date-only value. Maps to {@link LocalDate}. */
    DATE(LocalDate),

    /** Time-only value. Maps to {@link LocalTime}. */
    TIME(LocalTime)

    /** The Java class corresponding to this type constant, or {@code null} for {@link #NA}. */
    final Class clazz

    /**
     * Creates a {@code Type} constant bound to the given Java class.
     *
     * @param clazz the Java class that represents this type, or {@code null} for {@link #NA}
     */
    Type(Class clazz) {
        this.clazz = clazz
    }

    /**
     * Returns the enum constant name (e.g. {@code "BOOL"}, {@code "NUMBER"}).
     *
     * @return the name of this constant
     */
    String toString() {
        return name()
    }
}