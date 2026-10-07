package io.github.benjaminreiprich.fizzbuzz.api;

import io.github.benjaminreiprich.fizzbuzz.api.dto.FizzBuzzRequest;
import io.github.benjaminreiprich.fizzbuzz.application.FizzBuzzService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fizzbuzz")
class FizzBuzzController {

    private final FizzBuzzService fizzBuzzService;

    FizzBuzzController(FizzBuzzService fizzBuzzService) {
        this.fizzBuzzService = fizzBuzzService;
    }

    // GET because the operation is safe and idempotent (ADR-0006); query parameters bind to the request record.
    @GetMapping
    @Operation(
            summary = "Generate a FizzBuzz sequence",
            description = "Returns the numbers from 1 to limit, where multiples of int1 are replaced by str1,"
                    + " multiples of int2 by str2, and multiples of both by str1str2.")
    @ApiResponse(
            responseCode = "200",
            description = "One string per number from 1 to limit; empty when limit is lower than 1.",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            examples =
                                    @ExampleObject(
                                            value = "[\"1\",\"2\",\"fizz\",\"4\",\"buzz\",\"fizz\",\"7\",\"8\","
                                                    + "\"fizz\",\"buzz\",\"11\",\"fizz\",\"13\",\"14\",\"fizzbuzz\"]")))
    @ApiResponse(
            responseCode = "400",
            description = "Missing parameter, integer not in decimal notation, or limit exceeded."
                    + " Every invalid parameter is listed in errors.",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(value = """
                                                    {"title":"Bad Request","status":400,\
                                                    "detail":"Invalid request parameters.",\
                                                    "instance":"/api/v1/fizzbuzz",\
                                                    "errors":[{"field":"limit",\
                                                    "message":"must be less than or equal to 10000"}]}""")))
    ResponseEntity<List<String>> fizzBuzz(@ParameterObject @Valid FizzBuzzRequest request) {
        List<String> sequence = fizzBuzzService.fizzBuzz(request.toQuery());
        // A cached response would never reach the server, so the request would be missing from the statistics.
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(sequence);
    }
}
