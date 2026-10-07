package io.github.benjaminreiprich.fizzbuzz;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FizzBuzzApplicationTests {

    // No assertion needed: the test fails if the Spring context cannot start
    // (broken wiring, invalid configuration, auto-configuration errors).
    @Test
    void should_load_application_context() {}
}
