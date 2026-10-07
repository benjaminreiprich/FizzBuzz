package io.github.benjaminreiprich.fizzbuzz.domain;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Generates FizzBuzz sequences. Stateless, hence thread-safe. */
public final class FizzBuzzGenerator {

    // A Java list cannot hold more elements than this.
    private static final BigInteger MAX_LIST_SIZE = BigInteger.valueOf(Integer.MAX_VALUE);

    /**
     * Returns the numbers from 1 to {@code query.limit()}, where multiples of {@code int1} are replaced by
     * {@code str1}, multiples of {@code int2} by {@code str2}, and multiples of both by {@code str1 + str2}.
     *
     * @param query the parameters of the sequence
     * @return an unmodifiable list with one term per number from 1 to {@code limit}; empty if {@code limit < 1}
     * @throws IllegalArgumentException if {@code limit} exceeds {@link Integer#MAX_VALUE}, the maximum size of a list
     */
    public List<String> generate(FizzBuzzQuery query) {
        int size = sequenceSize(query.limit());
        List<String> sequence = new ArrayList<>(size);
        for (int n = 1; n <= size; n++) {
            sequence.add(termFor(n, query));
        }
        return Collections.unmodifiableList(sequence);
    }

    private static int sequenceSize(BigInteger limit) {
        // "From 1 to limit" contains no number when limit is lower than 1.
        boolean limitIsLowerThanOne = limit.compareTo(BigInteger.ONE) < 0;
        if (limitIsLowerThanOne) {
            return 0;
        }
        boolean limitFitsInAList = limit.compareTo(MAX_LIST_SIZE) <= 0;
        if (!limitFitsInAList) {
            throw new IllegalArgumentException(
                    "limit must not exceed " + MAX_LIST_SIZE + " to fit in a list, but was " + limit);
        }
        return limit.intValueExact();
    }

    private static String termFor(int n, FizzBuzzQuery query) {
        boolean multipleOfInt1 = isMultiple(n, query.int1());
        boolean multipleOfInt2 = isMultiple(n, query.int2());
        if (multipleOfInt1 && multipleOfInt2) {
            return query.str1() + query.str2();
        }
        if (multipleOfInt1) {
            return query.str1();
        }
        if (multipleOfInt2) {
            return query.str2();
        }
        return String.valueOf(n);
    }

    // The divisor can be any integer, so the check is done with BigInteger. The only multiple of 0 is 0 itself,
    // and n is never 0 here, so 0 replaces nothing. Negative divisors work as expected: 6 is a multiple of -3.
    private static boolean isMultiple(int n, BigInteger divisor) {
        if (divisor.equals(BigInteger.ZERO)) {
            return false;
        }
        BigInteger remainder = BigInteger.valueOf(n).remainder(divisor);
        return remainder.equals(BigInteger.ZERO);
    }
}
