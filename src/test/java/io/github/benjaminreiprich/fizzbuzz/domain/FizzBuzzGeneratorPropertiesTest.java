package io.github.benjaminreiprich.fizzbuzz.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Tuple;

/**
 * Checks the specification's rules on randomly generated queries rather than hand-picked examples.
 *
 * <p>Divisibility is computed here with {@link BigInteger} arithmetic, independently of the generator's
 * implementation.
 */
class FizzBuzzGeneratorPropertiesTest {

    private final FizzBuzzGenerator generator = new FizzBuzzGenerator();

    @Property
    void should_return_one_term_per_number_from_1_to_limit(@ForAll("queries") FizzBuzzQuery query) {
        int expectedSize = query.limit().signum() > 0 ? query.limit().intValueExact() : 0;

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

    // Mostly small divisors (zero and negatives included), so that every kind of term shows up; any int
    // and far beyond, since the statement accepts any integer. Limits include non-positive values.
    @Provide
    Arbitrary<FizzBuzzQuery> queries() {
        BigInteger tenToThe40 = BigInteger.TEN.pow(40);
        Arbitrary<BigInteger> smallDivisors =
                Arbitraries.integers().between(-30, 30).map(BigInteger::valueOf);
        Arbitrary<BigInteger> intDivisors = Arbitraries.integers().map(BigInteger::valueOf);
        Arbitrary<BigInteger> hugeDivisors = Arbitraries.bigIntegers().between(tenToThe40.negate(), tenToThe40);
        Arbitrary<BigInteger> divisors = Arbitraries.frequencyOf(
                Tuple.of(8, smallDivisors), Tuple.of(1, intDivisors), Tuple.of(1, hugeDivisors));
        Arbitrary<BigInteger> limits = Arbitraries.integers().between(-50, 500).map(BigInteger::valueOf);
        Arbitrary<String> words = Arbitraries.strings().alpha().ofMinLength(0).ofMaxLength(10);
        return Combinators.combine(divisors, divisors, limits, words, words).as(FizzBuzzQuery::new);
    }

    // Only 0 is a multiple of 0, and n is never 0 here.
    private static boolean isMultiple(int n, BigInteger divisor) {
        return divisor.signum() != 0 && BigInteger.valueOf(n).remainder(divisor).signum() == 0;
    }
}
