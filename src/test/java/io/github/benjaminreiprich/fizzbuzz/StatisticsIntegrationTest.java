package io.github.benjaminreiprich.fizzbuzz;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

/** Both endpoints end to end, on the real application context. */
@SpringBootTest
@AutoConfigureMockMvc
// The statistics live in a singleton: a fresh context per test keeps each one independent of the others.
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class StatisticsIntegrationTest {

    // Field injection on purpose: the context is replaced before each test, after the test instance is built, and
    // Spring
    // re-injects fields into the instance but cannot redo constructor injection.
    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_report_no_request_before_any_fizzbuzz_call() throws Exception {
        mockMvc.perform(get("/api/v1/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"request": null, "hits": 0}""", JsonCompareMode.STRICT));
    }

    @Test
    void should_report_the_most_frequent_valid_request_and_its_hits() throws Exception {
        // Three hits for the same integers, however they are written.
        fizzBuzz("3", "5", "15", "fizz", "buzz", status().isOk());
        fizzBuzz("+3", "05", "15", "fizz", "buzz", status().isOk());
        fizzBuzz("003", "+5", "15", "fizz", "buzz", status().isOk());
        // Swapped parameters give a different output, hence a different request.
        fizzBuzz("5", "3", "15", "buzz", "fizz", status().isOk());
        fizzBuzz("5", "3", "15", "buzz", "fizz", status().isOk());
        // Rejected requests are not counted, even when they are the most frequent calls.
        for (int i = 0; i < 5; i++) {
            fizzBuzz("5", "3", "abc", "buzz", "fizz", status().isBadRequest());
        }

        mockMvc.perform(get("/api/v1/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                                {"request": {"int1": 3, "int2": 5, "limit": 15, "str1": "fizz", "str2": "buzz"},\
                                 "hits": 3}""", JsonCompareMode.STRICT));
    }

    private void fizzBuzz(String int1, String int2, String limit, String str1, String str2, ResultMatcher expected)
            throws Exception {
        mockMvc.perform(get("/api/v1/fizzbuzz")
                        .param("int1", int1)
                        .param("int2", int2)
                        .param("limit", limit)
                        .param("str1", str1)
                        .param("str2", str2))
                .andExpect(expected);
    }
}
