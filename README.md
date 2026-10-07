# FizzBuzz REST API

[![CI](https://github.com/benjaminreiprich/FizzBuzz/actions/workflows/ci.yml/badge.svg)](https://github.com/benjaminreiprich/FizzBuzz/actions/workflows/ci.yml)

## Overview

A Spring Boot web service exposing a configurable FizzBuzz: given three integers `int1`, `int2`, `limit` and two strings `str1`, `str2`, it returns the numbers from 1 to `limit`, where multiples of `int1` are replaced by `str1`, multiples of `int2` by `str2`, and multiples of both by `str1str2`.

**Current state:** the FizzBuzz endpoint is available. The statistics endpoint is not implemented yet.

## Quick start

Prerequisite: **JDK 25**. Maven is not needed; the Maven Wrapper downloads the right version on first use. On Windows, use `mvnw.cmd` instead of `./mvnw`.

```bash
# Build, run all tests and quality gates
./mvnw verify

# Run locally on http://localhost:8080
./mvnw spring-boot:run

# Try it
curl "http://localhost:8080/api/v1/fizzbuzz?int1=3&int2=5&limit=15&str1=fizz&str2=buzz"

# Interactive documentation: http://localhost:8080/swagger-ui.html

# Fix formatting violations reported by verify
./mvnw spotless:apply
```

Run with Docker: Not implemented yet.

## API

Interactive documentation is served by Swagger UI at `/swagger-ui.html`, and the OpenAPI description at `/v3/api-docs`.

### `GET /api/v1/fizzbuzz`

| Parameter | Type | Accepted values |
|---|---|---|
| `int1` | integer | any integer, of any size, in decimal notation (`-3`, `0`, `+5`, `007`, `10000000000000000000000`) |
| `int2` | integer | same as `int1` |
| `limit` | integer | any decimal integer **up to 10 000** ([Limits](#limits)); `limit < 1` returns `[]` |
| `str1` | string | any string, empty included, **up to 50 characters** ([Limits](#limits)) |
| `str2` | string | same as `str1` |

All five parameters are required. Multiples follow arithmetic: a negative divisor has the same multiples as its absolute value, and `0` has no multiple between 1 and `limit`, so it replaces nothing.

```bash
curl "http://localhost:8080/api/v1/fizzbuzz?int1=3&int2=5&limit=15&str1=fizz&str2=buzz"
```

```json
["1","2","fizz","4","buzz","fizz","7","8","fizz","buzz","11","fizz","13","14","fizzbuzz"]
```

### Errors

Errors follow [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`). A 400 lists every invalid parameter; the rejected value is never echoed back.

```bash
curl "http://localhost:8080/api/v1/fizzbuzz?int1=0x10&int2=5&limit=100000&str1=fizz"
```

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid request parameters.",
  "instance": "/api/v1/fizzbuzz",
  "errors": [
    { "field": "int1", "message": "must be a decimal integer" },
    { "field": "limit", "message": "must be less than or equal to 10000" },
    { "field": "str2", "message": "is required" }
  ]
}
```

| Status | When |
|---|---|
| 400 | a parameter is missing, an integer is not in decimal notation, or a limit below is exceeded |
| 405 | any method other than GET |
| 500 | unexpected error; the body only says `"An unexpected error occurred."`, details are logged server-side |

### Limits

The statement accepts any integer and any string. Two bounds are the **only deliberate deviations** from it, required by "ready for production": without them, a single request with `limit = 2147483647` would try to build over 20 GB of JSON and crash the server for everyone.

| Bound | Default | Why this value |
|---|---|---|
| `limit` | 10 000 | Worst case (`int1 = int2 = 1`, two 50-character strings): 10 000 terms of ~103 bytes ≈ **1 MB** per response, generated in under a millisecond; 100 concurrent worst-case requests fit in ~100 MB. Also 100 times the classic 1-to-100 FizzBuzz. |
| `str1`, `str2` length | 50 characters | Keeps that worst case bounded (counted in Unicode code points, so an emoji counts as one). |

Both are configuration, not code: change or relax them with the properties below. Rationale in [ADR-0007](docs/adr/0007-operational-limits.md).

## Architecture

```
src/main/java/io/github/benjaminreiprich/fizzbuzz/
├── FizzBuzzApplication.java         Spring Boot entry point
├── api/                             HTTP adapter: controllers, DTOs, validation, error handling
│   ├── FizzBuzzController.java
│   ├── ApiExceptionHandler.java     RFC 9457 Problem Details
│   ├── dto/                         FizzBuzzRequest (query parameters), InvalidParameter
│   └── validation/                  @DecimalInteger, @MaxLimit, @MaxStringLength
├── application/
│   └── FizzBuzzService.java         use case
├── config/
│   ├── FizzBuzzConfiguration.java   declares the domain beans
│   ├── OpenApiConfiguration.java    OpenAPI title and description
│   └── FizzBuzzProperties.java      fizzbuzz.* limits, validated at startup
└── domain/                          pure Java, no framework dependency
    ├── FizzBuzzQuery.java           the five parameters: any integers, any strings
    └── FizzBuzzGenerator.java       FizzBuzzQuery -> List<String>
```

Request flow:

```
GET /api/v1/fizzbuzz?…
  → FizzBuzzController     binds and validates FizzBuzzRequest (@Valid); invalid → ApiExceptionHandler → 400
  → FizzBuzzRequest.toQuery()
  → FizzBuzzService.fizzBuzz(query)
  → FizzBuzzGenerator.generate(query)
  ← 200 ["1","2","fizz",…]
```

**Dependency rule:** `api → application → domain`; the domain depends only on the JDK, so business rules can be read and tested without Spring. `ArchitectureTest` (ArchUnit) fails the build if a domain class depends on anything else.

The `infrastructure` layer is not implemented yet.

## Design decisions

- Decisions are recorded as ADRs in `docs/adr/` — [ADR-0001](docs/adr/0001-record-architecture-decisions.md)
- Java 25 and Spring Boot 4.1.1 — [ADR-0002](docs/adr/0002-java-25-and-spring-boot-4-1.md)
- Formatting and coverage gates enforced by `./mvnw verify` from the first commit — [ADR-0003](docs/adr/0003-quality-gates-in-the-build.md)
- The domain accepts every input allowed by the statement (any integer, any string; operational limits are configuration) — [ADR-0005](docs/adr/0005-accept-every-input-allowed-by-the-statement.md), supersedes [ADR-0004](docs/adr/0004-domain-invariants-vs-configurable-limits.md)
- `GET` with query parameters under `/api/v1`, integers in decimal notation only — [ADR-0006](docs/adr/0006-get-endpoint-with-query-parameters.md)
- Configurable bounds on `limit` (10 000) and string length (50), the only deviations from the statement — [ADR-0007](docs/adr/0007-operational-limits.md)
- RFC 9457 Problem Details for every error, rejected values never echoed — [ADR-0008](docs/adr/0008-problem-details-error-responses.md)

## Configuration

| Property | Environment variable | Default | Description |
|---|---|---|---|
| `fizzbuzz.max-limit` | `FIZZBUZZ_MAX_LIMIT` | `10000` | Highest accepted `limit`; must be at least 1 |
| `fizzbuzz.max-string-length` | `FIZZBUZZ_MAX_STRING_LENGTH` | `50` | Highest accepted length of `str1` and `str2`, in characters; must be at least 1 |

Invalid values stop the application at startup with an explicit error.

## Testing strategy

| Kind | Status |
|---|---|
| Unit | `FizzBuzzQueryTest` (accepted inputs, equality), `FizzBuzzGeneratorTest` (specification example and edge cases: zero, negative and huge divisors, non-positive limits, empty strings) |
| Property-based (jqwik) | `FizzBuzzGeneratorPropertiesTest`: each rule of the specification checked on 1,000 random queries |
| Web slice (`@WebMvcTest`) | `FizzBuzzControllerTest` (happy path, every accepted input, every validation rule at its boundary, error body shape, 405), `FizzBuzzControllerConfiguredLimitsTest` (limits come from configuration), `ApiExceptionHandlerTest` (500 leaks nothing) |
| Configuration | `FizzBuzzPropertiesTest`: defaults, binding, startup failure on invalid limits |
| Architecture (ArchUnit) | `ArchitectureTest`: the domain depends only on the JDK |
| Smoke test (Spring context starts) | `FizzBuzzApplicationTests` |
| Integration (`@SpringBootTest`) | `OpenApiDocumentationTest`: the OpenAPI description documents the endpoint, its five required parameters and its responses; Swagger UI is served |

Everything runs with `./mvnw verify`, which also enforces:

- **Formatting**: Spotless with palantir-java-format; the build fails on unformatted code.
- **Coverage**: JaCoCo requires at least 90% line coverage on the `domain` and `application` packages. The HTML report is written to `target/site/jacoco/index.html`.

CI (GitHub Actions, `.github/workflows/ci.yml`) runs `./mvnw -B verify` on JDK 25 for every push to `main` and every pull request.

## Observability

Not implemented yet.

## Production considerations

- Request size is bounded ([Limits](#limits)), so one request cannot exhaust memory.
- Errors never expose stack traces or exception messages.
- Swagger UI and `/v3/api-docs` are enabled by default; whether to disable them in production (`springdoc.swagger-ui.enabled=false`, `springdoc.api-docs.enabled=false`) is decided in Phase 4.

Health probes, metrics, structured logging, Docker image: not implemented yet.

## Project history

- **Phase 0 — Bootstrap**: Spring Boot 4.1.1 / Java 25 project with Maven Wrapper, Spotless and JaCoCo gates, smoke test, GitHub Actions CI, first ADRs.
- **Phase 1 — Domain**: `FizzBuzzQuery` and `FizzBuzzGenerator`, covered by example-based and property-based (jqwik) tests; domain purity enforced by ArchUnit.
- **Statement compliance fix**: the domain accepts any integer and any string, as the statement requires (ADR-0005).
- **Phase 2 — FizzBuzz endpoint**: `GET /api/v1/fizzbuzz` with validated parameters, configurable limits, Problem Details errors and OpenAPI / Swagger UI documentation.
