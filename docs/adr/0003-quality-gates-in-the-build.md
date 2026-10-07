# 3. Quality gates in the build

Date: 2026-10-07

## Status

Accepted

## Context

Formatting debates and slowly eroding test coverage are both cheaper to prevent than to fix. Any check that only runs "when someone remembers" ends up skipped, so the checks have to run in the same command developers and CI already use.

## Decision

`./mvnw verify` runs two gates, active from the first commit:

- **Spotless with palantir-java-format**: the build fails on unformatted Java or on trailing whitespace in Markdown/YAML. We chose palantir over google-java-format for its 4-space indent and 120-column limit, which keep lambdas and Spring code readable. `./mvnw spotless:apply` fixes violations.
- **JaCoCo**: at least 90% line coverage on the `domain` and `application` packages, where the business logic lives. Framework wiring (controllers, configuration) is excluded from the ratio; its tests check behaviour rather than line count.

## Consequences

- No formatting comments in code review, and the coverage gate applies from Phase 1 instead of being added at the end.
- A failing gate blocks the build locally and in CI; contributors must run `spotless:apply` before committing.
