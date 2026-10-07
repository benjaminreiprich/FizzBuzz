package io.github.benjaminreiprich.fizzbuzz.api.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated text must not be longer than {@code fizzbuzz.max-string-length} characters. A standard {@code @Size}
 * cannot be used because its bound is fixed at compile time. Null is valid; combine with {@code @NotNull}.
 */
@Documented
@Constraint(validatedBy = MaxStringLengthValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface MaxStringLength {

    String message() default "must not exceed the configured maximum length";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
