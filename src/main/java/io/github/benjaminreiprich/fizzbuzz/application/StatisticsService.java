package io.github.benjaminreiprich.fizzbuzz.application;

import io.github.benjaminreiprich.fizzbuzz.application.port.RequestHits;
import io.github.benjaminreiprich.fizzbuzz.application.port.RequestStatistics;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Use case: tells which FizzBuzz request was made most often. */
@Service
public class StatisticsService {

    private final RequestStatistics statistics;

    public StatisticsService(RequestStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Returns the FizzBuzz request made most often. On a tie, the request that reached that number of hits first wins.
     *
     * @return the most frequent request and its hits, or empty if no FizzBuzz request was made yet
     */
    public Optional<RequestHits> mostFrequentRequest() {
        return statistics.mostFrequent();
    }
}
