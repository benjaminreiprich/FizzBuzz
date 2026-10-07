package io.github.benjaminreiprich.fizzbuzz.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/** Checks the specification's rules on randomly generated queries rather than hand-picked examples. */
class FizzBuzzGeneratorPropertiesTest {

    private final FizzBuzzGenerator generator = new FizzBuzzGenerator();

    @Property
    void should_return_exactly_limit_terms(@ForAll("queries") FizzBuzzQuery query) {
        assertThat(generator.generate(query)).hasSize(query.limit());
    }

    @Property
    void should_keep_the_number_when_multiple_of_neither(@ForAll("queries") FizzBuzzQuery query) {
        List<String> sequence = generator.generate(query);

        for (int n = 1; n <= query.limit(); n++) {
            if (!isMultiple(n, query.int1()) && !isMultiple(n, query.int2())) {
                assertThat(sequence.get(n - 1)).isEqualTo(String.valueOf(n));
            }
        }
    }

    @Property
    void should_return_str1str2_when_number_is_multiple_of_both(@ForAll("queries") FizzBuzzQuery query) {
        List<String> sequence = generator.generate(query);

        for (int n = 1; n <= query.limit(); n++) {
            if (isMultiple(n, query.int1()) && isMultiple(n, query.int2())) {
                assertThat(sequence.get(n - 1)).isEqualTo(query.str1() + query.str2());
            }
        }
    }

    @Property
    void should_return_str1_when_number_is_multiple_of_int1_only(@ForAll("queries") FizzBuzzQuery query) {
        List<String> sequence = generator.generate(query);

        for (int n = 1; n <= query.limit(); n++) {
            if (isMultiple(n, query.int1()) && !isMultiple(n, query.int2())) {
                assertThat(sequence.get(n - 1)).isEqualTo(query.str1());
            }
        }
    }

    @Property
    void should_return_str2_when_number_is_multiple_of_int2_only(@ForAll("queries") FizzBuzzQuery query) {
        List<String> sequence = generator.generate(query);

        for (int n = 1; n <= query.limit(); n++) {
            if (!isMultiple(n, query.int1()) && isMultiple(n, query.int2())) {
                assertThat(sequence.get(n - 1)).isEqualTo(query.str2());
            }
        }
    }

    // Small divisors relative to the limit, so that every kind of term shows up in most samples.
    @Provide
    Arbitrary<FizzBuzzQuery> queries() {
        Arbitrary<Integer> divisors = Arbitraries.integers().between(1, 30);
        Arbitrary<Integer> limits = Arbitraries.integers().between(1, 500);
        Arbitrary<String> words = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10);
        return Combinators.combine(divisors, divisors, limits, words, words).as(FizzBuzzQuery::new);
    }

    private static boolean isMultiple(int n, int divisor) {
        return n % divisor == 0;
    }
}
