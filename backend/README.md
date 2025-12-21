# Eduwork Platform - Backend

Production-grade education marketplace API built with Spring Boot.

## Architecture

**Pattern**: Modular Monolith
- **Identity Module**: User registration, authentication, KYC
- **Scheduling Module**: Mentor schedule management
- **Booking Module**: Session booking workflow
- **Discussion Module**: Pre-booking and in-session chat
- **Payment Module**: Manual payment verification (MVP), gateway-ready
- **Notification Module**: Email and in-app alerts
- **Audit Module**: Comprehensive activity logging

## Tech Stack

- **Java 21** - Latest LTS
- **Spring Boot 3.2** - Framework
- **PostgreSQL 15** - Primary database
- **Redis** - Session & caching
- **Elasticsearch** - Search & discovery
- **Flyway** - Database migrations
- **JWT** - Authentication
- **OpenAPI 3.0** - API documentation

## Quick Start

### Prerequisites
- Java 21 JDK
- Maven 3.9+
- Docker & Docker Compose

### 1. Start Infrastructure

```bash
# Start PostgreSQL, Redis, Elasticsearch
docker-compose up -d

# Verify services are healthy
docker-compose ps
```

### 2. Run Application

```bash
# Build and run
cd backend
./mvnw clean install
./mvnw spring-boot:run

# Or use IDE (IntelliJ/VSCode) to run EduworkApplication.java
```

### 3. Access Services

- **API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v1/api-docs
- **Kibana**: http://localhost:5601

## Development

### Database Migrations

Flyway migrations run automatically on startup. Manual commands:

```bash
# Migrate
./mvnw flyway:migrate

# Clean (dev only!)
./mvnw flyway:clean

# Info
./mvnw flyway:info
```

### Testing

```bash
# Unit tests
./mvnw test

# Integration tests (uses Testcontainers)
./mvnw verify -P integration-test

# All tests with coverage
./mvnw clean verify
```

### Code Quality

```bash
# Format code (if configured)
./mvnw spring-javaformat:apply

# Check style
./mvnw checkstyle:check
```

## Project Structure

```
backend/
├── src/main/java/com/eduwork/
│   ├── EduworkApplication.java
│   ├── common/              # Shared utilities
│   │   ├── config/          # Spring configurations
│   │   ├── exception/       # Global exception handling
│   │   ├── security/        # JWT, filters
│   │   └── util/            # Helper classes
│   ├── identity/            # User, Auth, KYC module
│   │   ├── domain/          # Entities
│   │   ├── repository/      # JPA repositories
│   │   ├── service/         # Business logic
│   │   └── controller/      # REST endpoints
│   ├── scheduling/          # Schedule module
│   ├── booking/             # Booking module
│   ├── discussion/          # Chat/Discussion  module
│   ├── payment/             # Payment module
│   ├── notification/        # Notification module
│   └── audit/               # Audit logging module
├── src/main/resources/
│   ├── application.yml      # Main config
│   ├── application-dev.yml  # Dev profile
│   ├── application-prod.yml # Production profile
│   └── db/migration/        # Flyway SQL scripts
└── src/test/
    ├── java/                # Unit & integration tests
    └── resources/           # Test configs
```

## Environment Variables

Required for production:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/eduwork
SPRING_DATASOURCE_USERNAME=eduwork_user
SPRING_DATASOURCE_PASSWORD=change-me

# JWT
JWT_SECRET=your-256-bit-secret-key-change-in-production

# Admin
ADMIN_PASSWORD=SecurePassword123!

# Email
MAIL_USERNAME=noreply@eduwork.id
MAIL_PASSWORD=smtp-password

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PASSWORD=redis-password

# Elasticsearch
SPRING_ELASTICSEARCH_URIS=http://localhost:9200
```

## API Documentation

Full API specification: [docs/api-spec.yaml](../docs/api-spec.yaml)

Key endpoints:
- `POST /auth/register` - User registration
- `POST /auth/login` - Authentication
- `GET /schedules` - Search mentor schedules
- `POST /bookings` - Create booking
- `POST /payments/{id}/proof` - Upload payment proof

## Deployment

### Docker Build

```bash
# Build image
docker build -t eduwork-api:latest .

# Run container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/eduwork \
  eduwork-api:latest
```

### Kubernetes

See `k8s/` directory for deployment manifests.

## Contributing

1. Create feature branch from `main`
2. Write tests for new features
3. Ensure >70% code coverage
4. Run `mvn verify` before committing
5. Create pull request

## License

Proprietary - Eduwork Platform © 2025
