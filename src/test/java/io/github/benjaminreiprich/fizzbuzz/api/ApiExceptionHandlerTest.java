package io.github.benjaminreiprich.fizzbuzz.api;

import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.benjaminreiprich.fizzbuzz.application.FizzBuzzService;
import io.github.benjaminreiprich.fizzbuzz.config.FizzBuzzConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FizzBuzzController.class)
@Import(FizzBuzzConfiguration.class)
class ApiExceptionHandlerTest {

    @MockitoBean
    private FizzBuzzService fizzBuzzService;

    private final MockMvc mockMvc;

    ApiExceptionHandlerTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void should_hide_unexpected_error_details_behind_a_generic_problem() throws Exception {
        given(fizzBuzzService.fizzBuzz(any())).willThrow(new IllegalStateException("internal secret"));

        mockMvc.perform(get("/api/v1/fizzbuzz")
                        .param("int1", "3")
                        .param("int2", "5")
                        .param("limit", "15")
                        .param("str1", "fizz")
                        .param("str2", "buzz"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
                .andExpect(content().string(not(Matchers.containsString("internal secret"))))
                .andExpect(content().string(not(Matchers.containsString("IllegalStateException"))));
    }
}
