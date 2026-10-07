package io.github.benjaminreiprich.fizzbuzz.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class DecimalIntegerValidator implements ConstraintValidator<DecimalInteger, String> {

    // An optional "+" or "-" sign, then one or more digits: "42", "-3", "+5" and "007" match; "0x10", "1.5",
    // "1e3" and " 5" do not. Spring's default conversion would accept hexadecimal such as 0x10 (ADR-0006).
    private static final Pattern DECIMAL_INTEGER = Pattern.compile("[+-]?[0-9]+");

    static boolean isDecimalInteger(String value) {
        return DECIMAL_INTEGER.matcher(value).matches();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // A missing value is reported by @NotNull, not here.
        if (value == null) {
            return true;
        }
        return isDecimalInteger(value);
    }
}
