package io.github.benjaminreiprich.fizzbuzz.domain;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;

/** Generates FizzBuzz sequences. Stateless, hence thread-safe. */
public final class FizzBuzzGenerator {

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
        int size = sizeOf(query.limit());
        IntPredicate isMultipleOfInt1 = multiplesOf(query.int1());
        IntPredicate isMultipleOfInt2 = multiplesOf(query.int2());

        List<String> sequence = new ArrayList<>(size);
        for (int n = 1; n <= size; n++) {
            sequence.add(termFor(n, isMultipleOfInt1.test(n), isMultipleOfInt2.test(n), query));
        }
        return Collections.unmodifiableList(sequence);
    }

    private static int sizeOf(BigInteger limit) {
        if (limit.signum() <= 0) {
            return 0;
        }
        if (limit.compareTo(MAX_LIST_SIZE) > 0) {
            throw new IllegalArgumentException(
                    "limit must not exceed " + MAX_LIST_SIZE + " to fit in a list, but was " + limit);
        }
        return limit.intValueExact();
    }

    // Numbers range from 1 to at most Integer.MAX_VALUE. Zero has no multiple in that range (its only multiple
    // is 0), nor has a divisor outside the int range (its smallest positive multiple exceeds Integer.MAX_VALUE).
    // Any other divisor fits in an int, so the loop never does BigInteger arithmetic.
    private static IntPredicate multiplesOf(BigInteger divisor) {
        boolean fitsInInt = divisor.bitLength() < Integer.SIZE;
        if (divisor.signum() == 0 || !fitsInInt) {
            return n -> false;
        }
        int intDivisor = divisor.intValueExact();
        return n -> n % intDivisor == 0;
    }

    private static String termFor(int n, boolean multipleOfInt1, boolean multipleOfInt2, FizzBuzzQuery query) {
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
}
