package io.github.benjaminreiprich.fizzbuzz.infrastructure.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.benjaminreiprich.fizzbuzz.application.port.RequestHits;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class InMemoryRequestStatisticsTest {

    private static final FizzBuzzQuery FIZZ_BUZZ = query(3, 5, "fizz", "buzz");
    private static final FizzBuzzQuery BUZZ_FIZZ = query(5, 3, "buzz", "fizz");

    private final InMemoryRequestStatistics statistics = new InMemoryRequestStatistics();

    @Test
    void should_return_nothing_when_no_request_was_recorded() {
        assertThat(statistics.mostFrequent()).isEmpty();
    }

    @Test
    void should_count_every_hit_of_a_request() {
        statistics.record(FIZZ_BUZZ);
        statistics.record(FIZZ_BUZZ);
        statistics.record(FIZZ_BUZZ);

        assertThat(statistics.mostFrequent()).contains(new RequestHits(FIZZ_BUZZ, 3));
    }

    // Their outputs differ, so they are different requests.
    @Test
    void should_count_requests_with_swapped_parameters_separately() {
        statistics.record(FIZZ_BUZZ);
        statistics.record(BUZZ_FIZZ);
        statistics.record(BUZZ_FIZZ);

        assertThat(statistics.mostFrequent()).contains(new RequestHits(BUZZ_FIZZ, 2));
    }

    @Test
    void should_count_equal_parameters_as_the_same_request() {
        statistics.record(query(5, 3, "buzz", "fizz"));
        statistics.record(
                new FizzBuzzQuery(new BigInteger("05"), new BigInteger("+3"), BigInteger.valueOf(15), "buzz", "fizz"));

        assertThat(statistics.mostFrequent()).contains(new RequestHits(BUZZ_FIZZ, 2));
    }

    @Test
    void should_keep_the_request_that_reached_the_highest_count_first_on_a_tie() {
        statistics.record(FIZZ_BUZZ);
        statistics.record(BUZZ_FIZZ);
        statistics.record(BUZZ_FIZZ); // BUZZ_FIZZ reaches 2 first
        statistics.record(FIZZ_BUZZ); // FIZZ_BUZZ only ties

        assertThat(statistics.mostFrequent()).contains(new RequestHits(BUZZ_FIZZ, 2));
    }

    @Test
    void should_switch_to_a_request_that_overtakes_the_leader() {
        statistics.record(FIZZ_BUZZ);
        statistics.record(BUZZ_FIZZ);
        statistics.record(BUZZ_FIZZ);
        statistics.record(FIZZ_BUZZ);
        statistics.record(FIZZ_BUZZ);

        assertThat(statistics.mostFrequent()).contains(new RequestHits(FIZZ_BUZZ, 3));
    }

    private static FizzBuzzQuery query(int int1, int int2, String str1, String str2) {
        return new FizzBuzzQuery(
                BigInteger.valueOf(int1), BigInteger.valueOf(int2), BigInteger.valueOf(15), str1, str2);
    }
}
