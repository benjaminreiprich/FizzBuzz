# 2. Java 25 and Spring Boot 4.1

Date: 2026-10-07

## Status

Accepted

## Context

The initial brief asked for Java 21 LTS and the latest stable Spring Boot. On 2026-10-07, start.spring.io lists Spring Boot 4.1.1 as the latest GA (4.2 is still a milestone). Java 25 has been an LTS release since September 2025 and is supported by Boot 4.1. Before switching, we ran `./mvnw verify` on a throwaway project with every tool this repository uses (Spotless + palantir-java-format, JaCoCo, jqwik, ArchUnit, Mockito); it passed with no blocking issue.

## Decision

Target Java 25 (Temurin) and pin Spring Boot 4.1.1 through `spring-boot-starter-parent`. `maven-enforcer-plugin` fails the build early on an older JDK.

## Consequences

- Longest support window available today, and no Java upgrade needed soon.
- Contributors need JDK 25 locally; CI and the future Docker image use the same version.
- Spring Boot 4 uses modular starters (`spring-boot-starter-webmvc`, `spring-boot-starter-webmvc-test`) and Jackson 3, so some Boot 3 examples found online will not apply as-is.
