package io.github.benjaminreiprich.fizzbuzz.api.dto;

import io.github.benjaminreiprich.fizzbuzz.api.validation.DecimalInteger;
import io.github.benjaminreiprich.fizzbuzz.api.validation.MaxLimit;
import io.github.benjaminreiprich.fizzbuzz.api.validation.MaxStringLength;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;

/**
 * Query parameters of {@code GET /api/v1/fizzbuzz}. Integers are received as text so that only decimal notation is
 * accepted and any size can be validated (ADR-0006).
 */
public record FizzBuzzRequest(
        @Parameter(
                description = "Integer whose multiples are replaced by str1. Any size, decimal notation.",
                example = "3",
                schema = @Schema(type = "integer"))
        @NotNull(message = REQUIRED)
        @DecimalInteger
        String int1,

        @Parameter(
                description = "Integer whose multiples are replaced by str2. Any size, decimal notation.",
                example = "5",
                schema = @Schema(type = "integer"))
        @NotNull(message = REQUIRED)
        @DecimalInteger
        String int2,

        @Parameter(
                description = "Last number of the sequence, at most fizzbuzz.max-limit (default 10000)."
                        + " A limit lower than 1 returns an empty list.",
                example = "15",
                schema = @Schema(type = "integer"))
        @NotNull(message = REQUIRED)
        @DecimalInteger
        @MaxLimit
        String limit,

        @Parameter(
                description = "Replacement for multiples of int1. Any string, empty included,"
                        + " at most fizzbuzz.max-string-length characters (default 50).",
                example = "fizz")
        @NotNull(message = REQUIRED)
        @MaxStringLength
        String str1,

        @Parameter(
                description = "Replacement for multiples of int2. Any string, empty included,"
                        + " at most fizzbuzz.max-string-length characters (default 50).",
                example = "buzz")
        @NotNull(message = REQUIRED)
        @MaxStringLength
        String str2) {

    private static final String REQUIRED = "is required";

    /** Must only be called once the request has been validated. */
    public FizzBuzzQuery toQuery() {
        return new FizzBuzzQuery(new BigInteger(int1), new BigInteger(int2), new BigInteger(limit), str1, str2);
    }
}
