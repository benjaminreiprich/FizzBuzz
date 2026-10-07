# 12. Container image

Date: 2026-10-07

## Status

Accepted

## Context

A container image is the usual delivery format for a service, and lets anyone run the API without installing Java. The image must be small, safe to run, quick to rebuild, and let the platform check its health and stop it cleanly.

## Decision

A multi-stage `Dockerfile`:

- **Build stage** on `eclipse-temurin:25-jdk` with the Maven Wrapper, so the image uses the same toolchain as development. Tests are skipped there because CI runs `./mvnw verify` first; a BuildKit cache mount keeps the Maven repository between builds.
- **Runtime stage** on `eclipse-temurin:25-jre`, running as a dedicated non-root user.
- **Layered jar** (`java -Djarmode=tools ... extract --layers`): dependencies and application code are separate layers, so a code change rebuilds and pushes only a small layer.
- **`HEALTHCHECK`** on the liveness probe (port 8081). The base image has no HTTP client, so `curl` is installed for it.
- **Exec-form `ENTRYPOINT`**: Java is PID 1 and receives `SIGTERM`, so graceful shutdown works. `-XX:MaxRAMPercentage=75.0`, because the JVM default would use only 25% of the container memory for the heap.
- **JSON logs** enabled in the image (ADR-0011).

Docker is not installed on the development machine: CI builds the image and smoke-tests it on every push.

## Consequences

- `docker build -t fizzbuzz-api . && docker run -p 8080:8080 fizzbuzz-api` is enough to run the API.
- The image is only verified in CI; a broken `Dockerfile` fails the pipeline, not a release.
