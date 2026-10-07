package io.github.benjaminreiprich.fizzbuzz.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Generates FizzBuzz sequences. Stateless, hence thread-safe. */
public final class FizzBuzzGenerator {

    /**
     * Returns the numbers from 1 to {@code query.limit()}, where multiples of {@code int1} are replaced by
     * {@code str1}, multiples of {@code int2} by {@code str2}, and multiples of both by {@code str1 + str2}.
     *
     * @param query the parameters of the sequence
     * @return an unmodifiable list of exactly {@code query.limit()} terms
     */
    public List<String> generate(FizzBuzzQuery query) {
        List<String> sequence = new ArrayList<>(query.limit());
        for (int n = 1; n <= query.limit(); n++) {
            sequence.add(termFor(n, query));
        }
        return Collections.unmodifiableList(sequence);
    }

    private static String termFor(int n, FizzBuzzQuery query) {
        boolean multipleOfInt1 = n % query.int1() == 0;
        boolean multipleOfInt2 = n % query.int2() == 0;
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
