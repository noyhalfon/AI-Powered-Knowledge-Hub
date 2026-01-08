# AI-Powered Knowledge Hub

A Spring Boot application built with Java and Gradle.

## Prerequisites

- Java 17 or higher
- Gradle 7.0 or higher (or use the included Gradle Wrapper)

## Getting Started

### Using Gradle Wrapper

1. Make the wrapper executable (if needed):
   ```bash
   chmod +x gradlew
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

### Using Local Gradle

1. Build the project:
   ```bash
   gradle build
   ```

2. Run the application:
   ```bash
   gradle bootRun
   ```

## Running the Application

Once the application is running, you can access:

- **API Health Check**: http://localhost:8080/api/health
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:knowledgehubdb`
  - Username: `sa`
  - Password: (leave empty)

## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/com/knowledgehub/
│   │   │   ├── KnowledgeHubApplication.java
│   │   │   └── controller/
│   │   │       └── HealthController.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/knowledgehub/
└── build.gradle
```

## Technologies Used

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Web** - RESTful web services
- **Spring Data JPA** - Database access
- **H2 Database** - In-memory database for development
- **Lombok** - Reduces boilerplate code
- **Gradle** - Build tool

## Development

The application uses H2 in-memory database for development. You can configure a different database by updating `application.properties`.

## License

This project is open source and available under the MIT License.