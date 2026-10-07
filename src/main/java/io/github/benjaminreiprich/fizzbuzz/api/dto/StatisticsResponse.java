package io.github.benjaminreiprich.fizzbuzz.api.dto;

import io.github.benjaminreiprich.fizzbuzz.application.port.RequestHits;
import io.swagger.v3.oas.annotations.media.Schema;

/** Body of {@code GET /api/v1/statistics}. */
public record StatisticsResponse(
        @Schema(
                description = "Parameters of the most frequent FizzBuzz request; null if no request was made yet.",
                nullable = true)
        RequestParameters request,

        @Schema(description = "Number of times that request was made; 0 if no request was made yet.", example = "42")
        long hits) {

    // Same shape as usual rather than an empty 204, so that clients always read "hits" (ADR-0010).
    public static final StatisticsResponse NO_REQUEST_YET = new StatisticsResponse(null, 0);

    public static StatisticsResponse of(RequestHits mostFrequent) {
        return new StatisticsResponse(RequestParameters.from(mostFrequent.query()), mostFrequent.hits());
    }
}
