package io.github.benjaminreiprich.fizzbuzz.domain;

import java.util.Objects;

/**
 * Parameters of a FizzBuzz computation, valid by construction.
 *
 * <p>Only the rules without which the computation is meaningless are enforced here. Operational limits (maximum
 * {@code limit}, maximum string length) are configuration and are checked at the API boundary, see ADR-0004.
 *
 * <p>Two queries are equal when all their parameters are equal, which makes this record usable as a key for request
 * statistics.
 *
 * @param int1 divisor whose multiples are replaced by {@code str1}; at least 1
 * @param int2 divisor whose multiples are replaced by {@code str2}; at least 1
 * @param limit last number of the sequence, inclusive; at least 1
 * @param str1 replacement for multiples of {@code int1}; not empty
 * @param str2 replacement for multiples of {@code int2}; not empty
 * @throws IllegalArgumentException if a number is lower than 1 or a string is empty
 * @throws NullPointerException if a string is null
 */
public record FizzBuzzQuery(int int1, int int2, int limit, String str1, String str2) {

    public FizzBuzzQuery {
        requireAtLeastOne(int1, "int1");
        requireAtLeastOne(int2, "int2");
        requireAtLeastOne(limit, "limit");
        requireNotEmpty(str1, "str1");
        requireNotEmpty(str2, "str2");
    }

    private static void requireAtLeastOne(int value, String name) {
        if (value < 1) {
            throw new IllegalArgumentException(name + " must be at least 1, but was " + value);
        }
    }

    private static void requireNotEmpty(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
    }
}
