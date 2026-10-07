package io.github.benjaminreiprich.fizzbuzz.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class FizzBuzzPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(FizzBuzzConfiguration.class);

    @Test
    void should_default_to_documented_limits() {
        contextRunner.run(context -> {
            FizzBuzzProperties properties = context.getBean(FizzBuzzProperties.class);

            assertThat(properties.maxLimit()).isEqualTo(10_000);
            assertThat(properties.maxStringLength()).isEqualTo(50);
        });
    }

    @Test
    void should_bind_configured_limits() {
        contextRunner
                .withPropertyValues("fizzbuzz.max-limit=100", "fizzbuzz.max-string-length=10")
                .run(context -> {
                    FizzBuzzProperties properties = context.getBean(FizzBuzzProperties.class);

                    assertThat(properties.maxLimit()).isEqualTo(100);
                    assertThat(properties.maxStringLength()).isEqualTo(10);
                });
    }

    // Asserts on the property name rather than the message, which the validator translates to the JVM locale.
    @ParameterizedTest
    @CsvSource({"fizzbuzz.max-limit, 0", "fizzbuzz.max-limit, -1", "fizzbuzz.max-string-length, 0"})
    void should_refuse_to_start_with_a_limit_lower_than_one(String property, String value) {
        contextRunner
                .withPropertyValues(property + "=" + value)
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .rootCause()
                        .isInstanceOf(BindValidationException.class)
                        .hasMessageContaining(property));
    }
}
