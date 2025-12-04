# Phoenix Backend

A Spring Boot REST API for personal health and fitness measurement tracking with JWT authentication and API key support.

## Tech Stack

- **Java** with Spring Boot 3.x
- **Spring Security** with JWT authentication
- **Spring Data JPA** with PostgreSQL
- **Gradle** build system
- **Lombok** for cleaner code
- **AOP** for cross-cutting concerns

## Features

- **User Authentication**
  - User registration and login
  - JWT token-based authentication
  - Role-based access control (ADMIN)

- **Measurements API**
  - Track weight, height, and body circumferences
  - CRUD operations on measurements
  - User-specific measurement history
  - Recent measurements with configurable limits

- **API Key Management**
  - Generate API keys for external access
  - Admin-only key generation endpoint

- **Security**
  - Dual authentication (JWT + API Key)
  - Custom security filters
  - Password encryption with BCrypt

- **Monitoring & Logging**
  - AOP-based logging for controllers, services, and repositories
  - API key usage tracking

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL database
- Gradle (or use included gradlew)

### Running the Application

1. **Configure database** in `application.properties` or use environment-specific profiles

2. **Run with Gradle:**
   ```bash
   ./gradlew bootRun
   ```

3. **Run with specific profile:**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

4. **Build the project:**
   ```bash
   ./gradlew clean build
   ```

The application will start on `http://localhost:8080`

## API Documentation

See [POSTMAN_SETUP.md](POSTMAN_SETUP.md) for:
- Complete API endpoint documentation
- Postman collection import instructions
- Example requests and responses
- Authentication setup guide

Quick import:
- Collection: `postman_collection.json`
- Environment: `postman_environment.json`

## Configuration

### Profiles

- **dev** - Development profile with verbose logging
- **prod** - Production profile
- **test** - Testing profile

### Key Properties

```properties
server.port=8080
spring.profiles.active=dev

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# API Key Configuration
apikey.expiration-offset-ms=7776000000
```

## Project Structure

```
src/main/java/dev/tomislavmiksik/phoenixbe/
├── aspect/          # AOP aspects (logging)
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data transfer objects
├── entity/         # JPA entities
├── repository/     # Data repositories
├── security/       # Security filters and JWT handling
├── service/        # Business logic
└── util/           # Utility classes
```

## Testing

Run all tests:
```bash
./gradlew test
```

Run specific test:
```bash
./gradlew test --tests SecurityConfigIntegrationTest
```

## License

Private project for Spring Professional Certification preparation.
