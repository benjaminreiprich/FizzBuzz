package io.github.benjaminreiprich.fizzbuzz.api;

import io.github.benjaminreiprich.fizzbuzz.api.dto.StatisticsResponse;
import io.github.benjaminreiprich.fizzbuzz.application.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
class StatisticsController {

    private final StatisticsService statisticsService;

    StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    // Takes no parameter, as the statement requires; any query parameter sent anyway is ignored (ADR-0010).
    @GetMapping
    @Operation(
            summary = "Most frequent FizzBuzz request",
            description = "Returns the parameters of the most frequent valid FizzBuzz request and its number of hits."
                    + " Requests are the same when all five parameters are equal; on a tie, the request that"
                    + " reached that number of hits first wins.")
    @ApiResponse(
            responseCode = "200",
            description = "The most frequent request and its hits, or no request and 0 hits if none was made yet.",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = StatisticsResponse.class),
                            examples = {
                                @ExampleObject(name = "most frequent request", value = """
                                                {"request":{"int1":3,"int2":5,"limit":15,\
                                                "str1":"fizz","str2":"buzz"},"hits":42}"""),
                                @ExampleObject(name = "no request yet", value = """
                                        {"request":null,"hits":0}""")
                            }))
    StatisticsResponse mostFrequentRequest() {
        return statisticsService
                .mostFrequentRequest()
                .map(StatisticsResponse::of)
                .orElse(StatisticsResponse.NO_REQUEST_YET);
    }
}
