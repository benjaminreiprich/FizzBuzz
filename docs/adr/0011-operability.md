# 11. Operability: probes, metrics, logs, shutdown

Date: 2026-10-07

## Status

Accepted

## Context

"Ready for production" is not defined by the statement. We kept the expectations any platform has of a service, as long as they cost configuration rather than code: tell whether it is alive and ready, expose its metrics, emit logs a collector can parse, stop without cutting requests. Correlation ids, custom metrics and docker-compose were considered and left out: they add code or files with little value for a single stateless service.

## Decision

- **Actuator on a separate management port (8081)**, exposing only `health` (with `liveness` and `readiness` probes) and `prometheus`. Probes and metrics never reach the public port; everything else (`env`, `beans`, `heapdump`...) stays closed, as it can reveal configuration or memory content. Health details are hidden.
- **Prometheus metrics** come from Spring's built-in instrumentation (`http_server_requests_seconds` per endpoint, JVM, Tomcat); no custom metric is needed.
- **Structured JSON logs (ECS)** are enabled in the container image only, through `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`; local runs keep readable text.
- **Graceful shutdown** is Spring Boot's default; it is made explicit in `application.yml` with its 30 s limit.
- **Swagger UI and the OpenAPI description stay enabled in production**: the API is public and read-only, its documentation reveals nothing sensitive. They can be disabled with `SPRINGDOC_SWAGGER_UI_ENABLED=false` and `SPRINGDOC_API_DOCS_ENABLED=false`.

## Consequences

- The platform must route probes and Prometheus scrapes to port 8081, and keep that port internal.
- Every behaviour is plain Spring Boot configuration: nothing to maintain in code.
