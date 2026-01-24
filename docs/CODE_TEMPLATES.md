# Eduwork - Code Templates & Patterns

## Clean Code Guidelines

**Core Principles**:
1. **Clean**: Self-documenting, clear naming
2. **Reusable**: DRY principle, extract common logic  
3. **Simple**: Avoid clever code, prefer obvious
4. **Performance-Aware**: Use caching where appropriate
5. **Tested**: Unit tests for business logic

---

## Repository Layer Template

```java
package com.eduwork.[module].infrastructure.persistence;

import com.eduwork.[module].domain.model.DomainModel;
import com.eduwork.[module].domain.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing DomainRepository using JPA.
 * Includes automatic caching for performance.
 */
@Component
@RequiredArgsConstructor
public class RepositoryAdapter implements DomainRepository {

    private final JpaRepository jpaRepository;

    @Override
    @Cacheable(value = "cacheName", key = "#id")
    public Optional<DomainModel> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "cacheName", key = "#model.id"),
        @CacheEvict(value = "cacheName", key = "'email:' + #model.email")
    })
    public DomainModel save(DomainModel model) {
        Entity entity = toEntity(model);
        Entity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    // Mapper methods...
}
```

**When to Cache**:
- ✅ User lookups (by ID, email)
- ✅ Profile data
- ✅ Reference data (rarely changes)
- ❌ Real-time data (bookings, payments)
- ❌ Frequently changing data

---

## Use Case (Service) Layer Template

```java
package com.eduwork.[module].application.usecase;

import com.eduwork.[module].domain.model.DomainModel;
import com.eduwork.[module].domain.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: [Description of business operation]
 * 
 * Performance: Uses cached repository, expected < 50ms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SomeUseCase {

    private final DomainRepository repository;
    // Inject other dependencies

    @Transactional
    public ResultDTO execute(CommandDTO command) {
        // 1. Validate input
        validateCommand(command);
        
        // 2. Load domain objects (cached automatically)
        DomainModel model = repository.findById(command.getId())
                .orElseThrow(() -> new NotFoundException("Not found"));
        
        // 3. Execute business logic
        model.performBusinessOperation(command.getData());
        
        // 4. Save changes (cache invalidated automatically)
        DomainModel saved = repository.save(model);
        
        // 5. Return result
        return toDTO(saved);
    }

    private void validateCommand(CommandDTO command) {
        // Simple, obvious validation
        if (command.getId() == null) {
            throw new ValidationException("ID is required");
        }
    }
}
```

**Best Practices**:
- ✅ One public method per use case
- ✅ Clear method names (execute, handle, process)
- ✅ Validate early, fail fast
- ✅ Use @Transactional for write operations
- ✅ Log important business events
- ❌ Don't put complex logic in use cases (move to domain)

---

## Controller (Presentation) Layer Template

```java
package com.eduwork.[module].presentation.controller;

import com.eduwork.common.dto.ApiResponse;
import com.eduwork.[module].application.usecase.SomeUseCase;
import com.eduwork.[module].presentation.dto.RequestDTO;
import com.eduwork.[module].presentation.dto.ResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for [module] operations.
 * Performance: Monitored by PerformanceLoggingFilter
 */
@Slf4j
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final SomeUseCase useCase;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResponseDTO>> getResource(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId) {
        
        ResponseDTO response = useCase.execute(new GetCommand(id, userId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResponseDTO>> createResource(
            @Valid @RequestBody RequestDTO request,
            @AuthenticationPrincipal String userId) {
        
        ResponseDTO response = useCase.execute(toCommand(request, userId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Resource created"));
    }
}
```

**Controller Guidelines**:
- ✅ Thin controllers - delegate to use cases
- ✅ Use @Valid for request validation
- ✅ Return standardized ApiResponse
- ✅ Extract userId from @AuthenticationPrincipal
- ❌ No business logic in controllers
- ❌ No direct repository access

