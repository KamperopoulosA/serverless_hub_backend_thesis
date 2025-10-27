# Serverless Platform Selector API

A comprehensive backend API for serverless platform selection and deployment system built with Spring Boot 3.x and Java 17.

## Features

- **Platform Management**: CRUD operations for serverless platforms with JSON-based feature storage
- **Advanced Filtering**: Multi-criteria filtering and weighted ranking of platforms
- **Credential Management**: Secure storage and encryption of user credentials for multiple cloud platforms
- **Deployment Orchestration**: Integration with Serverless Framework CLI for automated deployments
- **Comprehensive API Documentation**: Auto-generated OpenAPI/Swagger documentation
- **Robust Error Handling**: Global exception handling with meaningful error responses
- **Security**: AES encryption for sensitive data and CORS configuration

## Technology Stack

- **Java 17+**
- **Spring Boot 3.2.0**
- **Spring Data JPA** with Hibernate
- **PostgreSQL 14+**
- **Spring Security**
- **OpenAPI 3** (Swagger)
- **JUnit 5** & Mockito for testing
- **Maven** for dependency management

## Architecture

The application follows Clean Architecture principles with clear separation of concerns:

```
├── Controller Layer    # REST endpoints, validation, HTTP handling
├── Service Layer      # Business logic, orchestration
├── Repository Layer   # Data access with Spring Data JPA
├── Entity Layer       # JPA entities
├── DTO Layer         # Data Transfer Objects
├── Exception Layer   # Custom exceptions and global error handling
└── Utility Layer     # Encryption, helper utilities
```

## Database Schema

### Platforms
- Stores serverless platform information with JSON-based features
- Supports categories, descriptions, and flexible feature definitions

### User Credentials
- Encrypted storage of user credentials for different platforms
- Links users to platforms with key-value credential pairs

### Deployment Records
- Tracks deployment history, status, and endpoints
- Stores deployment logs for troubleshooting

## API Endpoints

### Platform Management
- `GET /api/platforms` - Retrieve all platforms
- `POST /api/platforms` - Create new platform
- `PUT /api/platforms/{id}` - Update platform
- `DELETE /api/platforms/{id}` - Delete platform
- `POST /api/platforms/search` - Advanced filtering and search

### Credential Management
- `POST /api/credentials` - Save/update user credentials

### Deployment Management
- `POST /api/deployments` - Deploy serverless function

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+
- Serverless Framework CLI (for deployments)

### Database Setup
1. Create PostgreSQL database:
```sql
CREATE DATABASE serverless_platform_db;
```

2. Update `application.yml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/serverless_platform_db
    username: your_username
    password: your_password
```

### Running the Application

1. Clone the repository
2. Install dependencies:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

4. Access the API documentation at: `http://localhost:8080/swagger-ui.html`

### Environment Profiles

- **Development**: `application-dev.yml` - Detailed logging, create-drop schema
- **Production**: `application-prod.yml` - Minimal logging, validate schema

Activate profiles using:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Configuration

### Environment Variables
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `ENCRYPTION_SECRET_KEY` - AES encryption key (32 characters)
- `DATABASE_URL` - Full database URL (production)

### Application Properties
Key configuration options in `application.yml`:
- Server port and context path
- Database connection settings
- JPA/Hibernate configuration
- Logging levels
- Encryption settings

## Security

- **Credential Encryption**: All sensitive credentials are encrypted using AES
- **Input Validation**: Comprehensive validation using Bean Validation
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Error Handling**: Secure error responses without sensitive information exposure

## Testing

Run tests with:
```bash
mvn test
```

The project includes:
- Unit tests for services and controllers
- Integration tests with test containers
- Mock-based testing for external dependencies

## Deployment Integration

The system integrates with Serverless Framework CLI to:
1. Extract function packages from base64-encoded uploads
2. Generate platform-specific `serverless.yml` configurations
3. Execute deployments using ProcessBuilder
4. Parse deployment output for endpoint URLs
5. Store deployment records and logs

### Supported Platforms
- AWS Lambda
- Azure Functions
- Google Cloud Functions

## API Documentation

Interactive API documentation is available at `/swagger-ui.html` when the application is running. The documentation includes:
- Complete endpoint descriptions
- Request/response schemas
- Example payloads
- Error response formats

## Monitoring and Logging

- Structured logging with SLF4J and Logback
- Configurable log levels per environment
- Request/response logging for debugging
- Performance monitoring capabilities

## Contributing

1. Follow the existing code structure and patterns
2. Write comprehensive tests for new features
3. Update API documentation for new endpoints
4. Follow Java coding conventions and Spring Boot best practices

## License

This project is licensed under the MIT License.