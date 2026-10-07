package io.github.benjaminreiprich.fizzbuzz.infrastructure.statistics;

import io.github.benjaminreiprich.fizzbuzz.application.port.RequestHits;
import io.github.benjaminreiprich.fizzbuzz.application.port.RequestStatistics;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Keeps the count of every request in memory, and remembers the most frequent one so that reading it is immediate.
 * Data is lost on restart and not shared between instances (ADR-0009).
 *
 * <p>Both methods are {@code synchronized}: one thread at a time updates the counts and the leader together, so they
 * can never disagree. The lock is held for well under a microsecond, negligible next to an HTTP request (ADR-0014).
 */
@Component
public class InMemoryRequestStatistics implements RequestStatistics {

    private final Map<FizzBuzzQuery, Long> hitsByQuery = new HashMap<>();

    // The most frequent request so far; null until the first request is recorded.
    private RequestHits leader;

    @Override
    public synchronized void record(FizzBuzzQuery query) {
        long hits = hitsByQuery.getOrDefault(query, 0L) + 1;
        hitsByQuery.put(query, hits);

        // Strictly greater: on a tie, the request that reached this count first stays the leader.
        if (leader == null || hits > leader.hits()) {
            leader = new RequestHits(query, hits);
        }
    }

    @Override
    public synchronized Optional<RequestHits> mostFrequent() {
        return Optional.ofNullable(leader);
    }
}
