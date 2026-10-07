package io.github.benjaminreiprich.fizzbuzz.infrastructure.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.benjaminreiprich.fizzbuzz.application.port.RequestHits;
import io.github.benjaminreiprich.fizzbuzz.domain.FizzBuzzQuery;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.RepeatedTest;

/**
 * Many threads record at the same time; no hit may be lost and the leader must be the request with the most hits.
 * Repeated because a race that is missed once may show up on another run.
 */
class InMemoryRequestStatisticsConcurrencyTest {

    private static final int THREADS = 8;
    private static final int ROUNDS_PER_THREAD = 5_000;

    // Hits per round: the leader is unique (3 > 2 > 1), so the expected result is deterministic.
    private static final FizzBuzzQuery MOST_FREQUENT = query(3);
    private static final FizzBuzzQuery SECOND = query(4);
    private static final FizzBuzzQuery THIRD = query(5);

    @RepeatedTest(5)
    void should_count_every_hit_exactly_when_threads_record_concurrently() throws Exception {
        InMemoryRequestStatistics statistics = new InMemoryRequestStatistics();
        // A "starting gun": every thread waits on it, so that they all begin recording at the same moment.
        CountDownLatch start = new CountDownLatch(1);

        // A pool of 8 threads; try-with-resources shuts it down at the end of the block.
        try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
            // 1. Start every thread: each one immediately waits for the starting gun.
            List<Future<?>> workers = new ArrayList<>();
            for (int thread = 0; thread < THREADS; thread++) {
                workers.add(executor.submit(recordRounds(statistics, start)));
            }
            // 2. Fire the starting gun: all threads now record concurrently.
            start.countDown();
            // 3. Wait for every thread to finish; get() also rethrows any exception a thread may have hit.
            for (Future<?> worker : workers) {
                worker.get();
            }
        }

        // 3 hits per round for MOST_FREQUENT: if a single hit had been lost, this count would be lower.

        assertThat(statistics.mostFrequent())
                .contains(new RequestHits(MOST_FREQUENT, 3L * THREADS * ROUNDS_PER_THREAD));
    }

    // The work of one thread: wait for the starting gun, then record the three requests in turn, so that threads
    // keep competing for both the counts and the leader.
    private static Callable<Void> recordRounds(InMemoryRequestStatistics statistics, CountDownLatch start) {
        return () -> {
            start.await();
            for (int round = 0; round < ROUNDS_PER_THREAD; round++) {
                statistics.record(THIRD);
                statistics.record(SECOND);
                statistics.record(MOST_FREQUENT);
                statistics.record(SECOND);
                statistics.record(MOST_FREQUENT);
                statistics.record(MOST_FREQUENT);
            }
            return null;
        };
    }

    private static FizzBuzzQuery query(int int1) {
        return new FizzBuzzQuery(BigInteger.valueOf(int1), BigInteger.TWO, BigInteger.TEN, "fizz", "buzz");
    }
}
