# Local Development Setup Guide

This guide explains how to run the Eduwork platform locally using Docker for all dependencies.

## 🚀 Quick Start

### 1. Start All Services

```bash
# Start PostgreSQL, Redis, Elasticsearch, Kibana, and MailHog
docker-compose up -d

# Check all services are running
docker-compose ps
```

### 2. Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or using your IDE
# Run EduworkApplication.java
```

### 3. Verify Everything Works

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Try registration endpoint
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"SecurePass123!","role":"STUDENT"}'
```

---

## 📦 Services Overview

| Service | Port | Purpose | Web UI |
|---------|------|---------|--------|
| **Application** | 8080 | Spring Boot API | - |
| **PostgreSQL** | 5432 | Database | - |
| **Redis** | 6379 | Caching | - |
| **Elasticsearch** | 9200 | Search Engine | - |
| **Kibana** | 5601 | ES Visualization | http://localhost:5601 |
| **MailHog** | 1025 (SMTP)<br>8025 (UI) | Email Testing | http://localhost:8025 |

---

## 🗄️ Database Access

### Using psql Command Line
```bash
# Access PostgreSQL
docker exec -it eduwork-postgres psql -U eduwork_user -d eduwork_dev

# List tables
\dt

# Query users
SELECT * FROM users;

# Exit
\q
```

### Using Database IDE (DBeaver, DataGrip, etc.)
```
Host: localhost
Port: 5432
Database: eduwork_dev
Username: eduwork_user
Password: eduwork_password
```

---

## 📧 Email Testing with MailHog

1. **Send emails** - Application sends to localhost:1025
2. **View emails** - Open http://localhost:8025
3. **Check inbox** - All emails appear here instantly
4. **Test verification links** - Click links directly from MailHog UI

**Benefits:**
- No real email account needed
- Instant delivery
- See all emails in browser
- Test email verification flow

---

## 🔍 Elasticsearch & Kibana

### Elasticsearch
```bash
# Check cluster health
curl http://localhost:9200/_cluster/health

# List all indices
curl http://localhost:9200/_cat/indices?v
```

### Kibana
- Open http://localhost:5601
- Explore data
- Create visualizations
- Monitor Elasticsearch

---

## 🧪 Testing

### Run All Tests
```bash
# Unit + Integration tests (uses H2)
mvn test

# Test with actual PostgreSQL in Docker
SPRING_PROFILES_ACTIVE=dev mvn test
```

**Note:** Tests use H2 in-memory database by default. Development environment uses PostgreSQL via Docker.

---

## 🛠️ Common Commands

### Docker Compose

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f postgres

# Restart a service
docker-compose restart postgres

# Check status
docker-compose ps
```

### Database Management

```bash
# Backup database
docker exec eduwork-postgres pg_dump -U eduwork_user eduwork_dev > backup.sql

# Restore database
docker exec -i eduwork-postgres psql -U eduwork_user eduwork_dev < backup.sql

# Reset database (drop all tables)
docker exec eduwork-postgres psql -U eduwork_user -d eduwork_dev -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

### Clear Redis Cache

```bash
# Clear all Redis data
docker exec eduwork-redis redis-cli FLUSHALL

# Check Redis keys
docker exec eduwork-redis redis-cli KEYS '*'
```

---

## 🔄 Development Workflow

### First Time Setup
```bash
# 1. Clone repository
git clone <repository>
cd eduwork

# 2. Start services
docker-compose up -d

# 3. Wait for services (check health)
docker-compose ps

# 4. Run application
mvn spring-boot:run

# 5. Test registration
# See curl-commands.md for examples
```

### Daily Development
```bash
# Start Docker services (if not running)
docker-compose up -d

# Run application
mvn spring-boot:run

# Make code changes... (auto-restart with spring-boot-devtools)

# View emails at http://localhost:8025
# View ES data at http://localhost:5601

# Stop when done
docker-compose down
```

---

## 🐛 Troubleshooting

### Application can't connect to PostgreSQL

```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres

# Test connection manually
docker exec eduwork-postgres psql -U eduwork_user -d eduwork_dev -c "SELECT 1"
```

### Port already in use

```bash
# Check what's using port
# Windows
netstat -ano | findstr :5432

# Linux/Mac
lsof -i :5432

# Stop conflicting service or change docker-compose port
```

### Redis connection refused

```bash
# Check Redis is running
docker-compose ps redis

# Test Redis
docker exec eduwork-redis redis-cli ping
# Should return: PONG
```

### Clear everything and start fresh

```bash
# Stop and remove everything
docker-compose down -v

# Remove Docker networks
docker network prune

# Start again
docker-compose up -d
```

---

## 📊 Monitoring

### Application Actuator
```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Info
curl http://localhost:8080/actuator/info
```

### Docker Stats
```bash
# Resource usage
docker stats

# Container health
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

---

## 🎯 Next Steps

1. ✅ Services running
2. ✅ Database connected
3. ✅ Application started
4. ➡️ Try the API endpoints (see `docs/api-testing/curl-commands.md`)
5. ➡️ View emails in MailHog at http://localhost:8025
6. ➡️ Explore data in Kibana at http://localhost:5601

---

## 💡 Tips

1. **Leave Docker running** - Services start instantly when already running
2. **Use MailHog** - Much easier than real email servers for testing
3. **Monitor logs** - `docker-compose logs -f` shows all service logs
4. **Reset when stuck** - `docker-compose down -v` cleans everything
5. **Keep data** - Volumes persist data between restarts

---

**Need help?** Check logs: `docker-compose logs -f [service-name]`

**Last Updated:** 2025-12-21
