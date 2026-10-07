package io.github.benjaminreiprich.fizzbuzz.infrastructure.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.benjaminreiprich.fizzbuzz.application.port.RequestHits;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Compares the store with a direct reading of its specification on random request sequences: the most frequent
 * request is the one with the highest count and, on a tie, the first one to reach that count.
 */
class InMemoryRequestStatisticsPropertiesTest {

    @Property
    void should_report_the_most_frequent_request_of_any_sequence(
            @ForAll("requestSequences") List<FizzBuzzQuery> requests) {
        InMemoryRequestStatistics statistics = new InMemoryRequestStatistics();

        requests.forEach(statistics::record);

        assertThat(statistics.mostFrequent()).isEqualTo(expectedMostFrequent(requests));
    }

    private static Optional<RequestHits> expectedMostFrequent(List<FizzBuzzQuery> requests) {
        Map<FizzBuzzQuery, Long> totals = new HashMap<>();
        requests.forEach(request -> totals.merge(request, 1L, Long::sum));
        if (totals.isEmpty()) {
            return Optional.empty();
        }
        long highest = Collections.max(totals.values());

        // The winner is the first request whose running count reaches the highest total.
        Map<FizzBuzzQuery, Long> running = new HashMap<>();
        for (FizzBuzzQuery request : requests) {
            if (running.merge(request, 1L, Long::sum) == highest) {
                return Optional.of(new RequestHits(request, highest));
            }
        }
        throw new AssertionError("unreachable: some request reaches the highest total");
    }

    // A small pool of distinct requests, so that sequences contain many repeats and ties.
    @Provide
    Arbitrary<List<FizzBuzzQuery>> requestSequences() {
        Arbitrary<FizzBuzzQuery> requests = Arbitraries.integers()
                .between(1, 4)
                .map(int1 ->
                        new FizzBuzzQuery(BigInteger.valueOf(int1), BigInteger.TWO, BigInteger.TEN, "fizz", "buzz"));
        return requests.list().ofMaxSize(200);
    }
}
