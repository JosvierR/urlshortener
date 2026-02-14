# syntax=docker/dockerfile:1

FROM gradle:8.5.0-jdk11 AS build
WORKDIR /workspace

# Copy source and build the fat jar
COPY --chown=gradle:gradle . .
RUN gradle --no-daemon shadowJar

FROM eclipse-temurin:11-jre-jammy AS runtime
WORKDIR /app

ENV LOG_PATH=/var/log/urlshortener

# Default exposed port for local development.
# The effective listening port should be controlled via the PORT env variable at runtime.
EXPOSE 7070

RUN mkdir -p ${LOG_PATH}/archive
VOLUME ["/var/log/urlshortener"]

COPY --from=build /workspace/build/libs/app.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
