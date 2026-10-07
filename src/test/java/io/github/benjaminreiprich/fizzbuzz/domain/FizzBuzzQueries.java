package io.github.benjaminreiprich.fizzbuzz.domain;

import java.math.BigInteger;

/** Builds queries from {@code long} literals, so that tests do not drown in {@code BigInteger.valueOf}. */
final class FizzBuzzQueries {

    private FizzBuzzQueries() {}

    static FizzBuzzQuery query(long int1, long int2, long limit, String str1, String str2) {
        return new FizzBuzzQuery(
                BigInteger.valueOf(int1), BigInteger.valueOf(int2), BigInteger.valueOf(limit), str1, str2);
    }
}
