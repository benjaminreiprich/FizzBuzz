package io.github.benjaminreiprich.fizzbuzz.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FizzBuzzGeneratorTest {

    private final FizzBuzzGenerator generator = new FizzBuzzGenerator();

    static Stream<Arguments> examples() {
        return Stream.of(
                arguments(
                        "classic 3/5 up to 15",
                        new FizzBuzzQuery(3, 5, 15, "fizz", "buzz"),
                        terms("1 2 fizz 4 buzz fizz 7 8 fizz buzz 11 fizz 13 14 fizzbuzz")),
                arguments(
                        "equal divisors replace every multiple by str1str2",
                        new FizzBuzzQuery(2, 2, 6, "fizz", "buzz"),
                        terms("1 fizzbuzz 3 fizzbuzz 5 fizzbuzz")),
                arguments(
                        "int1 = 1 replaces every number",
                        new FizzBuzzQuery(1, 3, 6, "fizz", "buzz"),
                        terms("fizz fizz fizzbuzz fizz fizz fizzbuzz")),
                arguments("limit = 1 returns a single term", new FizzBuzzQuery(3, 5, 1, "fizz", "buzz"), terms("1")),
                arguments(
                        "divisors greater than limit keep every number",
                        new FizzBuzzQuery(7, 11, 5, "fizz", "buzz"),
                        terms("1 2 3 4 5")),
                arguments(
                        "non-coprime divisors: multiples of 4 are also multiples of 2",
                        new FizzBuzzQuery(2, 4, 8, "fizz", "buzz"),
                        terms("1 fizz 3 fizzbuzz 5 fizz 7 fizzbuzz")),
                arguments(
                        "str1 always comes first, even when int1 > int2",
                        new FizzBuzzQuery(5, 3, 15, "fizz", "buzz"),
                        terms("1 2 buzz 4 fizz buzz 7 8 buzz fizz 11 buzz 13 14 fizzbuzz")));
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
        List<String> sequence = generator.generate(new FizzBuzzQuery(3, 5, 15, "fizz", "buzz"));

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> sequence.add("16"));
    }
}
