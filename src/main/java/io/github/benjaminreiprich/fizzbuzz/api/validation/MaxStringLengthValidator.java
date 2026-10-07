package io.github.benjaminreiprich.fizzbuzz.api.validation;

import io.github.benjaminreiprich.fizzbuzz.config.FizzBuzzProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Instantiated by Spring's constraint validator factory, which injects the configured limits.
public class MaxStringLengthValidator implements ConstraintValidator<MaxStringLength, String> {

    private final int maxStringLength;

    public MaxStringLengthValidator(FizzBuzzProperties properties) {
        this.maxStringLength = properties.maxStringLength();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Counted in code points, so that an emoji counts as one character rather than two UTF-16 units.
        if (value == null || value.codePointCount(0, value.length()) <= maxStringLength) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("length must be less than or equal to " + maxStringLength)
                .addConstraintViolation();
        return false;
    }
}
