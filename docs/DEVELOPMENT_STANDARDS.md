# Eduwork Development Standards & Best Practices

## Overview

This document defines mandatory constraints, rules, and practices for Eduwork platform development.

---

## Development Constraints

### 1. Test-Driven Development (TDD)

**Mandatory**: Write failing tests FIRST, then implement code to make them pass.

**Workflow**:
```
1. Write failing test (RED)
2. Write minimal code to pass (GREEN)
3. Refactor for quality (REFACTOR)
4. Repeat
```

**Test Coverage Requirements**:
- **Unit Tests**: >70% coverage minimum
- **Edge Cases**: All boundary conditions tested
- **Error Cases**: All exception paths covered
- **Simple Tests**: Prefer clarity over cleverness

**Example Test Structure**:
```java
@Test
void shouldThrowException_whenEmailAlreadyExists() {
    // Given (setup)
    var existingUser = UserFixtures.createUser("test@example.com");
    userRepository.save(existingUser);
    
    var request = new RegisterRequest("test@example.com", "password");
    
    // When & Then
    assertThrows(EmailAlreadyExistsException.class, () -> {
        userService.register(request);
    });
}
```

### 2. Clean Architecture

**Layered Architecture (Hexagonal/Ports & Adapters)**:

```
┌─────────────────────────────────────┐
│         Presentation Layer          │  ← Controllers, DTOs
│  (REST, GraphQL, gRPC adapters)     │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│         Application Layer           │  ← Use Cases, Services
│   (Business Logic Orchestration)    │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│           Domain Layer              │  ← Entities, Value Objects
│      (Core Business Rules)          │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│       Infrastructure Layer          │  ← JPA, Redis, External APIs
│  (Database, Cache, External Svcs)   │
└─────────────────────────────────────┘
```

**Package Structure per Module**:
```
com/eduwork/identity/
├── domain/
│   ├── model/              # Entities, Value Objects
│   ├── repository/         # Repository interfaces (ports)
│   └── service/            # Domain services
├── application/
│   ├── usecase/            # Use case implementations
│   ├── dto/                # Request/Response DTOs
│   └── mapper/             # DTO ↔ Entity mappers
├── infrastructure/
│   ├── persistence/        # JPA implementations
│   ├── cache/              # Redis implementations
│   └── external/           # External API clients
└── presentation/
    ├── controller/         # REST controllers
    └── exception/          # Controller advice
```

**Dependency Rules**:
- ❌ Domain NEVER depends on infrastructure
- ❌ Domain NEVER depends on presentation
- ✅ Infrastructure depends on domain (implements interfaces)
- ✅ Presentation depends on application layer

### 3. Clean Code Principles

**Naming Conventions**:
- Classes: `PascalCase` (e.g., `UserRegistrationService`)
- Methods: `camelCase`, verb-first (e.g., `validateEmail()`)
- Variables: `camelCase`, descriptive (e.g., `verifiedUser`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_LOGIN_ATTEMPTS`)
- Booleans: `is/has/can` prefix (e.g., `isEmailVerified`)

**Method Size**: Max 20 lines, single responsibility

**Class Size**: Max 300 lines, cohesive purpose

**Lombok Usage**:
- **Domain Layer**: Use `@Getter` only, keep explicit constructors with business rules
  ```java
  @Getter
  public class User {
      private UUID id;
      // Explicit constructor with business logic
      public User(String email, String password) {
          this.id = UUID.randomUUID(); // Business rule
          this.email = normalizeEmail(email); // Business rule
      }
  }
  ```
- **DTOs (Application Layer)**: Use `@Data`, `@Builder`
  ```java
  @Data
  @Builder
  public class RegisterRequestDTO {
      private String email;
      private String password;
  }
  ```
- **JPA Entities (Infrastructure)**: Use `@Getter`, `@Setter`, `@NoArgsConstructor`
  ```java
  @Entity
  @Getter @Setter
  @NoArgsConstructor
  public class UserEntity { }
  ```
- **Value Objects**: Use `@Value` (immutable)
  ```java
  @Value
  public class EmailVerificationToken {
      String token;
      Instant expiresAt;
  }
  ```

**Comments**: 
- ❌ What the code does (code should be self-explanatory)
- ✅ Why the code exists (business rationale)
- ✅ Complex algorithm explanations
- ✅ Security considerations

**Example**:
```java
// ❌ BAD: Comment states the obvious
// Get user by email
User user = userRepository.findByEmail(email);

// ✅ GOOD: Code is self-explanatory
User user = userRepository.findByEmail(email);

// ✅ GOOD: Explains WHY
// Use READ_UNCOMMITTED to prevent phantom reads during concurrent registrations
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public void register(RegisterRequest request) { ... }
```

### 4. Security-First Development

**Mandatory Security Practices**:

1. **Input Validation**: All endpoints validate input
   ```java
   @PostMapping("/register")
   public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
       // @Valid triggers Bean Validation
   }
   ```

2. **SQL Injection Prevention**: Only JPA/JPQL (no raw SQL)
   ```java
   // ✅ GOOD: Parameterized query
   @Query("SELECT u FROM User u WHERE u.email = :email")
   Optional<User> findByEmail(@Param("email") String email);
   
   // ❌ BAD: String concatenation
   String query = "SELECT * FROM users WHERE email = '" + email + "'";
   ```

3. **Password Security**: BCrypt with min 10 rounds
   ```java
   String hashedPassword = passwordEncoder.encode(rawPassword);
   ```

4. **Sensitive Data**: Never log passwords, tokens, PII
   ```java
   // ❌ BAD
   log.info("User login: {}", loginRequest); // Contains password
   
   // ✅ GOOD
   log.info("User login attempt: email={}", loginRequest.getEmail());
   ```

5. **Authorization**: Check permissions on every endpoint
   ```java
   @PreAuthorize("hasRole('MENTOR')")
   @PostMapping("/schedules")
   public ResponseEntity<?> createSchedule() { ... }
   ```

6. **Rate Limiting**: Prevent abuse
   ```java
   @RateLimiter(name = "registration", fallbackMethod = "rateLimitFallback")
   public void register() { ... }
   ```

### 5. Idempotent APIs

**All write operations MUST be idempotent**.

**POST (Creation)**:
- Use idempotency keys for payment/booking
- Return 200 + existing resource if duplicate

```java
@PostMapping("/bookings")
public ResponseEntity<?> createBooking(
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @RequestBody BookingRequest request) {
    
    // Check if already processed
    Optional<Booking> existing = bookingRepository
        .findByIdempotencyKey(idempotencyKey);
    
    if (existing.isPresent()) {
        return ResponseEntity.ok(existing.get()); // 200, not 201
    }
    
    // Create new booking
    Booking booking = bookingService.create(request, idempotencyKey);
    return ResponseEntity.status(HttpStatus.CREATED).body(booking);
}
```

**PUT (Update)**:
- Use optimistic locking (version field)
- Return 409 Conflict if version mismatch

```java
@Entity
public class Schedule {
    @Version
    private Long version;
}

