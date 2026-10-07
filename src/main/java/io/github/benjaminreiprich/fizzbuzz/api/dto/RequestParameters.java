package io.github.benjaminreiprich.fizzbuzz.api.dto;

import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigInteger;

/**
 * The five parameters of a FizzBuzz request, as returned by the statistics. Integers are JSON numbers of any size,
 * written in canonical form: a request sent with {@code int1=05} is reported as {@code 5}.
 */
public record RequestParameters(
        @Schema(example = "3") BigInteger int1,
        @Schema(example = "5") BigInteger int2,
        @Schema(example = "15") BigInteger limit,
        @Schema(example = "fizz") String str1,
        @Schema(example = "buzz") String str2) {

    public static RequestParameters from(FizzBuzzQuery query) {
        return new RequestParameters(query.int1(), query.int2(), query.limit(), query.str1(), query.str2());
    }
}
