package io.github.benjaminreiprich.fizzbuzz.application;

import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzGenerator;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.util.List;
import org.springframework.stereotype.Service;

/** Use case: computes the FizzBuzz sequence for a request. */
@Service
public class FizzBuzzService {

    private final FizzBuzzGenerator generator;

    public FizzBuzzService(FizzBuzzGenerator generator) {
        this.generator = generator;
    }

    /**
     * Returns the FizzBuzz sequence described by {@code query}.
     *
     * @param query the five parameters of the request
     * @return an unmodifiable list with one term per number from 1 to {@code query.limit()}
     */
    public List<String> fizzBuzz(FizzBuzzQuery query) {
        return generator.generate(query);
    }
}