// Service layer
public void updateSchedule(UUID id, UpdateRequest request, Long version) {
    Schedule schedule = scheduleRepository.findById(id)
        .orElseThrow(NotFoundException::new);
    
    if (!schedule.getVersion().equals(version)) {
        throw new OptimisticLockException("Schedule was modified");
    }
    
    // Apply updates
}
```

**DELETE**:
- Soft delete (set deleted_at)
- Idempotent by nature (deleting deleted item = no-op)

### 6. High Concurrency Support

**Database Level**:
- Use optimistic locking for updates
- Use database constraints (UNIQUE, CHECK)
- Use SELECT FOR UPDATE for critical sections

```java
// Critical section: booking limited seats
@Transactional
public Booking bookSchedule(UUID scheduleId, UUID studentId) {
    // Lock schedule row
    Schedule schedule = scheduleRepository
        .findByIdForUpdate(scheduleId)
        .orElseThrow(NotFoundException::new);
    
    if (schedule.getBookedCount() >= schedule.getMaxStudents()) {
        throw new ScheduleFullException();
    }
    
    // Create booking (transactional)
    Booking booking = new Booking(schedule, studentId);
    
    // Update count
    schedule.incrementBookedCount();
    
    return bookingRepository.save(booking);
}
```

**Application Level**:
- Stateless services
- Redis distributed locks for external APIs
- Connection pooling tuning

**Async Processing**:
- Use `@Async` for notifications
- Use message queues for batch jobs

### 7. Testability & Maintainability

**Constructor Injection** (enables testing):
```java
// ✅ GOOD: Constructor injection
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
}

// ❌ BAD: Field injection (hard to test)
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

**Interface Segregation**:
```java
// Define interfaces for external dependencies
public interface EmailService {
    void sendVerificationEmail(String to, String token);
}

// Implementation
@Service
public class SmtpEmailService implements EmailService { ... }

// Easy to mock in tests
@MockBean
private EmailService emailService;
```

---

## Development Rules

### Rule 1: Always Propose Design Before Coding

**Workflow**:
1. Receive requirement
2. Create design document (in docs/ or as artifact)
3. Get approval
4. Implement with TDD

**Design Document Must Include**:
- Use case description
- API endpoint design
- Request/Response samples
- Database changes (if any)
- Security considerations
- Performance considerations
- Test scenarios

### Rule 2: Challenge Bad Decisions

**When to Challenge**:
- ❌ N+1 query patterns
- ❌ Premature optimization
- ❌ Over-engineering
- ❌ Security vulnerabilities
- ❌ Violation of SOLID principles

**How to Challenge**:
1. Explain the problem
2. Propose alternative
3. Provide trade-offs
4. Let user decide

### Rule 3: Optimize for Production, Not Demo

