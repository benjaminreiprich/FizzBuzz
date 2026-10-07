package io.github.benjaminreiprich.fizzbuzz.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.benjaminreiprich.fizzbuzz.application.FizzBuzzService;
import io.github.benjaminreiprich.fizzbuzz.config.FizzBuzzConfiguration;
import io.github.benjaminreiprich.fizzbuzz.infrastructure.statistics.InMemoryRequestStatistics;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(FizzBuzzController.class)
@Import({FizzBuzzConfiguration.class, FizzBuzzService.class, InMemoryRequestStatistics.class})
class FizzBuzzControllerTest {

    private static final String URL = "/api/v1/fizzbuzz";
    private static final String FIFTY_CHARACTERS = "x".repeat(50);

    private final MockMvc mockMvc;

    FizzBuzzControllerTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void should_return_fizzbuzz_sequence_as_json_array() throws Exception {
        perform(validParameters())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                                ["1","2","fizz","4","buzz","fizz","7","8","fizz","buzz","11","fizz","13","14","fizzbuzz"]
                                """, JsonCompareMode.STRICT));
    }

    // A cached response would never reach the server, so the request would be missing from the statistics (ADR-0013).
    @Test
    void should_forbid_caching_so_that_every_request_is_counted() throws Exception {
        perform(validParameters()).andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"));
    }

    static Stream<Arguments> inputsAllowedByTheStatement() {
        return Stream.of(
                arguments("zero and negative divisors", "0", "-5", "5", "a", "b", """
                        ["1","2","3","4","b"]"""),
                arguments("divisor of any size", "1000000000000000000000000000000", "2", "3", "a", "b", """
                        ["1","b","3"]"""),
                arguments("limit = 0", "3", "5", "0", "a", "b", "[]"),
                arguments("negative limit of any size", "3", "5", "-1000000000000000000000", "a", "b", "[]"),
                arguments("empty strings", "3", "5", "5", "", "", """
                        ["1","2","","4",""]"""),
                arguments("explicit sign and leading zeros", "+3", "005", "5", "a", "b", """
                        ["1","2","a","4","b"]"""));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("inputsAllowedByTheStatement")
    void should_accept_every_input_allowed_by_the_statement(
            String scenario, String int1, String int2, String limit, String str1, String str2, String expectedJson)
            throws Exception {
        perform(parameters(int1, int2, limit, str1, str2))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, JsonCompareMode.STRICT));
    }

    @Test
    void should_accept_limit_equal_to_the_configured_maximum() throws Exception {
        perform(parameters("3", "5", "10000", "fizz", "buzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10000));
    }

    @Test
    void should_accept_strings_whose_length_equals_the_configured_maximum() throws Exception {
        perform(parameters("1", "1", "1", FIFTY_CHARACTERS, FIFTY_CHARACTERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(FIFTY_CHARACTERS + FIFTY_CHARACTERS));
    }

    @ParameterizedTest
    @ValueSource(strings = {"int1", "int2", "limit", "str1", "str2"})
    void should_reject_missing_parameter(String parameter) throws Exception {
        Map<String, String> parameters = validParameters();
        parameters.remove(parameter);

        expectSingleError(perform(parameters), parameter, "is required");
    }

    // In @CsvSource, single quotes keep a value exactly as written: ' 5' is "5" preceded by a space, '' is an empty
    // string.
    @ParameterizedTest(name = "{0}={1}")
    @CsvSource({
        "int1, abc",
        "int1, 1.5",
        "int1, 0x10",
        "int1, 1e3",
        "int1, ' 5'",
        "int1, ''",
        "int2, abc",
        "limit, abc",
        "limit, 1.5"
    })
    void should_reject_integer_parameter_that_is_not_a_decimal_integer(String parameter, String value)
            throws Exception {
        Map<String, String> parameters = validParameters();
        parameters.put(parameter, value);

        expectSingleError(perform(parameters), parameter, "must be a decimal integer");
    }

    @Test
    void should_reject_repeated_integer_parameter() throws Exception {
        MockHttpServletRequestBuilder request = request(validParameters()).param("int1", "4");

        expectSingleError(mockMvc.perform(request), "int1", "must be a decimal integer");
    }

    @ParameterizedTest
    @ValueSource(strings = {"10001", "1000000000000000000000000000000"})
    void should_reject_limit_above_the_configured_maximum(String limit) throws Exception {
        expectSingleError(
                perform(parameters("3", "5", limit, "fizz", "buzz")), "limit", "must be less than or equal to 10000");
    }

    @ParameterizedTest
    @ValueSource(strings = {"str1", "str2"})
    void should_reject_string_longer_than_the_configured_maximum(String parameter) throws Exception {
        Map<String, String> parameters = validParameters();
        parameters.put(parameter, FIFTY_CHARACTERS + "x");

        expectSingleError(perform(parameters), parameter, "length must be less than or equal to 50");
    }

    @Test
    void should_report_every_invalid_parameter_as_problem_details() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                // RFC 9457 3.1.1: an absent "type" means "about:blank", which Spring omits.
                .andExpect(jsonPath("$.type").doesNotExist())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request parameters."))
                .andExpect(jsonPath("$.instance").value(URL))
                .andExpect(jsonPath("$.errors[*].field").value(contains("int1", "int2", "limit", "str1", "str2")))
                .andExpect(jsonPath("$.errors[*].message")
                        .value(contains("is required", "is required", "is required", "is required", "is required")));
    }

    @Test
    void should_answer_unsupported_method_with_problem_details() throws Exception {
        mockMvc.perform(post(URL))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(405));
    }

    private static Map<String, String> validParameters() {
        return parameters("3", "5", "15", "fizz", "buzz");
    }

    private static Map<String, String> parameters(String int1, String int2, String limit, String str1, String str2) {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("int1", int1);
        parameters.put("int2", int2);
        parameters.put("limit", limit);
        parameters.put("str1", str1);
        parameters.put("str2", str2);
        return parameters;
    }

    private static MockHttpServletRequestBuilder request(Map<String, String> parameters) {
        MockHttpServletRequestBuilder request = get(URL);
        parameters.forEach(request::param);
        return request;
    }

    private ResultActions perform(Map<String, String> parameters) throws Exception {
        return mockMvc.perform(request(parameters));
    }

    private static void expectSingleError(ResultActions result, String field, String message) throws Exception {
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value(field))
                .andExpect(jsonPath("$.errors[0].message").value(message));
    }
}
