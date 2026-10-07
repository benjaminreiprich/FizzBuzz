# 8. Problem Details error responses

Date: 2026-10-07

## Status

Accepted

## Context

Clients need a predictable error format, with enough detail to fix a bad request, and the server must never leak internals such as stack traces or exception messages.

## Decision

Every error is an RFC 9457 Problem Details body (`application/problem+json`). `ApiExceptionHandler` extends Spring's `ResponseEntityExceptionHandler`, which already covers framework errors (404, 405, 415...), and adds:

- for validation errors, `"detail": "Invalid request parameters."` and an `errors` array of `{field, message}`, sorted by field, listing every invalid parameter at once;
- for any unexpected exception, a generic 500 (`"An unexpected error occurred."`); the exception is logged server-side only.

The rejected value is never echoed back: it may be large, and repeating user input in responses and logs is a needless risk. `type` is omitted, which RFC 9457 defines as `about:blank`.

## Consequences

- One error format for the whole API, documented once in the README and OpenAPI.
- Clients see which parameter failed and why, without seeing their input repeated.
