package io.github.benjaminreiprich.fizzbuzz.api;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.benjaminreiprich.fizzbuzz.application.StatisticsService;
import io.github.benjaminreiprich.fizzbuzz.application.port.RequestHits;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

/** HTTP contract of the statistics endpoint; counting itself is tested on the store and end to end. */
@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    private static final String URL = "/api/v1/statistics";

    @MockitoBean
    private StatisticsService statisticsService;

    private final MockMvc mockMvc;

    StatisticsControllerTest(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    // ADR-0010: same shape as usual, with no request and zero hits, rather than an empty 204.
    @Test
    void should_report_no_request_and_zero_hits_before_any_request() throws Exception {
        given(statisticsService.mostFrequentRequest()).willReturn(Optional.empty());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"request": null, "hits": 0}""", JsonCompareMode.STRICT));
    }

    @Test
    void should_return_the_parameters_of_the_most_frequent_request_and_its_hits() throws Exception {
        given(statisticsService.mostFrequentRequest())
                .willReturn(Optional.of(new RequestHits(query(3, 5, 15, "fizz", "buzz"), 42)));

        mockMvc.perform(get(URL)).andExpect(status().isOk()).andExpect(content().json("""
                                {"request": {"int1": 3, "int2": 5, "limit": 15, "str1": "fizz", "str2": "buzz"},\
                                 "hits": 42}""", JsonCompareMode.STRICT));
    }

    // Statistics change with every FizzBuzz request: a cached copy would be stale (ADR-0013).
    @Test
    void should_forbid_caching_of_statistics() throws Exception {
        given(statisticsService.mostFrequentRequest()).willReturn(Optional.empty());

        mockMvc.perform(get(URL)).andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"));
    }

    // Compared as text: JSON comparison libraries may round numbers this large.
    @Test
    void should_return_integers_of_any_size_exactly() throws Exception {
        FizzBuzzQuery query =
                new FizzBuzzQuery(BigInteger.TEN.pow(30), BigInteger.TEN.pow(30).negate(), BigInteger.ONE, "", "buzz");
        given(statisticsService.mostFrequentRequest()).willReturn(Optional.of(new RequestHits(query, 1)));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"int1\":1000000000000000000000000000000")))
                .andExpect(content().string(containsString("\"int2\":-1000000000000000000000000000000")))
                .andExpect(content().string(containsString("\"str1\":\"\"")));
    }

    // The statement says the endpoint accepts no parameter: any parameter sent anyway is ignored (ADR-0010).
    @Test
    void should_ignore_query_parameters() throws Exception {
        given(statisticsService.mostFrequentRequest()).willReturn(Optional.empty());

        mockMvc.perform(get(URL).param("int1", "3").param("unknown", "x"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"request": null, "hits": 0}""", JsonCompareMode.STRICT));
    }

    @Test
    void should_answer_unsupported_method_with_problem_details() throws Exception {
        mockMvc.perform(post(URL))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    private static FizzBuzzQuery query(int int1, int int2, int limit, String str1, String str2) {
        return new FizzBuzzQuery(
                BigInteger.valueOf(int1), BigInteger.valueOf(int2), BigInteger.valueOf(limit), str1, str2);
    }
}