**Production Checklist**:
- ✅ Error handling (never expose stack traces)
- ✅ Logging (structured, with correlation IDs)
- ✅ Monitoring (metrics, health checks)
- ✅ Database indexes
- ✅ Connection pooling
- ✅ Timeout configurations
- ✅ Retry logic with backoff

**Demo Anti-Patterns to Avoid**:
- ❌ `@Transactional` on controllers
- ❌ Returning entities from controllers
- ❌ Hardcoded credentials
- ❌ Disabled security
- ❌ No error handling

### Rule 4: Always Add Documentation for KT

**Code Documentation**:
- JavaDoc for all public APIs
- README per module
- Architecture Decision Records (ADRs)

**API Documentation**:
- OpenAPI annotations
- Request/Response examples
- Error codes catalog

**Knowledge Transfer Docs**:
- Runbooks (how to deploy, rollback)
- Troubleshooting guides
- Onboarding guides

---

## Code Review Checklist

Before committing code, verify:

### Functionality
- [ ] All tests pass (unit + integration)
- [ ] TDD approach followed (tests written first)
- [ ] Edge cases covered
- [ ] Error handling implemented

### Architecture
- [ ] Clean Architecture layers respected
- [ ] No circular dependencies
- [ ] Domain logic in domain layer
- [ ] Infrastructure details isolated

### Code Quality
- [ ] Methods < 20 lines
- [ ] Classes < 300 lines
- [ ] Descriptive names (no abbreviations)
- [ ] No magic numbers (use constants)
- [ ] SonarQube issues resolved

### Security
- [ ] Input validation present
- [ ] No SQL injection risk
- [ ] No sensitive data in logs
- [ ] Authorization checks present
- [ ] Rate limiting configured

### Performance
- [ ] No N+1 queries
- [ ] Indexes on foreign keys
- [ ] Connection pooling configured
- [ ] Async for long-running tasks

### Documentation
- [ ] JavaDoc on public methods
- [ ] Comments explain "why", not "what"
- [ ] README updated (if needed)
- [ ] API spec updated

---

## Testing Standards

### Unit Test Template

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("Should register user successfully when valid input")
    void shouldRegisterUser_whenValidInput() {
        // Given
        var request = new RegisterRequest("test@example.com", "Password123!");
        when(userRepository.existsByEmail(request.getEmail()))
            .thenReturn(false);
        when(passwordEncoder.encode(request.getPassword()))
            .thenReturn("hashedPassword");
        
        // When
        var result = userService.register(request);
        
        // Then
        assertNotNull(result.getId());
        assertEquals(request.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowException_whenEmailExists() {
        // Given
        var request = new RegisterRequest("test@example.com", "Password123!");
        when(userRepository.existsByEmail(request.getEmail()))
            .thenReturn(true);
        
        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.register(request);
        });
        
        verify(userRepository, never()).save(any());
    }
}
```

### Integration Test Template

```java
@SpringBootTest
@Testcontainers
class UserRegistrationIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:15-alpine"
    );
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldRegisterAndPersistUser() {
        // Given
        var request = new RegisterRequest("test@example.com", "Password123!");
        
        // When
        var result = userService.register(request);
        
        // Then
        var savedUser = userRepository.findById(result.getId());
        assertTrue(savedUser.isPresent());
        assertEquals("test@example.com", savedUser.get().getEmail());
    }
}
```

---

## Logging Standards

**Log Levels**:
- **ERROR**: System errors requiring immediate attention
- **WARN**: Potential issues (e.g., retries, fallbacks)
- **INFO**: Business events (user registered, booking created)
- **DEBUG**: Detailed flow for debugging

**Structured Logging**:
```java
log.info("User registered successfully: userId={}, email={}, role={}", 
    user.getId(), user.getEmail(), user.getRole());

// With MDC for correlation
MDC.put("correlationId", request.getHeader("X-Correlation-ID"));
log.info("Processing booking: scheduleId={}, studentId={}", 
    scheduleId, studentId);
MDC.clear();
```

---

## Performance Standards

**Database Queries**:
- [ ] All queries use indexes
- [ ] Batch inserts used where applicable
- [ ] Pagination for list endpoints (max 100 items)
- [ ] Lazy loading configured appropriately

**API Response Times** (p95):
- Read endpoints: < 200ms
- Write endpoints: < 500ms
- Search endpoints: < 1s

**Connection Pools**:
- Database: 10 max, 5 min idle
- Redis: 8 max, 8 idle
- HTTP clients: 50 max, 20 idle

---

## Summary

✅ **TDD**: Red → Green → Refactor
✅ **Clean Architecture**: Domain independent of infrastructure
✅ **Clean Code**: Readable, maintainable, documented
✅ **Security**: Input validation, authorization, no injection
✅ **Idempotency**: All writes idempotent
✅ **Concurrency**: Optimistic locking, stateless services
✅ **Testability**: Constructor injection, interface segregation

**Remember**: Production quality from day 1, not "we'll fix it later".
