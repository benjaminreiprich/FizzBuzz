package io.github.benjaminreiprich.fizzbuzz.api.validation;

import io.github.benjaminreiprich.fizzbuzz.config.FizzBuzzProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigInteger;

// Spring creates this validator and passes the configured limits to its constructor.
public class MaxLimitValidator implements ConstraintValidator<MaxLimit, String> {

    private final BigInteger maxLimit;

    public MaxLimitValidator(FizzBuzzProperties properties) {
        this.maxLimit = BigInteger.valueOf(properties.maxLimit());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // A missing value or a non-integer is already reported by @NotNull or @DecimalInteger. Accepting it here
        // avoids a second, confusing error message for the same parameter.
        boolean canBeCompared = value != null && DecimalIntegerValidator.isDecimalInteger(value);
        if (!canBeCompared) {
            return true;
        }

        boolean withinMaxLimit = new BigInteger(value).compareTo(maxLimit) <= 0;
        if (withinMaxLimit) {
            return true;
        }

        // The annotation's default message cannot contain the configured value: we disable it and add our own.
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("must be less than or equal to " + maxLimit)
                .addConstraintViolation();
        return false;
    }
}
