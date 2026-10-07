# FizzBuzz REST API

## Overview

A Spring Boot web service that will expose a configurable FizzBuzz endpoint (two divisors, two replacement strings, an upper limit) plus a statistics endpoint returning the most frequent request.

**Current state:** the FizzBuzz domain logic is implemented and tested, but not exposed over HTTP yet.

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

Current package layout:

```
src/main/java/io/github/benjaminreiprich/fizzbuzz/
├── FizzBuzzApplication.java     Spring Boot entry point
└── domain/                      pure Java, no framework dependency
    ├── FizzBuzzQuery.java       the five parameters, valid by construction
    └── FizzBuzzGenerator.java   FizzBuzzQuery -> List<String>
```

**Dependency rule:** the domain depends only on the JDK, so business rules can be read and tested without Spring. `ArchitectureTest` (ArchUnit) fails the build if a domain class depends on anything else.

The `api`, `application` and `infrastructure` layers are not implemented yet.

## Design decisions

- Decisions are recorded as ADRs in `docs/adr/` — [ADR-0001](docs/adr/0001-record-architecture-decisions.md)
- Java 25 and Spring Boot 4.1.1 — [ADR-0002](docs/adr/0002-java-25-and-spring-boot-4-1.md)
- Formatting and coverage gates enforced by `./mvnw verify` from the first commit — [ADR-0003](docs/adr/0003-quality-gates-in-the-build.md)
- The domain enforces only what makes FizzBuzz meaningful; operational limits are configuration — [ADR-0004](docs/adr/0004-domain-invariants-vs-configurable-limits.md)

## Configuration

Not implemented yet.

## Testing strategy

| Kind | Status |
|---|---|
| Unit | `FizzBuzzQueryTest` (invariants), `FizzBuzzGeneratorTest` (specification example and edge cases) |
| Property-based (jqwik) | `FizzBuzzGeneratorPropertiesTest`: each rule of the specification checked on 1,000 random queries |
| Architecture (ArchUnit) | `ArchitectureTest`: the domain depends only on the JDK |
| Smoke test (Spring context starts) | `FizzBuzzApplicationTests` |
| Slice / integration tests | Not implemented yet |

Everything runs with `./mvnw verify`, which also enforces:

- **Formatting**: Spotless with palantir-java-format; the build fails on unformatted code.
- **Coverage**: JaCoCo requires at least 90% line coverage on the `domain` and `application` packages (`application` does not exist yet). The HTML report is written to `target/site/jacoco/index.html`.

CI (GitHub Actions, `.github/workflows/ci.yml`) runs `./mvnw -B verify` on JDK 25 for every push to `main` and every pull request.

## Observability

Not implemented yet.

## Production considerations

Not implemented yet.

## Project history

- **Phase 0 — Bootstrap**: Spring Boot 4.1.1 / Java 25 project with Maven Wrapper, Spotless and JaCoCo gates, smoke test, GitHub Actions CI, first ADRs.
- **Phase 1 — Domain**: `FizzBuzzQuery` (invariants enforced at construction) and `FizzBuzzGenerator`, covered by example-based and property-based (jqwik) tests; domain purity enforced by ArchUnit.
