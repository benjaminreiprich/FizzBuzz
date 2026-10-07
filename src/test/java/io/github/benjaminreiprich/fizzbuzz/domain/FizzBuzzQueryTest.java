package io.github.benjaminreiprich.fizzbuzz.domain;

import static io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQueries.query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigInteger;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class FizzBuzzQueryTest {

    private static final BigInteger THREE = BigInteger.valueOf(3);
    private static final BigInteger FIVE = BigInteger.valueOf(5);
    private static final BigInteger FIFTEEN = BigInteger.valueOf(15);

    static Stream<Arguments> constructionsWithANullParameter() {
        return Stream.of(
                arguments("int1", (ThrowingCallable) () -> new FizzBuzzQuery(null, FIVE, FIFTEEN, "fizz", "buzz")),
                arguments("int2", (ThrowingCallable) () -> new FizzBuzzQuery(THREE, null, FIFTEEN, "fizz", "buzz")),
                arguments("limit", (ThrowingCallable) () -> new FizzBuzzQuery(THREE, FIVE, null, "fizz", "buzz")),
                arguments("str1", (ThrowingCallable) () -> new FizzBuzzQuery(THREE, FIVE, FIFTEEN, null, "buzz")),
                arguments("str2", (ThrowingCallable) () -> new FizzBuzzQuery(THREE, FIVE, FIFTEEN, "fizz", null)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("constructionsWithANullParameter")
    void should_reject_null_parameter(String parameter, ThrowingCallable construction) {
        assertThatNullPointerException().isThrownBy(construction).withMessage(parameter + " must not be null");
    }

    // The statement accepts any integer: zero, negative and beyond the range of int or long.
    @ParameterizedTest
    @ValueSource(strings = {"0", "-3", "2147483648", "-1000000000000000000000000000000"})
    void should_accept_any_integer(String value) {
        BigInteger integer = new BigInteger(value);

        FizzBuzzQuery query = new FizzBuzzQuery(integer, integer, integer, "fizz", "buzz");

        assertThat(query.int1()).isEqualTo(integer);
        assertThat(query.int2()).isEqualTo(integer);
        assertThat(query.limit()).isEqualTo(integer);
    }

    @Test
    void should_accept_empty_strings() {
        FizzBuzzQuery query = query(3, 5, 15, "", "");

        assertThat(query.str1()).isEmpty();
        assertThat(query.str2()).isEmpty();
    }

    @Test
    void should_be_equal_when_all_parameters_are_equal() {
        assertThat(query(3, 5, 15, "fizz", "buzz"))
                .isEqualTo(new FizzBuzzQuery(new BigInteger("3"), new BigInteger("5"), FIFTEEN, "fizz", "buzz"))
                .hasSameHashCodeAs(query(3, 5, 15, "fizz", "buzz"))
                .isNotEqualTo(query(5, 3, 15, "buzz", "fizz"));
    }
}
