package io.github.benjaminreiprich.fizzbuzz.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FizzBuzzQueryTest {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void should_reject_int1_lower_than_one(int int1) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new FizzBuzzQuery(int1, 5, 15, "fizz", "buzz"))
                .withMessage("int1 must be at least 1, but was " + int1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void should_reject_int2_lower_than_one(int int2) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new FizzBuzzQuery(3, int2, 15, "fizz", "buzz"))
                .withMessage("int2 must be at least 1, but was " + int2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void should_reject_limit_lower_than_one(int limit) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new FizzBuzzQuery(3, 5, limit, "fizz", "buzz"))
                .withMessage("limit must be at least 1, but was " + limit);
    }

    @Test
    void should_reject_null_str1() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FizzBuzzQuery(3, 5, 15, null, "buzz"))
                .withMessage("str1 must not be null");
    }

    @Test
    void should_reject_null_str2() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FizzBuzzQuery(3, 5, 15, "fizz", null))
                .withMessage("str2 must not be null");
    }

    @Test
    void should_reject_empty_str1() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new FizzBuzzQuery(3, 5, 15, "", "buzz"))
                .withMessage("str1 must not be empty");
    }

    @Test
    void should_reject_empty_str2() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new FizzBuzzQuery(3, 5, 15, "fizz", ""))
                .withMessage("str2 must not be empty");
    }

    @Test
    void should_accept_smallest_valid_values() {
        FizzBuzzQuery query = new FizzBuzzQuery(1, 1, 1, "a", "b");

        assertThat(query.int1()).isEqualTo(1);
        assertThat(query.int2()).isEqualTo(1);
        assertThat(query.limit()).isEqualTo(1);
        assertThat(query.str1()).isEqualTo("a");
        assertThat(query.str2()).isEqualTo("b");
    }

    // The specification only requires non-empty strings; a space is a legitimate replacement.
    @Test
    void should_accept_whitespace_only_strings() {
        FizzBuzzQuery query = new FizzBuzzQuery(3, 5, 15, " ", " ");

        assertThat(query.str1()).isEqualTo(" ");
        assertThat(query.str2()).isEqualTo(" ");
    }

    @Test
    void should_be_equal_when_all_parameters_are_equal() {
        assertThat(new FizzBuzzQuery(3, 5, 15, "fizz", "buzz"))
                .isEqualTo(new FizzBuzzQuery(3, 5, 15, "fizz", "buzz"))
                .hasSameHashCodeAs(new FizzBuzzQuery(3, 5, 15, "fizz", "buzz"))
                .isNotEqualTo(new FizzBuzzQuery(5, 3, 15, "buzz", "fizz"));
    }
}
