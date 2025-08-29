# Multi-stage build for SOLSOLHEY backend (Spring Boot, Java 17)

FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Copy Gradle wrapper and build scripts first (leverage layer caching)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Ensure wrapper is executable
RUN chmod +x ./gradlew

# Copy sources
COPY src ./src

# Build (skip tests for faster dev image)
RUN ./gradlew --no-daemon clean build -x test


FROM eclipse-temurin:17-jre AS run
WORKDIR /app

# Copy built jar
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

# Expose default port (override with SERVER_PORT if needed)
EXPOSE 8080

# Optional JVM opts
ENV JAVA_OPTS=""
ENV SERVER_PORT=8080

# Install curl for container healthcheck
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Container healthcheck (expects /health endpoint)
HEALTHCHECK --interval=30s --timeout=5s --start-period=25s --retries=5 \
  CMD curl -fsS http://localhost:${SERVER_PORT}/health || exit 1

# Run application
ENTRYPOINT ["sh","-lc","java $JAVA_OPTS -jar /app/app.jar"]
