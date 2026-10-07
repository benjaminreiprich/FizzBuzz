package io.github.benjaminreiprich.fizzbuzz.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.benjaminreiprich.fizzbuzz.application.FizzBuzzService;
import io.github.benjaminreiprich.fizzbuzz.config.FizzBuzzConfiguration;
import io.github.benjaminreiprich.fizzbuzz.infrastructure.statistics.InMemoryRequestStatistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** The limits are read from configuration, not hard-coded. */
@WebMvcTest(
        controllers = FizzBuzzController.class,
        properties = {"fizzbuzz.max-limit=20", "fizzbuzz.max-string-length=3"})
@Import({FizzBuzzConfiguration.class, FizzBuzzService.class, InMemoryRequestStatistics.class})
class FizzBuzzControllerConfiguredLimitsTest {

    private final MockMvc mockMvc;

    FizzBuzzControllerConfiguredLimitsTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void should_apply_the_configured_maximum_limit() throws Exception {
        mockMvc.perform(fizzBuzz("20", "abc")).andExpect(status().isOk());

        mockMvc.perform(fizzBuzz("21", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value("must be less than or equal to 20"));
    }

    @Test
    void should_apply_the_configured_maximum_string_length() throws Exception {
        mockMvc.perform(fizzBuzz("15", "abcd"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("str1"))
                .andExpect(jsonPath("$.errors[0].message").value("length must be less than or equal to 3"));
    }

    private static MockHttpServletRequestBuilder fizzBuzz(String limit, String str1) {
        return get("/api/v1/fizzbuzz")
                .param("int1", "3")
                .param("int2", "5")
                .param("limit", limit)
                .param("str1", str1)
                .param("str2", "b");
    }
}
