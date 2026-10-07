package io.github.benjaminreiprich.fizzbuzz.infrastructure.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.benjaminreiprich.fizzbuzz.application.port.RequestHits;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.math.BigInteger;
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

        for (FizzBuzzQuery request : requests) {
            statistics.record(request);
        }

        assertThat(statistics.mostFrequent()).isEqualTo(expectedMostFrequent(requests));
    }

    private static Optional<RequestHits> expectedMostFrequent(List<FizzBuzzQuery> requests) {
        if (requests.isEmpty()) {
            return Optional.empty();
        }

        // Step 1: count how many times each request appears in the whole sequence.
        Map<FizzBuzzQuery, Long> totals = new HashMap<>();
        for (FizzBuzzQuery request : requests) {
            totals.put(request, totals.getOrDefault(request, 0L) + 1);
        }

        // Step 2: find the highest of these counts.
        long highestTotal = 0;
        for (long total : totals.values()) {
            if (total > highestTotal) {
                highestTotal = total;
            }
        }

        // Step 3: replay the sequence; the winner is the first request whose count reaches the highest total.
        Map<FizzBuzzQuery, Long> countsSoFar = new HashMap<>();
        for (FizzBuzzQuery request : requests) {
            long countSoFar = countsSoFar.getOrDefault(request, 0L) + 1;
            countsSoFar.put(request, countSoFar);
            if (countSoFar == highestTotal) {
                return Optional.of(new RequestHits(request, highestTotal));
            }
        }
        throw new AssertionError("unreachable: some request always reaches the highest total");
    }

    // Sequences of up to 200 requests picked among only 4 distinct ones, so that repeats and ties are frequent.
    @Provide
    Arbitrary<List<FizzBuzzQuery>> requestSequences() {
        Arbitrary<FizzBuzzQuery> requests =
                Arbitraries.integers().between(1, 4).map(InMemoryRequestStatisticsPropertiesTest::requestNumber);
        return requests.list().ofMaxSize(200);
    }

    private static FizzBuzzQuery requestNumber(int number) {
        return new FizzBuzzQuery(BigInteger.valueOf(number), BigInteger.TWO, BigInteger.TEN, "fizz", "buzz");
    }
}
