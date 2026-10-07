# 9. In-memory statistics store behind a port

Date: 2026-10-07

## Status

Accepted

## Context

The statistics bonus needs the most frequent request and its hit count. Every successful FizzBuzz call records a hit, so recording sits on the hot path and runs concurrently; reading must stay cheap. The exact answer requires keeping a count for every distinct request.

## Decision

- `RequestStatistics` (application port) is the project's only interface: `record(query)` and `mostFrequent()`. `FizzBuzzService` records a request only after its sequence was generated.
- `InMemoryRequestStatistics` is the default implementation, thread-safe without a global lock: a `ConcurrentHashMap<FizzBuzzQuery, AtomicLong>` holds the counts, and an `AtomicReference` holds the leader, replaced with a compare-and-set loop only when a count strictly exceeds it. `mostFrequent()` is a single read, O(1).
- `AtomicLong`, not `LongAdder`: each increment's exact result is compared to the leader, and `LongAdder` does not return it.
- "Strictly exceeds" makes the first request to reach a count win a tie, and the leader's count never goes backwards.

## Consequences

- Statistics are lost on restart and not shared between instances; the port lets a shared store (e.g. Redis) replace the in-memory one without changing the use cases.
- The number of distinct requests is unbounded (about 200 bytes each), so a client sending millions of different requests grows memory. Bounding it (e.g. Space-Saving) would make the answer approximate, a deviation from the statement; we keep it exact and document the limit.
