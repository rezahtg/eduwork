# Eduwork API - cURL Commands

Base URL: `http://localhost:8080`

---

## Authentication

### 1. Register User (Student - Success) ✅

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "SecurePass123!",
    "phone": "+6281234567890",
    "role": "STUDENT"
  }'
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "timestamp": "2025-12-21T08:00:00Z",
  "data": {
    "id": "uuid",
    "email": "student@example.com",
    "phone": "+6281234567890",
    "status": "PENDING_VERIFICATION",
    "profileComplete": false,
    "createdAt": "2025-12-21T08:00:00Z"
  },
  "message": "User registered successfully. Please check your email to verify your account."
}
```

---

### 2. Register User (Mentor - Success) ✅

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "mentor@example.com",
    "password": "MentorPass123!",
    "phone": "+6289876543210",
    "role": "MENTOR"
  }'
```

---

### 3. Register User (Without Phone) ✅

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nophone@example.com",
    "password": "SecurePass123!",
    "role": "STUDENT"
  }'
```

**Note:** Phone field is optional

---

### 4. Register User (Email Normalization) ✅

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "Test@EXAMPLE.COM",
    "password": "SecurePass123!",
    "role": "STUDENT"
  }'
```

**Note:** Email will be normalized to lowercase: `test@example.com`

---

### 5. Register User (Duplicate Email - Error) ❌

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "AnotherPass123!",
    "role": "MENTOR"
  }'
```

**Expected Response (409 Conflict):**
```json
{
  "success": false,
  "timestamp": "2025-12-21T08:00:00Z",
  "error": {
    "code": "EMAIL_ALREADY_EXISTS",
    "message": "Email already registered: student@example.com"
  },
  "path": "/api/v1/auth/register"
}
```

---

### 6. Register User (Invalid Email Format - Error) ❌

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "password": "SecurePass123!",
    "role": "STUDENT"
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "timestamp": "2025-12-21T08:00:00Z",
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "email",
        "message": "Email must be valid"
      }
    ]
  },
  "path": "/api/v1/auth/register"
}
```

---

### 7. Register User (Weak Password - Error) ❌

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "weak",
    "role": "STUDENT"
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "timestamp": "2025-12-21T08:00:00Z",
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "password",
        "message": "Password must be at least 8 characters with uppercase, lowercase, digit, and special character"
      }
    ]
  },
  "path": "/api/v1/auth/register"
}
```

---

### 8. Register User (Missing Required Fields - Error) ❌

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "role": "STUDENT"
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "timestamp": "2025-12-21T08:00:00Z",
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "email",
        "message": "Email is required"
      },
      {
        "field": "password",
        "message": "Password is required"
      }
    ]
  },
  "path": "/api/v1/auth/register"
}
```

---

### 9. Register User (Invalid Phone Format - Error) ❌

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePass123!",
    "phone": "invalid-phone",
    "role": "STUDENT"
  }'
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "timestamp": "2025-12-21T08:00:00Z",
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "phone",
        "message": "Phone must be in E.164 format (e.g., +6281234567890)"
      }
    ]
  },
  "path": "/api/v1/auth/register"
}
```

---

## Password Policy Requirements

Your password must meet the following criteria:
- ✅ Minimum 8 characters
- ✅ At least 1 uppercase letter (A-Z)
- ✅ At least 1 lowercase letter (a-z)
- ✅ At least 1 digit (0-9)
- ✅ At least 1 special character (@$!%*?&)

**Valid Examples:**
- `SecurePass123!`
- `MyP@ssw0rd`
- `Test1234!`

**Invalid Examples:**
- `weak` (too short, no uppercase, no digit, no special)
- `password123` (no uppercase, no special)
- `PASSWORD123!` (no lowercase)

---

## Phone Number Format (E.164)

Valid phone numbers must follow E.164 international format:
- Starts with `+` followed by country code
- 1-15 digits total
- No spaces or special characters

**Valid Examples:**
- `+6281234567890` (Indonesia)
- `+14155552671` (USA)
- `+442071234567` (UK)

**Invalid Examples:**
- `081234567890` (missing country code)
- `+62 812 3456 7890` (contains spaces)
- `invalid-phone` (not a number)

---

## Next Features - Add Here

### Email Verification
*(To be added)*

### Login
*(To be added)*

### Password Reset
*(To be added)*
