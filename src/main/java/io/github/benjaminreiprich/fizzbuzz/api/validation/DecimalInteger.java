package io.github.benjaminreiprich.fizzbuzz.api.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated text must be an integer in decimal notation, of any size: an optional sign followed by ASCII digits.
 * Null is valid; combine with {@code @NotNull} to make the value required.
 */
@Documented
@Constraint(validatedBy = DecimalIntegerValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface DecimalInteger {

    String message() default "must be a decimal integer";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
