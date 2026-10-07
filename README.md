# FizzBuzz REST API

## Overview

A Spring Boot web service that will expose a configurable FizzBuzz endpoint (two divisors, two replacement strings, an upper limit) plus a statistics endpoint returning the most frequent request.

**Current state:** project bootstrap only. The application builds, starts and is tested, but exposes no endpoint yet.

## Quick start

Prerequisite: **JDK 25**. Maven is not needed; the Maven Wrapper downloads the right version on first use. On Windows, use `mvnw.cmd` instead of `./mvnw`.

```bash
# Build, run all tests and quality gates
./mvnw verify

# Run locally (starts on http://localhost:8080; no endpoint yet)
./mvnw spring-boot:run

# Fix formatting violations reported by verify
./mvnw spotless:apply
```

Run with Docker: Not implemented yet.

## API

Not implemented yet.

## Architecture

Not implemented yet. The repository currently contains only the Spring Boot entry point, `FizzBuzzApplication`.

## Design decisions

- Decisions are recorded as ADRs in `docs/adr/` — [ADR-0001](docs/adr/0001-record-architecture-decisions.md)
- Java 25 and Spring Boot 4.1.1 — [ADR-0002](docs/adr/0002-java-25-and-spring-boot-4-1.md)
- Formatting and coverage gates enforced by `./mvnw verify` from the first commit — [ADR-0003](docs/adr/0003-quality-gates-in-the-build.md)

## Configuration

Not implemented yet.

## Testing strategy

| Kind | Status |
|---|---|
| Smoke test (Spring context starts) | `FizzBuzzApplicationTests` |
| Unit / property-based / slice / integration / architecture tests | Not implemented yet |

Everything runs with `./mvnw verify`, which also enforces:

- **Formatting**: Spotless with palantir-java-format; the build fails on unformatted code.
- **Coverage**: JaCoCo requires at least 90% line coverage on the `domain` and `application` packages (none exist yet). The HTML report is written to `target/site/jacoco/index.html`.

CI (GitHub Actions, `.github/workflows/ci.yml`) runs `./mvnw -B verify` on JDK 25 for every push to `main` and every pull request.

## Observability

Not implemented yet.

## Production considerations

Not implemented yet.

## Project history

- **Phase 0 — Bootstrap**: Spring Boot 4.1.1 / Java 25 project with Maven Wrapper, Spotless and JaCoCo gates, smoke test, GitHub Actions CI, first ADRs.
