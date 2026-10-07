# 7. Operational limits on limit and string length

Date: 2026-10-07

## Status

Accepted

## Context

The statement accepts any integer and any string, but also requires the server to be "ready for production". Without bounds, one request with `limit = 2147483647` builds about 2.1 billion strings (over 20 GB of JSON) and crashes the JVM for every user; long strings multiply the response size the same way.

## Decision

Two bounds, the only deliberate deviations from the statement, both configurable without code change:

- `fizzbuzz.max-limit` (default **10 000**): the worst case is `int1 = int2 = 1` with two 50-character strings, i.e. 10 000 terms of about 103 bytes, **≈ 1 MB** per response, generated in well under a millisecond. 100 concurrent worst-case requests fit in about 100 MB. It is also 100 times the classic 1-to-100 FizzBuzz.
- `fizzbuzz.max-string-length` (default **50** characters, counted in code points) keeps that worst case bounded.

They are checked at the API boundary by custom constraints that read the configuration (a standard `@Max` or `@Size` is fixed at compile time), never in the domain. Both must be at least 1, checked at startup.

## Consequences

- A request above a bound gets a 400 that states the configured maximum.
- Raising a bound is a configuration change (`FIZZBUZZ_MAX_LIMIT`, `FIZZBUZZ_MAX_STRING_LENGTH`); the cost grows linearly with each.
