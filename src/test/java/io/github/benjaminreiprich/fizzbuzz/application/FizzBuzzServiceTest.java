package io.github.benjaminreiprich.fizzbuzz.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import io.github.benjaminreiprich.fizzbuzz.application.port.RequestHits;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzGenerator;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import io.github.benjaminreiprich.fizzbuzz.infrastructure.statistics.InMemoryRequestStatistics;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class FizzBuzzServiceTest {

    private final InMemoryRequestStatistics statistics = new InMemoryRequestStatistics();
    private final FizzBuzzService service = new FizzBuzzService(new FizzBuzzGenerator(), statistics);

    @Test
    void should_return_the_sequence_and_record_the_request() {
        FizzBuzzQuery query = query(BigInteger.valueOf(5));

        assertThat(service.fizzBuzz(query)).containsExactly("1", "2", "fizz", "4", "buzz");
        assertThat(statistics.mostFrequent()).contains(new RequestHits(query, 1));
    }

    @Test
    void should_not_record_a_request_whose_sequence_could_not_be_generated() {
        FizzBuzzQuery tooLarge = query(BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE));

        assertThatIllegalArgumentException().isThrownBy(() -> service.fizzBuzz(tooLarge));
        assertThat(statistics.mostFrequent()).isEmpty();
    }

    private static FizzBuzzQuery query(BigInteger limit) {
        return new FizzBuzzQuery(BigInteger.valueOf(3), BigInteger.valueOf(5), limit, "fizz", "buzz");
    }
}
