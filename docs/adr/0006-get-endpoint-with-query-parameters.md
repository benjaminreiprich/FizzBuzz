# 6. GET endpoint with query parameters

Date: 2026-10-07

## Status

Accepted. Its caching argument is corrected by [ADR-0013](0013-responses-are-not-cacheable.md): responses are not cacheable, so that every request is counted.

## Context

The endpoint takes five scalar parameters and computes a result without changing any state. We also need a stable public path and an unambiguous format for integers of any size (ADR-0005).

## Decision

- `GET /api/v1/fizzbuzz?int1=…&int2=…&limit=…&str1=…&str2=…` returns `200` with a JSON array of strings.
- GET because the operation is safe and idempotent: responses can be cached, URLs can be shared and retried freely. POST would hide a read behind a write verb; the URL length is bounded by the string length limit (ADR-0007).
- The version is a plain path prefix (`/api/v1`). Spring Framework 7's built-in API versioning is not used: with a single version it would add configuration without benefit.
- Integers are received as text and must be decimal (`[+-]?[0-9]+`), then converted to `BigInteger`. Spring's default conversion would silently accept hexadecimal (`0x10` as 16); rejecting it keeps one obvious notation. `5`, `+5` and `05` denote the same integer.

## Consequences

- All parameters are required; a missing or non-decimal integer gives a 400 listing every invalid parameter (ADR-0008).
- A future incompatible change gets a new `/api/v2` path while `/api/v1` keeps working.
