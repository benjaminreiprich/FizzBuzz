package io.github.benjaminreiprich.fizzbuzz.infrastructure.statistics;

import io.github.benjaminreiprich.fizzbuzz.application.port.RequestHits;
import io.github.benjaminreiprich.fizzbuzz.application.port.RequestStatistics;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/**
 * Thread-safe without a global lock: each request has its own atomic counter, and the leader is replaced with a
 * compare-and-set only when a counter strictly exceeds it, so {@link #mostFrequent()} is a single read. Data is lost on
 * restart and not shared between instances (ADR-0009).
 */
@Component
public class InMemoryRequestStatistics implements RequestStatistics {

    private final ConcurrentMap<FizzBuzzQuery, AtomicLong> hitsByQuery = new ConcurrentHashMap<>();
    private final AtomicReference<RequestHits> leader = new AtomicReference<>();

    @Override
    public void record(FizzBuzzQuery query) {
        // AtomicLong rather than LongAdder: the exact count returned by this increment is compared to the leader.
        long hits = hitsByQuery.computeIfAbsent(query, key -> new AtomicLong()).incrementAndGet();

        // "Strictly exceeds" keeps the request that reached a count first on a tie. A thread holding an outdated count
        // gives up, so the leader's count never goes backwards.
        RequestHits current;
        do {
            current = leader.get();
            if (current != null && hits <= current.hits()) {
                return;
            }
        } while (!leader.compareAndSet(current, new RequestHits(query, hits)));
    }

    @Override
    public Optional<RequestHits> mostFrequent() {
        return Optional.ofNullable(leader.get());
    }
}