---

## Async Operations Template

```java
package com.eduwork.[module].infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Async service for non-critical background operations.
 */
@Slf  4j
@Service
@RequiredArgsConstructor
public class AsyncService {

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void performAsyncOperation(DataDTO data) {
        try {
            // Long-running operation (email, file upload, etc.)
            log.info("Starting async operation for: {}", data.getId());
            
            // Do work...
            
            log.info("Completed async operation for: {}", data.getId());
        } catch (Exception e) {
            log.error("Async operation failed for: {}", data.getId(), e);
            // Don't throw - this shouldn't fail parent transaction
        }
    }
}
```

**When to Use @Async**:
- ✅ Email sending
- ✅ File uploads to S3
- ✅ Notification dispatch
- ✅ Audit logging
- ✅ Analytics events
- ❌ Critical business logic
- ❌ Operations needing immediate feedback

---

## Code Review Checklist

Before committing code, verify:

- [ ] **Is it obvious?** Can a junior dev understand it?
- [ ] **Is it simple?** No unnecessary complexity?
- [ ] **Is it reusable?** Can logic be extracted?
- [ ] **Is it performant?** Caching where appropriate?
- [ ] **Is it tested?** Unit tests for business logic?
- [ ] **Is logging appropriate?** Info for important events, debug for details?
- [ ] **Are exceptions handled?** Clear error messages?
- [ ] **Is validation present?** Fail fast on invalid input?

---

## Anti-Patterns to Avoid

### ❌ BAD: Complex, Clever Code
```java
public User getUser(UUID id) {
    return Optional.ofNullable(id)
        .map(i -> repo.findById(i))
        .orElseGet(() -> Optional.empty())
        .filter(u -> u.getStatus() != null)
        .orElseThrow(() -> new RuntimeException("error"));
}
```

### ✅ GOOD: Simple, Clear Code
```java
public User getUser(UUID id) {
    if (id == null) {
        throw new ValidationException("User ID is required");
    }
    return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
}
```

---

### ❌ BAD: No Caching
```java
public User getUser(UUID id) {
    return userRepository.findById(id).orElseThrow();
    // Always hits database - slow
}
```

### ✅ GOOD: With Caching
```java
@Cacheable("users")
public User getUser(UUID id) {
    return userRepository.findById(id).orElseThrow();
    // Cached automatically - fast
}
```

---

### ❌ BAD: Blocking Email
```java
public void register(User user) {
    userRepository.save(user);
    emailService.sendWelcome(user);  // Blocks for 300ms
}
```

### ✅ GOOD: Async Email
```java
public void register(User user) {
    userRepository.save(user);
    emailService.sendWelcomeAsync(user);  // Non-blocking
}

@Async
public void sendWelcomeAsync(User user) {
    // Runs in separate thread
}
```

---

## Performance Monitoring

Every request is automatically monitored by `PerformanceLoggingFilter`:

```
// Good performance
✅ GET /api/v1/users/me - 45ms

// Warning - approaching limit
📊 POST /api/v1/auth/login - 150ms (status: 200)

// Requires optimization
⚠️  SLOW REQUEST: GET /api/v1/bookings - 650ms (status: 200) - EXCEEDS p95 TARGET
```

**When you see slow requests**:
1. Check if caching can help
2. Review database queries (use EXPLAIN)
3. Consider async processing
4. Add composite indexes

---

## Quick Reference

| Pattern | Use When | Don't Use When |
|---------|----------|----------------|
| `@Cacheable` | Frequent reads, rare writes | Real-time data |
| `@Async` | Non-critical background work | Critical operations |
| `@Transactional` | Write operations | Read-only queries |
| Composite indexes | Frequent multi-column queries | Single column lookups |

**Performance Targets**:
- p50 < 100ms
- p95 < 500ms  
- p99 < 1s
- DB queries < 50ms

Follow these patterns, and performance will be built-in from day one! 🚀
