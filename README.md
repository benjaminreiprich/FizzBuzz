# FizzBuzz REST API

[![CI](https://github.com/benjaminreiprich/FizzBuzz/actions/workflows/ci.yml/badge.svg)](https://github.com/benjaminreiprich/FizzBuzz/actions/workflows/ci.yml)

## Overview

A Spring Boot web service exposing a configurable FizzBuzz: given three integers `int1`, `int2`, `limit` and two strings `str1`, `str2`, it returns the numbers from 1 to `limit`, where multiples of `int1` are replaced by `str1`, multiples of `int2` by `str2`, and multiples of both by `str1str2`. A statistics endpoint reports the most frequent request and its number of hits.

**Current state:** both endpoints are available.

## Quick start

Prerequisite: **JDK 25**. Maven is not needed; the Maven Wrapper downloads the right version on first use. On Windows, use `mvnw.cmd` instead of `./mvnw`.

```bash
# Build, run all tests and quality gates
./mvnw verify

# Run locally on http://localhost:8080
./mvnw spring-boot:run

# Try it
curl "http://localhost:8080/api/v1/fizzbuzz?int1=3&int2=5&limit=15&str1=fizz&str2=buzz"
curl "http://localhost:8080/api/v1/statistics"

# Interactive documentation: http://localhost:8080/swagger-ui.html

# Fix formatting violations reported by verify
./mvnw spotless:apply
```

Run with Docker (no JDK needed):

```bash
docker build -t fizzbuzz-api .
docker run --rm -p 8080:8080 -p 8081:8081 fizzbuzz-api
```

Port 8080 serves the API, port 8081 the health probes and metrics ([Observability](#observability)). Logs are JSON inside the container.

On Windows, if `git clone` fails with *Filename too long*, clone into a shorter path or enable long paths: `git config --global core.longpaths true`.

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

Responses carry `Cache-Control: no-store`: a cached response would never reach the server, and the request would be missing from the statistics ([ADR-0013](docs/adr/0013-responses-are-not-cacheable.md)).

```bash
curl "http://localhost:8080/api/v1/fizzbuzz?int1=3&int2=5&limit=15&str1=fizz&str2=buzz"
```

```json
["1","2","fizz","4","buzz","fizz","7","8","fizz","buzz","11","fizz","13","14","fizzbuzz"]
```

### `GET /api/v1/statistics`

Takes no parameter (any parameter sent is ignored) and returns the most frequent FizzBuzz request and its number of hits:

```bash
curl "http://localhost:8080/api/v1/statistics"
```

```json
{"request": {"int1": 3, "int2": 5, "limit": 15, "str1": "fizz", "str2": "buzz"}, "hits": 42}
```

| Rule | Behaviour |
|---|---|
| Before any request | `200 {"request": null, "hits": 0}`: same shape, zero hits |
| What counts | Only valid requests (`200`); rejected requests (`400`) are not counted |
| Same request | All five parameters equal; integers compared as numbers (`05` and `5` are the same, reported as `5`), strings exactly. `(3, 5, fizz, buzz)` and `(5, 3, buzz, fizz)` are different requests |
| Tie | The request that reached the highest count first wins |

Rationale in [ADR-0010](docs/adr/0010-statistics-semantics.md). Statistics are kept in memory: see [Production considerations](#production-considerations).

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
│   ├── StatisticsController.java
│   ├── ApiExceptionHandler.java     RFC 9457 Problem Details
│   ├── dto/                         FizzBuzzRequest, InvalidParameter, StatisticsResponse, RequestParameters
│   └── validation/                  @DecimalInteger, @MaxLimit, @MaxStringLength
├── application/
│   ├── FizzBuzzService.java         use case: generate, then count the request
│   ├── StatisticsService.java       use case: most frequent request
│   └── port/                        RequestStatistics (interface), RequestHits
├── config/
│   ├── FizzBuzzConfiguration.java   declares the domain beans
│   ├── OpenApiConfiguration.java    OpenAPI title and description
│   └── FizzBuzzProperties.java      fizzbuzz.* limits, validated at startup
├── domain/                          pure Java, no framework dependency
│   ├── FizzBuzzQuery.java           the five parameters: any integers, any strings
│   └── FizzBuzzGenerator.java       FizzBuzzQuery -> List<String>
└── infrastructure/
    └── statistics/                  InMemoryRequestStatistics, the default RequestStatistics
```

Request flow:

```
GET /api/v1/fizzbuzz?…
  → FizzBuzzController     binds and validates FizzBuzzRequest (@Valid); invalid → ApiExceptionHandler → 400
  → FizzBuzzRequest.toQuery()
  → FizzBuzzService.fizzBuzz(query)
  → FizzBuzzGenerator.generate(query)
  → RequestStatistics.record(query)   only once the sequence is generated
  ← 200 ["1","2","fizz",…]
```

```
GET /api/v1/statistics
  → StatisticsController → StatisticsService → RequestStatistics.mostFrequent()   O(1)
  ← 200 {"request": {…}, "hits": n}
```

**Dependency rule:** `api → application → domain`, `infrastructure → application → domain`, `config` wires everything. Nothing depends on `api`, and `application` never depends on `api` or `infrastructure`. The domain depends only on the JDK, so business rules can be read and tested without Spring. `ArchitectureTest` (ArchUnit) fails the build on any violation.

## Design decisions

- Decisions are recorded as ADRs in `docs/adr/` — [ADR-0001](docs/adr/0001-record-architecture-decisions.md)
- Java 25 and Spring Boot 4.1.1 — [ADR-0002](docs/adr/0002-java-25-and-spring-boot-4-1.md)
- Formatting and coverage gates enforced by `./mvnw verify` from the first commit — [ADR-0003](docs/adr/0003-quality-gates-in-the-build.md)
- The domain accepts every input allowed by the statement (any integer, any string; operational limits are configuration) — [ADR-0005](docs/adr/0005-accept-every-input-allowed-by-the-statement.md), supersedes [ADR-0004](docs/adr/0004-domain-invariants-vs-configurable-limits.md)
- `GET` with query parameters under `/api/v1`, integers in decimal notation only — [ADR-0006](docs/adr/0006-get-endpoint-with-query-parameters.md)
- Configurable bounds on `limit` (10 000) and string length (50), the only deviations from the statement — [ADR-0007](docs/adr/0007-operational-limits.md)
- RFC 9457 Problem Details for every error, rejected values never echoed — [ADR-0008](docs/adr/0008-problem-details-error-responses.md)
- Statistics kept in memory behind the `RequestStatistics` port, thread-safe without a global lock, exact — [ADR-0009](docs/adr/0009-in-memory-statistics-store.md)
- Statistics semantics: zero hits before any request, only valid requests counted, parameters compared as values, first to reach a count wins ties — [ADR-0010](docs/adr/0010-statistics-semantics.md)
- Probes and Prometheus metrics on a separate management port, JSON logs, graceful shutdown, Swagger kept in production — [ADR-0011](docs/adr/0011-operability.md)
- Multi-stage Docker image: JRE only, non-root, layered jar, health check — [ADR-0012](docs/adr/0012-container-image.md)
- `Cache-Control: no-store` on both endpoints, so that every request reaches the server and is counted — [ADR-0013](docs/adr/0013-responses-are-not-cacheable.md)

## Configuration

| Property | Environment variable | Default | Description |
|---|---|---|---|
| `fizzbuzz.max-limit` | `FIZZBUZZ_MAX_LIMIT` | `10000` | Highest accepted `limit`; must be at least 1 |
| `fizzbuzz.max-string-length` | `FIZZBUZZ_MAX_STRING_LENGTH` | `50` | Highest accepted length of `str1` and `str2`, in characters; must be at least 1 |
| `management.server.port` | `MANAGEMENT_SERVER_PORT` | `8081` | Port of the health probes and Prometheus metrics; keep it internal |
| `logging.structured.format.console` | `LOGGING_STRUCTURED_FORMAT_CONSOLE` | *(unset: plain text)* | `ecs` for JSON logs |
| `springdoc.swagger-ui.enabled` | `SPRINGDOC_SWAGGER_UI_ENABLED` | `true` | Serve Swagger UI |
| `springdoc.api-docs.enabled` | `SPRINGDOC_API_DOCS_ENABLED` | `true` | Serve the OpenAPI description |

Invalid values stop the application at startup with an explicit error.

## Testing strategy

| Kind | Status |
|---|---|
| Unit | `FizzBuzzQueryTest` (accepted inputs, equality), `FizzBuzzGeneratorTest` (specification example and edge cases: zero, negative and huge divisors, non-positive limits, empty strings), `InMemoryRequestStatisticsTest` (counting, distinct requests, tie-break), `FizzBuzzServiceTest` (a request is counted only once its sequence is generated) |
| Property-based (jqwik) | `FizzBuzzGeneratorPropertiesTest`: each rule of the specification checked on 1,000 random queries; `InMemoryRequestStatisticsPropertiesTest`: the most frequent request of any random sequence matches a direct reading of the specification |
| Concurrency | `InMemoryRequestStatisticsConcurrencyTest`: 8 threads record 240,000 hits at once, repeated 5 times; no hit is lost and the leader is right |
| Web slice (`@WebMvcTest`) | `FizzBuzzControllerTest` (happy path, every accepted input, every validation rule at its boundary, error body shape, 405), `FizzBuzzControllerConfiguredLimitsTest` (limits come from configuration), `ApiExceptionHandlerTest` (500 leaks nothing), `StatisticsControllerTest` (response shape, empty state, integers of any size, ignored parameters) |
| Configuration | `FizzBuzzPropertiesTest`: defaults, binding, startup failure on invalid limits |
| Architecture (ArchUnit) | `ArchitectureTest`: the domain depends only on the JDK; layers follow the dependency rule |
| Smoke test (Spring context starts) | `FizzBuzzApplicationTests` |
| Integration (`@SpringBootTest`) | `OpenApiDocumentationTest`: the OpenAPI description documents the endpoint, its five required parameters and its responses; Swagger UI is served; `StatisticsIntegrationTest`: both endpoints end to end, rejected requests not counted, `05` and `5` counted together; `ActuatorEndpointsTest`: probes are UP, request metrics are exported, every other Actuator endpoint is closed, nothing is exposed on the public port |

Everything runs with `./mvnw verify`, which also enforces:

- **Formatting**: Spotless with palantir-java-format; the build fails on unformatted code.
- **Coverage**: JaCoCo requires at least 90% line coverage on the `domain` and `application` packages. The HTML report is written to `target/site/jacoco/index.html`.

CI (GitHub Actions, `.github/workflows/ci.yml`) runs on every push to `main` and every pull request:

1. `./mvnw -B verify` on JDK 25 (tests, formatting, coverage);
2. then builds the Docker image, starts it and checks that the `HEALTHCHECK` turns healthy, both endpoints answer, the process is not root, every log line is JSON, and `SIGTERM` triggers a graceful shutdown.

## Observability

Operational endpoints are served on a **separate management port (8081)**, so they never reach the public port. Only these are exposed ([ADR-0011](docs/adr/0011-operability.md)):

| Endpoint (port 8081) | Purpose |
|---|---|
| `/actuator/health` | Overall status (`UP` / `DOWN`), details hidden |
| `/actuator/health/liveness` | Liveness probe: restart the instance if it fails |
| `/actuator/health/readiness` | Readiness probe: stop routing traffic to the instance while it fails |
| `/actuator/prometheus` | Metrics in Prometheus format: `http_server_requests_seconds` per endpoint and status, JVM memory and GC, Tomcat threads |

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/prometheus
```

**Logging**: plain text by default; JSON in [Elastic Common Schema](https://www.elastic.co/guide/en/ecs/current/index.html) with `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`, so that a log collector can index every field. Unexpected errors are logged with their stack trace, never returned to clients.

## Production considerations

- Request size is bounded ([Limits](#limits)), so one request cannot exhaust memory.
- Errors never expose stack traces or exception messages.
- Health probes and Prometheus metrics on an internal port; every other Actuator endpoint is closed ([Observability](#observability)).
- Graceful shutdown: on stop, in-flight requests get up to 30 s to complete before the server exits.
- Docker image: JRE-only runtime, non-root user, layered jar for small rebuilds, `HEALTHCHECK` on the liveness probe, heap sized to 75% of the container memory ([ADR-0012](docs/adr/0012-container-image.md)).
- Swagger UI and `/v3/api-docs` stay enabled in production on purpose: the API is public and read-only. Disable them with `SPRINGDOC_SWAGGER_UI_ENABLED=false` and `SPRINGDOC_API_DOCS_ENABLED=false`.

Known limitations:

- Statistics are kept in memory: they are lost on restart and not shared between instances. The `RequestStatistics` port exists so that a shared store such as Redis can replace the in-memory one ([ADR-0009](docs/adr/0009-in-memory-statistics-store.md)).
- The number of distinct requests tracked is unbounded (about 200 bytes each): a client sending millions of different valid requests grows memory. Keeping the answer exact requires it; an approximate algorithm would be a deviation from the statement.

## Project history

- **Phase 0 — Bootstrap**: Spring Boot 4.1.1 / Java 25 project with Maven Wrapper, Spotless and JaCoCo gates, smoke test, GitHub Actions CI, first ADRs.
- **Phase 1 — Domain**: `FizzBuzzQuery` and `FizzBuzzGenerator`, covered by example-based and property-based (jqwik) tests; domain purity enforced by ArchUnit.
- **Statement compliance fix**: the domain accepts any integer and any string, as the statement requires (ADR-0005).
- **Phase 2 — FizzBuzz endpoint**: `GET /api/v1/fizzbuzz` with validated parameters, configurable limits, Problem Details errors and OpenAPI / Swagger UI documentation.
- **Phase 3 — Statistics**: `GET /api/v1/statistics` backed by a thread-safe in-memory store behind the `RequestStatistics` port.
- **Phase 4 — Production readiness**: health probes and Prometheus metrics on a separate management port, JSON logs, graceful shutdown, Docker image built and smoke-tested in CI.
