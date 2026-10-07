package io.github.benjaminreiprich.fizzbuzz.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class DecimalIntegerValidator implements ConstraintValidator<DecimalInteger, String> {

    // Spring's default String-to-number conversion would also accept hexadecimal such as 0x10 (ADR-0006).
    private static final Pattern DECIMAL_INTEGER = Pattern.compile("[+-]?[0-9]+");

    static boolean isDecimalInteger(String value) {
        return DECIMAL_INTEGER.matcher(value).matches();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || isDecimalInteger(value);
    }
}
