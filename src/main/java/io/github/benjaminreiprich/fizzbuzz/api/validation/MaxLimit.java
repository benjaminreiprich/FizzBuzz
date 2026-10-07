package io.github.benjaminreiprich.fizzbuzz.api.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated decimal integer, given as text, must not exceed {@code fizzbuzz.max-limit}. A standard {@code @Max}
 * cannot be used because its bound is fixed at compile time. Values that are null or not decimal integers are valid
 * here and left to {@code @NotNull} and {@link DecimalInteger}.
 */
@Documented
@Constraint(validatedBy = MaxLimitValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface MaxLimit {

    String message() default "must not exceed the configured maximum limit";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
