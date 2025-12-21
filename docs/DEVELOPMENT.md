# Eduwork Development Guide

## Getting Started

This guide walks through setting up the Eduwork platform for local development.

## Step 1: Start Infrastructure

Start the required services using Docker Compose:

```bash
# From project root
docker-compose up -d

# Check all services are running
docker-compose ps

# Should show: postgres, redis, elasticsearch, kibana all healthy
```

**Verify services**:
- PostgreSQL: `psql -h localhost -U eduwork_user -d eduwork` (password: eduwork_pass)
- Redis: `redis-cli ping` (should return PONG)
- Elasticsearch: `curl http://localhost:9200` (should return cluster info)
- Kibana: Open http://localhost:5601 in browser

## Step 2: Build and Run Backend

```bash
cd backend

# First time: install dependencies
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The API will start on http://localhost:8080/api/v1

## Step 3: Verify Setup

### Check API Health

```bash
curl http://localhost:8080/api/v1/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### Access Swagger UI

Open in browser: http://localhost:8080/api/v1/swagger-ui.html

You should see all API endpoints documented.

### Test Database Connection

Check Flyway migrations ran successfully:

```bash
# Connect to database
psql -h localhost -U eduwork_user -d eduwork

# List tables
\dt

# Should show: users, user_profiles, schedules, bookings, transactions, etc.
```

## Step 4: Create Test Data (Optional)

```bash
# Run seed script (when available)
./mvnw exec:java -Dexec.mainClass="com.eduwork.DevDataSeeder"
```

## Common Development Tasks

### Run Tests

```bash
# Unit tests only
./mvnw test

# Integration tests (uses Testcontainers)
./mvnw verify -P integration-test
```

### Database Management

```bash
# Reset database (dev only!)
./mvnw flyway:clean flyway:migrate

# View migration history
./mvnw flyway:info

# Repair migration checksum
./mvnw flyway:repair
```

### Debugging

Run with debug enabled (port 5005):

```bash
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

In IntelliJ/VSCode, attach debugger to localhost:5005

### View Logs

```bash
# Application logs
tail -f backend/logs/eduwork.log

# Docker container logs
docker-compose logs -f postgres
docker-compose logs -f elasticsearch
```

## Troubleshooting

### Port Already in Use

```bash
# Kill process on port 8080
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

### Database Connection Failed

```bash
# Restart PostgreSQL
docker-compose restart postgres

# Check PostgreSQL logs
docker-compose logs postgres
```

### Elasticsearch Not Responding

```bash
# Increase memory (edit docker-compose.yml)
# Change ES_JAVA_OPTS to -Xms1g -Xmx1g

docker-compose down
docker-compose up -d elasticsearch
```

### Flyway Migration Conflict

```bash
# Development only - clean and remigrate
./mvnw flyway:clean
./mvnw flyway:migrate
```

## Next Steps

1. Read [API Specification](./docs/api-spec.yaml)
2. Review [E2E Flows](./docs/e2e-flows.md)
3. Check [Roadmap](./docs/roadmap.md) for current phase tasks
4. Start implementing Identity module endpoints

## Code Standards

- **Java**: Follow Google Java Style Guide
- **SQL**: Lowercase with underscores
- **API**: RESTful conventions, kebab-case URLs
- **Git**: Conventional commits (feat:, fix:, docs:, etc.)

## Environment Profiles

- **default**: Local development
- **test**: Integration tests
- **prod**: Production (set via SPRING_PROFILES_ACTIVE)

## Useful Commands

```bash
# Clean build
./mvnw clean package

# Skip tests
./mvnw install -DskipTests

# Update dependencies
./mvnw versions:display-dependency-updates

# Generate dependency tree
./mvnw dependency:tree
```
