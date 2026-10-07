# 10. Statistics endpoint semantics

Date: 2026-10-07

## Status

Accepted

## Context

The statement asks for an endpoint that "accepts no parameter" and returns "the parameters corresponding to the most used request, as well as the number of hits for this request". It does not say what a request is, what to return before any request, or how to break a tie.

## Decision

`GET /api/v1/statistics` returns `200 {"request": {int1, int2, limit, str1, str2}, "hits": n}`, with these rules:

1. **Before any request**: `200 {"request": null, "hits": 0}`, not `204`: clients always get the same shape and the hit count the statement asks for.
2. **What counts**: only valid requests (`200`). A rejected request (`400`) has no valid parameters to report.
3. **Identity**: the five parameters, integers compared as numbers and strings exactly. `05` and `5` are the same request, reported as `5`; `(3, 5, fizz, buzz)` and `(5, 3, buzz, fizz)` are different, as their outputs differ.
4. **Ties**: the request that reached the highest count first wins, so the answer is one request, as the statement says, and deterministic.
5. **Parameters sent anyway** are ignored, as usual in HTTP.

## Consequences

- The response always has the same shape and is easy to consume.
- Integers are JSON numbers of any size; clients parsing them as 64-bit floats (JavaScript) lose precision beyond 2⁵³, only for such extreme requests.
