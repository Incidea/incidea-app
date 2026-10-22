# syntax=docker/dockerfile:1.7
#
# Production image: Maven build (tests skipped) + minimal JRE runtime.
#
ARG JAVA_VERSION=21

FROM eclipse-temurin:${JAVA_VERSION}-jdk AS build

RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN --mount=type=cache,id=backend-m2,target=/root/.m2 \
    ./mvnw -B -DskipTests dependency:go-offline

COPY src ./src

RUN --mount=type=cache,id=backend-m2,target=/root/.m2 \
    ./mvnw -B -DskipTests package

FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine AS prod

RUN addgroup -S app \
 && adduser -S -u 10001 -G app app

WORKDIR /app
COPY --from=build --chown=app:app /app/target/*.jar app.jar

USER app

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=5 \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health/liveness >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
