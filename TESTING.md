# Testing Guide

This document explains how to run and add tests for the ChatModel-Demo3 Spring Boot application.

## Prerequisites

- Java Development Kit (JDK) 21 or later
- Maven 3.6+ (or use the provided Maven Wrapper)
- (Optional) Docker for integration tests

## Running Tests

From the project root, execute:

```bash
# Run all tests (unit, integration, context load)
./mvnw test

# Or with Maven
mvn test
```

Test reports are generated under `target/surefire-reports`.

To run specific test classes or packages:

```bash
# Run all service implementation tests
./mvnw -Dtest=*ServiceImplTest test

# Run controller tests only
./mvnw -Dtest=*ControllerTest test
```

## Test Structure

- `src/test/java/com/andrew/chatmodeldemo3/Service` — unit tests for the service layer (JUnit 5 & Mockito)
- `src/test/java/com/andrew/chatmodeldemo3/Controller` — controller tests using MockMvc
- `src/test/java/com/andrew/chatmodeldemo3` — integration tests annotated with `@SpringBootTest`

## Writing New Tests

1. Place test classes under `src/test/java`, matching the application package structure.
2. Name test classes with a `*Test.java` suffix.
3. Use appropriate annotations:
   - `@ExtendWith(MockitoExtension.class)` for pure unit tests
   - `@WebMvcTest` or `@SpringBootTest` for controller/integration tests
4. Verify tests locally (`./mvnw test`) and in CI.

## Code Coverage

Generate a coverage report with JaCoCo:

```bash
./mvnw test jacoco:report
```

Open the HTML report at `target/site/jacoco/index.html`.

## Continuous Integration

Tests are run in the CI pipeline. See `Jenkinsfile` and `.github/workflows/ci.yml` for configuration.