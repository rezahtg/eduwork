# Eduwork Platform

Production-grade education marketplace connecting mentors with students for live 1-on-1 and group mentoring sessions.

## 🎯 Project Overview

Eduwork is a 3rd party platform that facilitates:
- **Mentors** creating flexible teaching schedules
- **Students** discovering and booking mentoring sessions
- Live video sessions (Google Meet/Zoom)
- Secure payment escrow system
- Pre-booking and in-session discussions
- Quality assurance through reviews and dispute resolution

## 📋 Current Status

🚧 **Phase 1 In Progress** - Foundation & MVP Core

- [x] Architecture & database design
- [x] API contracts (OpenAPI 3.0)
- [ ] Identity module (registration, auth, KYC)
- [ ] Scheduling module
- [ ] Booking & payment (manual transfer)

See [Roadmap](docs/roadmap.md) for complete development plan.

## 🏗️ Architecture

**Pattern**: Modular Monolith (can extract microservices later)

### Modules
- **Identity**: User registration, authentication, profile, KYC
- **Scheduling**: Mentor schedules, discovery (Elasticsearch)
- **Booking**: Session bookings, attendance
- **Discussion**: Pre-booking Q&A, in-session chat
- **Payment**: Manual verification (MVP) → Gateway integration (Phase 4)
- **Notification**: Email, in-app, push (Phase 2)
- **Audit**: Comprehensive activity logging

### Tech Stack
- **Backend**: Java 21, Spring Boot 3.2
- **Database**: PostgreSQL 15, Redis, Elasticsearch
- **Auth**: JWT with brute force protection
- **Live Sessions**: Google Meet / Zoom APIs
- **Documentation**: OpenAPI 3.0 (Swagger UI)

## 🚀 Quick Start

### Prerequisites
- Java 21 JDK
- Maven 3.9+
- Docker & Docker Compose

### 1. Clone Repository

```bash
git clone <repo-url>
cd eduwork
```

### 2. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- PostgreSQL (port 5432)
- Redis (port 6379)
- Elasticsearch (port 9200)
- Kibana (port 5601)

### 3. Run Backend

```bash
cd backend
./mvnw spring-boot:run
```

API available at: http://localhost:8080/api/v1

### 4. Access Swagger UI

Open: http://localhost:8080/api/v1/swagger-ui.html

## 📚 Documentation

- **[API Specification](docs/api-spec.yaml)** - OpenAPI 3.0 contract
- **[E2E Flows](docs/e2e-flows.md)** - Sequence diagrams for all journeys
- **[Implementation Plan](docs/implementation-plan.md)** - Architecture & design decisions
- **[Roadmap](docs/roadmap.md)** - 5-phase development timeline
- **[Development Guide](docs/DEVELOPMENT.md)** - Setup & troubleshooting

## 🧪 Testing

```bash
# Unit tests
./mvnw test

# Integration tests (Testcontainers)
./mvnw verify -P integration-test

# All tests with coverage (target: >70%)
./mvnw clean verify
```

## 📦 Project Structure

```
eduwork/
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/eduwork/
│   │   ├── identity/           # User, Auth, KYC
│   │   ├── scheduling/         # Schedules
│   │   ├── booking/            # Bookings
│   │   ├── discussion/         # Chat
│   │   ├── payment/            # Transactions
│   │   ├── notification/       # Alerts
│   │   └── audit/              # Logging
│   └── src/main/resources/
│       └── db/migration/       # Flyway SQL scripts
├── docs/                       # Documentation
├── frontend/                   # (Phase 5 - TBD)
└── docker-compose.yml          # Local dev environment
```

## 🛣️ Development Roadmap

| Phase | Timeline | Status | Deliverables |
|-------|----------|--------|--------------|
| **Phase 1** | Weeks 1-4 | 🚧 In Progress | MVP with manual payment |
| **Phase 2** | Weeks 5-7 | ⏳ Planned | Live sessions, discussions |
| **Phase 3** | Weeks 8-11 | ⏳ Planned | Group sessions, disputes, reviews |
| **Phase 4** | Weeks 12-15 | ⏳ Planned | Payment gateways, scaling |
| **Phase 5** | Weeks 16-20 | ⏳ Planned | Mobile apps, i18n, ML |

## 🔐 Security

- JWT-based authentication
- Brute force protection (5 attempts → 30 min lock)
- Rate limiting (60 req/min)
- Password hashing (BCrypt)
- SQL injection prevention (JPA/Hibernate)
- HTTPS only in production

## 🌍 Internationalization

- **UI**: Indonesian (default), English (Phase 5)
- **API**: English only
- **Currency**: IDR (Rupiah)
- **Timezone**: Asia/Jakarta

## 🤝 Contributing

1. Create feature branch: `git checkout -b feature/identity-module`
2. Write tests (>70% coverage required)
3. Commit: `git commit -m "feat: add user registration endpoint"`
4. Push: `git push origin feature/identity-module`
5. Create Pull Request

## 📝 License

Proprietary - Eduwork Platform © 2025

## 👥 Team

- **Backend**: Spring Boot, PostgreSQL, APIs
- **Frontend**: Next.js (Phase 5)
- **Mobile**: React Native/Flutter (Phase 5)
- **DevOps**: Docker, Kubernetes, CI/CD

## 📞 Contact

- **Email**: dev@eduwork.id
- **Documentation**: [Confluence/Notion link]
- **Issue Tracker**: GitHub Issues
