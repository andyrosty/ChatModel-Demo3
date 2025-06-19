# syntax=docker/dockerfile:1
# Multi-stage Dockerfile to build and run the Spring Boot application

## Build stage
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
# Copy pom and download dependencies for caching
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests -B

## Runtime stage
FROM openjdk:21-slim
WORKDIR /app
# Copy the packaged jar from the build stage
COPY --from=build /app/target/*.jar app.jar
# Expose default Spring Boot port
EXPOSE 8080
# Run the application
ENTRYPOINT ["java","-jar","app.jar"]