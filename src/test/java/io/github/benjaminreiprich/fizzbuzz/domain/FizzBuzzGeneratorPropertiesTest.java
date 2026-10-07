package io.github.benjaminreiprich.fizzbuzz.domain;

import static io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQueries.query;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Checks the specification's rules on 1,000 random queries per property, rather than on hand-picked examples.
 *
 * <p>The expected result is computed here with plain {@code int} arithmetic, independently of the generator, which
 * works with {@code BigInteger}. Very large divisors are covered by the examples in {@link FizzBuzzGeneratorTest}.
 */
class FizzBuzzGeneratorPropertiesTest {

    private final FizzBuzzGenerator generator = new FizzBuzzGenerator();

    @Property
    void should_return_one_term_per_number_from_1_to_limit(@ForAll("queries") FizzBuzzQuery query) {
        int limit = query.limit().intValue();
        int expectedSize = Math.max(limit, 0);

        assertThat(generator.generate(query)).hasSize(expectedSize);
    }

    @Property
    void should_keep_the_number_when_multiple_of_neither(@ForAll("queries") FizzBuzzQuery query) {
        List<String> sequence = generator.generate(query);

        for (int n = 1; n <= sequence.size(); n++) {
            if (!isMultiple(n, query.int1()) && !isMultiple(n, query.int2())) {
                assertThat(sequence.get(n - 1)).isEqualTo(String.valueOf(n));
            }
        }
    }

    @Property
    void should_return_str1str2_when_number_is_multiple_of_both(@ForAll("queries") FizzBuzzQuery query) {
        List<String> sequence = generator.generate(query);

        for (int n = 1; n <= sequence.size(); n++) {
            if (isMultiple(n, query.int1()) && isMultiple(n, query.int2())) {
                assertThat(sequence.get(n - 1)).isEqualTo(query.str1() + query.str2());
            }
        }
    }

    @Property
    void should_return_str1_when_number_is_multiple_of_int1_only(@ForAll("queries") FizzBuzzQuery query) {
        List<String> sequence = generator.generate(query);

        for (int n = 1; n <= sequence.size(); n++) {
            if (isMultiple(n, query.int1()) && !isMultiple(n, query.int2())) {
                assertThat(sequence.get(n - 1)).isEqualTo(query.str1());
            }
        }
    }

    @Property
    void should_return_str2_when_number_is_multiple_of_int2_only(@ForAll("queries") FizzBuzzQuery query) {
        List<String> sequence = generator.generate(query);

        for (int n = 1; n <= sequence.size(); n++) {
            if (!isMultiple(n, query.int1()) && isMultiple(n, query.int2())) {
                assertThat(sequence.get(n - 1)).isEqualTo(query.str2());
            }
        }
    }

    // Random queries: divisors from -30 to 30 (zero and negatives included) so that every kind of term shows up,
    // limits from -50 to 500 (non-positive limits included), and letters-only strings of 0 to 10 characters.
    // Combinators.combine draws one value from each source and passes the five of them to query(...).
    @Provide
    Arbitrary<FizzBuzzQuery> queries() {
        Arbitrary<Integer> divisors = Arbitraries.integers().between(-30, 30);
        Arbitrary<Integer> limits = Arbitraries.integers().between(-50, 500);
        Arbitrary<String> words = Arbitraries.strings().alpha().ofMaxLength(10);
        return Combinators.combine(divisors, divisors, limits, words, words)
                .as((int1, int2, limit, str1, str2) -> query(int1, int2, limit, str1, str2));
    }

    // The only multiple of 0 is 0, and n is never 0 here.
    private static boolean isMultiple(int n, BigInteger divisor) {
        int intDivisor = divisor.intValue();
        if (intDivisor == 0) {
            return false;
        }
        return n % intDivisor == 0;
    }
}
