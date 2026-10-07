package io.github.benjaminreiprich.fizzbuzz.domain;

import static io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQueries.query;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FizzBuzzQueryTest {

    private static final BigInteger THREE = BigInteger.valueOf(3);
    private static final BigInteger FIVE = BigInteger.valueOf(5);
    private static final BigInteger FIFTEEN = BigInteger.valueOf(15);

    @Test
    void should_reject_null_int1() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FizzBuzzQuery(null, FIVE, FIFTEEN, "fizz", "buzz"))
                .withMessage("int1 must not be null");
    }

    @Test
    void should_reject_null_int2() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FizzBuzzQuery(THREE, null, FIFTEEN, "fizz", "buzz"))
                .withMessage("int2 must not be null");
    }

    @Test
    void should_reject_null_limit() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FizzBuzzQuery(THREE, FIVE, null, "fizz", "buzz"))
                .withMessage("limit must not be null");
    }

    @Test
    void should_reject_null_str1() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FizzBuzzQuery(THREE, FIVE, FIFTEEN, null, "buzz"))
                .withMessage("str1 must not be null");
    }

    @Test
    void should_reject_null_str2() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FizzBuzzQuery(THREE, FIVE, FIFTEEN, "fizz", null))
                .withMessage("str2 must not be null");
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
