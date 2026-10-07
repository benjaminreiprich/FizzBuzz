# syntax=docker/dockerfile:1

# ---- Build: same toolchain as development (JDK 25 + Maven Wrapper) ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

# Tests are not run here: CI runs ./mvnw verify before building the image.
# "sh mvnw" does not rely on the executable bit, which a build context from Windows may lose.
# The cache mount keeps the Maven repository between builds without storing it in a layer.
RUN --mount=type=cache,target=/root/.m2 \
    sh mvnw -B --no-transfer-progress package -DskipTests \
    && cp target/fizzbuzz-api-*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---- Runtime: JRE only, non-root ----
FROM eclipse-temurin:25-jre

# The base image ships without curl; the health check below needs it.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --no-create-home app

WORKDIR /application

# One layer per category, from least to most frequently changed: a code change only rebuilds the last one.
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER app

# JSON logs for log collectors (ADR-0011).
ENV LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs

# 8080: public API. 8081: health probes and metrics, to keep internal.
EXPOSE 8080 8081

HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
    CMD curl --fail --silent --output /dev/null http://localhost:8081/actuator/health/liveness || exit 1

# Exec form: java runs as PID 1 and receives SIGTERM directly, so graceful shutdown works.
# MaxRAMPercentage: by default the JVM would only use 25% of the container's memory limit for its heap.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "application.jar"]
