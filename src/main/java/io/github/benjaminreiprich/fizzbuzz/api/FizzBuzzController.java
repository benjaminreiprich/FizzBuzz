package io.github.benjaminreiprich.fizzbuzz.api;

import io.github.benjaminreiprich.fizzbuzz.api.dto.FizzBuzzRequest;
import io.github.benjaminreiprich.fizzbuzz.application.FizzBuzzService;
import jakarta.validation.Valid;
import java.util.List;
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
    List<String> fizzBuzz(@Valid FizzBuzzRequest request) {
        return fizzBuzzService.fizzBuzz(request.toQuery());
    }
}
