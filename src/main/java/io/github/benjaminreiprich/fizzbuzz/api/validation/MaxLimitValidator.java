package io.github.benjaminreiprich.fizzbuzz.api.validation;

import io.github.benjaminreiprich.fizzbuzz.config.FizzBuzzProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigInteger;

// Instantiated by Spring's constraint validator factory, which injects the configured limits.
public class MaxLimitValidator implements ConstraintValidator<MaxLimit, String> {

    private final BigInteger maxLimit;

    public MaxLimitValidator(FizzBuzzProperties properties) {
        this.maxLimit = BigInteger.valueOf(properties.maxLimit());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || !DecimalIntegerValidator.isDecimalInteger(value)) {
            return true;
        }
        if (new BigInteger(value).compareTo(maxLimit) <= 0) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("must be less than or equal to " + maxLimit)
                .addConstraintViolation();
        return false;
    }
}
