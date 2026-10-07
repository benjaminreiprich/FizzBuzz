package io.github.benjaminreiprich.fizzbuzz.application.port;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class RequestHitsTest {

    private static final FizzBuzzQuery QUERY =
            new FizzBuzzQuery(BigInteger.valueOf(3), BigInteger.valueOf(5), BigInteger.TEN, "fizz", "buzz");

    @Test
    void should_reject_a_request_counted_less_than_once() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new RequestHits(QUERY, 0))
                .withMessage("hits must be at least 1, but was 0");
    }

    @Test
    void should_reject_a_missing_query() {
        assertThatNullPointerException()
                .isThrownBy(() -> new RequestHits(null, 1))
                .withMessage("query must not be null");
    }
}
