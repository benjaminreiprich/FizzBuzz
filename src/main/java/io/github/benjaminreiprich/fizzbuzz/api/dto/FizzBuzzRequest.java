package io.github.benjaminreiprich.fizzbuzz.api.dto;

import io.github.benjaminreiprich.fizzbuzz.api.validation.DecimalInteger;
import io.github.benjaminreiprich.fizzbuzz.api.validation.MaxLimit;
import io.github.benjaminreiprich.fizzbuzz.api.validation.MaxStringLength;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;

/**
 * Query parameters of {@code GET /api/v1/fizzbuzz}. Integers are received as text so that only decimal notation is
 * accepted and any size can be validated (ADR-0006).
 */
public record FizzBuzzRequest(
        @NotNull(message = REQUIRED) @DecimalInteger String int1,
        @NotNull(message = REQUIRED) @DecimalInteger String int2,

        @NotNull(message = REQUIRED) @DecimalInteger @MaxLimit
        String limit,

        @NotNull(message = REQUIRED) @MaxStringLength String str1,
        @NotNull(message = REQUIRED) @MaxStringLength String str2) {

    private static final String REQUIRED = "is required";

    /** Must only be called once the request has been validated. */
    public FizzBuzzQuery toQuery() {
        return new FizzBuzzQuery(new BigInteger(int1), new BigInteger(int2), new BigInteger(limit), str1, str2);
    }
}
