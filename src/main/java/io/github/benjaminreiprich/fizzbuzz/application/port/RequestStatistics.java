package io.github.benjaminreiprich.fizzbuzz.application.port;

import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.util.Optional;

/**
 * Counts FizzBuzz requests and tells which one was made most often.
 *
 * <p>The project's only abstraction, on purpose: the default in-memory store is lost on restart and not shared between
 * instances, and this port lets a shared store replace it without touching the use cases (see ADR-0009).
 *
 * <p>Implementations must be thread-safe.
 */
public interface RequestStatistics {

    /**
     * Counts one more hit for a request. Requests are the same when all their parameters are equal.
     *
     * @param query the parameters of the request
     */
    void record(FizzBuzzQuery query);

    /**
     * Returns the request made most often. On a tie, the request that reached that number of hits first wins.
     *
     * @return the most frequent request and its hits, or empty if no request was recorded
     */
    Optional<RequestHits> mostFrequent();
}
