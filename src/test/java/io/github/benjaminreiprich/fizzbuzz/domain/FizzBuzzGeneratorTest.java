package io.github.benjaminreiprich.fizzbuzz.domain;

import static io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQueries.query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FizzBuzzGeneratorTest {

    private static final BigInteger TEN_TO_THE_30 = BigInteger.TEN.pow(30);

    private final FizzBuzzGenerator generator = new FizzBuzzGenerator();

    static Stream<Arguments> examples() {
        return Stream.of(
                arguments(
                        "classic 3/5 up to 15",
                        query(3, 5, 15, "fizz", "buzz"),
                        terms("1 2 fizz 4 buzz fizz 7 8 fizz buzz 11 fizz 13 14 fizzbuzz")),
                arguments(
                        "equal divisors replace every multiple by str1str2",
                        query(2, 2, 6, "fizz", "buzz"),
                        terms("1 fizzbuzz 3 fizzbuzz 5 fizzbuzz")),
                arguments(
                        "int1 = 1 replaces every number",
                        query(1, 3, 6, "fizz", "buzz"),
                        terms("fizz fizz fizzbuzz fizz fizz fizzbuzz")),
                arguments("limit = 1 returns a single term", query(3, 5, 1, "fizz", "buzz"), terms("1")),
                arguments(
                        "divisors greater than limit keep every number",
                        query(7, 11, 5, "fizz", "buzz"),
                        terms("1 2 3 4 5")),
                arguments(
                        "multiples of both are multiples of the LCM, not of the product",
                        query(4, 6, 12, "fizz", "buzz"),
                        terms("1 2 3 fizz 5 buzz 7 fizz 9 10 11 fizzbuzz")),
                arguments(
                        "str1 always comes first, even when int1 > int2",
                        query(5, 3, 15, "fizz", "buzz"),
                        terms("1 2 buzz 4 fizz buzz 7 8 buzz fizz 11 buzz 13 14 fizzbuzz")),
                arguments(
                        "a negative divisor has the same multiples as its absolute value",
                        query(-3, 5, 15, "fizz", "buzz"),
                        terms("1 2 fizz 4 buzz fizz 7 8 fizz buzz 11 fizz 13 14 fizzbuzz")),
                arguments(
                        "zero divisor replaces nothing, as 0 is its only multiple",
                        query(0, 5, 10, "fizz", "buzz"),
                        terms("1 2 3 4 buzz 6 7 8 9 buzz")),
                arguments("two zero divisors replace nothing", query(0, 0, 3, "fizz", "buzz"), terms("1 2 3")),
                arguments(
                        "smallest int divisor has no multiple in range",
                        query(Integer.MIN_VALUE, 5, 5, "fizz", "buzz"),
                        terms("1 2 3 4 buzz")),
                arguments(
                        "divisor just above the int range has no multiple in range",
                        query(1L + Integer.MAX_VALUE, 5, 5, "fizz", "buzz"),
                        terms("1 2 3 4 buzz")),
                arguments(
                        "divisor of any size is accepted",
                        new FizzBuzzQuery(TEN_TO_THE_30, TEN_TO_THE_30.negate(), BigInteger.valueOf(3), "fizz", "buzz"),
                        terms("1 2 3")),
                arguments(
                        "empty strings replace multiples by an empty term",
                        query(2, 3, 6, "", "buzz"),
                        List.of("1", "", "buzz", "", "5", "buzz")),
                arguments("limit = 0 returns an empty list", query(3, 5, 0, "fizz", "buzz"), List.of()),
                arguments("negative limit returns an empty list", query(3, 5, -5, "fizz", "buzz"), List.of()),
                arguments(
                        "limit of any negative size returns an empty list",
                        new FizzBuzzQuery(
                                BigInteger.valueOf(3), BigInteger.valueOf(5), TEN_TO_THE_30.negate(), "fizz", "buzz"),
                        List.of()));
    }

    // Space-separated, so that each expected sequence reads like the specification example.
    private static List<String> terms(String spaceSeparatedTerms) {
        return List.of(spaceSeparatedTerms.split(" "));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("examples")
    void should_generate_expected_sequence(String scenario, FizzBuzzQuery query, List<String> expected) {
        assertThat(generator.generate(query)).containsExactlyElementsOf(expected);
    }

    @Test
    void should_return_unmodifiable_list() {
        List<String> sequence = generator.generate(query(3, 5, 15, "fizz", "buzz"));

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> sequence.add("16"));
    }

    // A Java list cannot hold more than Integer.MAX_VALUE elements; the API caps limit far below this.
    @Test
    void should_reject_limit_that_cannot_fit_in_a_list() {
        FizzBuzzQuery query = query(3, 5, 1L + Integer.MAX_VALUE, "fizz", "buzz");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> generator.generate(query))
                .withMessage("limit must not exceed 2147483647 to fit in a list, but was 2147483648");
    }
}
