package io.github.benjaminreiprich.fizzbuzz.config;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Operational limits of the API: deliberate deviations from the statement, justified by "ready for production"
 * (see ADR-0007). Validated at startup so that a misconfiguration fails fast instead of rejecting every request.
 *
 * @param maxLimit highest accepted {@code limit}; 10 000 keeps the worst-case response around 1 MB
 * @param maxStringLength highest accepted length of {@code str1} and {@code str2}, in characters
 */
@Validated
@ConfigurationProperties("fizzbuzz")
public record FizzBuzzProperties(
        @DefaultValue("10000") @Positive int maxLimit,
        @DefaultValue("50") @Positive int maxStringLength) {}
