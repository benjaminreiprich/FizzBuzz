package io.github.benjaminreiprich.fizzbuzz.application.port;

import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.util.Objects;

/**
 * A FizzBuzz request and the number of times it was made.
 *
 * @param query the parameters of the request
 * @param hits how many times the request was made; at least 1
 */
public record RequestHits(FizzBuzzQuery query, long hits) {

    public RequestHits {
        Objects.requireNonNull(query, "query must not be null");
        if (hits < 1) {
            throw new IllegalArgumentException("hits must be at least 1, but was " + hits);
        }
    }
}
