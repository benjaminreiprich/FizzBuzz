package io.github.benjaminreiprich.fizzbuzz.domain;

import java.math.BigInteger;
import java.util.Objects;

/**
 * The five parameters of a FizzBuzz computation.
 *
 * <p>The statement accepts any integers and any strings, so the only rule is that every parameter is present. Integers
 * are {@link BigInteger} because the statement puts no bound on them, and request statistics must return the
 * parameters exactly as received. Operational limits (maximum {@code limit}, maximum string length) are configuration
 * and are checked at the API boundary, see ADR-0005.
 *
 * <p>Two queries are equal when all their parameters are equal, which makes this record usable as a key for request
 * statistics.
 *
 * @param int1 integer whose multiples are replaced by {@code str1}
 * @param int2 integer whose multiples are replaced by {@code str2}
 * @param limit last number of the sequence, inclusive; the sequence is empty when it is lower than 1
 * @param str1 replacement for multiples of {@code int1}
 * @param str2 replacement for multiples of {@code int2}
 * @throws NullPointerException if a parameter is null
 */
public record FizzBuzzQuery(BigInteger int1, BigInteger int2, BigInteger limit, String str1, String str2) {

    public FizzBuzzQuery {
        Objects.requireNonNull(int1, "int1 must not be null");
        Objects.requireNonNull(int2, "int2 must not be null");
        Objects.requireNonNull(limit, "limit must not be null");
        Objects.requireNonNull(str1, "str1 must not be null");
        Objects.requireNonNull(str2, "str2 must not be null");
    }
}
