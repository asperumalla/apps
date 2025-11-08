# Multi-stage build for Spring Boot application
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Gradle wrapper files first (must be before build)
COPY gradlew .
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src src

# Build the application (skip tests for faster build)
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the JAR from build stage (use explicit name to avoid wildcard issues)
COPY --from=build /app/build/libs/payment-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port (can be overridden by platform)
EXPOSE 8090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

