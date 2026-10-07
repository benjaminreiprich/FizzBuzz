# 4. Domain invariants vs configurable limits

Date: 2026-10-07

## Status

Superseded by [ADR-0005](0005-accept-every-input-allowed-by-the-statement.md).

## Context

A FizzBuzz request can be wrong in two different ways. Some values make the computation meaningless: a divisor of 0 (division by zero), a negative divisor or limit, an empty replacement string. Other values are computable but unreasonable for a public service: `limit = 10⁹` would allocate gigabytes, a 1 MB replacement string would bloat the response. The first kind is a property of FizzBuzz itself; the second is an operational choice that may differ between environments.

## Decision

`FizzBuzzQuery` enforces only the first kind, in its compact constructor: `int1`, `int2` and `limit` at least 1, `str1` and `str2` non-null and non-empty. Any instance is therefore valid, and the generator never checks its input.

Upper bounds (`fizzbuzz.max-limit`, `fizzbuzz.max-string-length`) live in configuration and are enforced at the API boundary (Phase 2), never in the domain.

## Consequences

- The domain stays free of configuration and framework code, and its rules can be tested with plain `new`.
- Invalid input is rejected twice: by Bean Validation at the API (to return a clear 400) and by the record (as a last line of defence). The API must validate first so that users never see a domain exception message.
- Whitespace-only strings such as `" "` are accepted: the specification only requires non-empty strings.
