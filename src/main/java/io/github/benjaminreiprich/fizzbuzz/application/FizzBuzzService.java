package io.github.benjaminreiprich.fizzbuzz.application;

import io.github.benjaminreiprich.fizzbuzz.application.port.RequestStatistics;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzGenerator;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.util.List;
import org.springframework.stereotype.Service;

/** Use case: computes the FizzBuzz sequence for a request and counts the request in the statistics. */
@Service
public class FizzBuzzService {

    private final FizzBuzzGenerator generator;
    private final RequestStatistics statistics;

    public FizzBuzzService(FizzBuzzGenerator generator, RequestStatistics statistics) {
        this.generator = generator;
        this.statistics = statistics;
    }

    /**
     * Returns the FizzBuzz sequence described by {@code query}, then counts the request.
     *
     * @param query the five parameters of the request
     * @return an unmodifiable list with one term per number from 1 to {@code query.limit()}
     */
    public List<String> fizzBuzz(FizzBuzzQuery query) {
        List<String> sequence = generator.generate(query);
        // Counted only once the sequence exists, so that a failed request never shows up in the statistics.
        statistics.record(query);
        return sequence;
    }
}
