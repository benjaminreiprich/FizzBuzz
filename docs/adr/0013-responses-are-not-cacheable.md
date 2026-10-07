# 13. Responses are not cacheable

Date: 2026-10-07

## Status

Accepted. Corrects the caching argument of [ADR-0006](0006-get-endpoint-with-query-parameters.md).

## Context

ADR-0006 listed cacheability among the reasons for GET. But the statistics count every valid FizzBuzz request: a response served by a browser, proxy or CDN cache never reaches the server, so that request would be missing from the counts. The statistics themselves change with every FizzBuzz request, so a cached copy would be stale.

## Decision

Both endpoints answer with `Cache-Control: no-store`, set explicitly in each controller next to the reason. GET remains the right verb: the operation is safe and idempotent, and its URL can be shared and retried.

## Consequences

- Every request reaches the server and is counted; the statistics are always current.
- No HTTP caching: each repeated request is computed again, which the bounds of ADR-0007 keep cheap (under a millisecond, at most about 1 MB).
