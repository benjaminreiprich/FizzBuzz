package io.github.benjaminreiprich.fizzbuzz.api.validation;

import io.github.benjaminreiprich.fizzbuzz.config.FizzBuzzProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Spring creates this validator and passes the configured limits to its constructor.
public class MaxStringLengthValidator implements ConstraintValidator<MaxStringLength, String> {

    private final int maxStringLength;

    public MaxStringLengthValidator(FizzBuzzProperties properties) {
        this.maxStringLength = properties.maxStringLength();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // A missing value is already reported by @NotNull.
        if (value == null) {
            return true;
        }

        // Counted in characters as a user sees them: value.length() would count an emoji as 2.
        int length = value.codePointCount(0, value.length());
        if (length <= maxStringLength) {
            return true;
        }

        // The annotation's default message cannot contain the configured value: we disable it and add our own.
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("length must be less than or equal to " + maxStringLength)
                .addConstraintViolation();
        return false;
    }
}
