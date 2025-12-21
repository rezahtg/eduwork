# API Testing Documentation

This directory contains API testing resources for the Eduwork platform.

## 📁 Files

### 1. `Eduwork.postman_collection.json`
Complete Postman collection with all API endpoints organized by feature.

**How to Use:**
1. Open Postman
2. Click **Import** → **File**
3. Select `Eduwork.postman_collection.json`
4. Collection will be imported with all requests ready to use
5. Update the `base_url` variable if needed (default: `http://localhost:8080`)

### 2. `curl-commands.md`
Comprehensive cURL commands for all endpoints with:
- Request examples
- Expected responses (success & error cases)
- Validation rules
- Response format documentation

**How to Use:**
1. Copy the cURL command
2. Run in terminal/command prompt
3. Compare actual response with expected response

---

## 🚀 Quick Start

### Prerequisites
- Backend server running on `http://localhost:8080`
- Valid database connection

### Testing with Postman
```bash
# 1. Import collection
# 2. Set environment variable base_url = http://localhost:8080
# 3. Run requests from "Authentication" folder
```

### Testing with cURL
```bash
# Success case
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"SecurePass123!","role":"STUDENT"}'

# Error case - duplicate email
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"AnotherPass123!","role":"STUDENT"}'
```

---

## 📊 Test Coverage

### Current Features
- ✅ **User Registration** - 9 test scenarios
  - Success cases (with/without phone, email normalization)
  - Validation errors (email, password, phone formats)
  - Business rule errors (duplicate email)

### Upcoming Features
- 🔲 Email Verification
- 🔲 Login/Logout
- 🔲 Password Reset
- 🔲 User Profile
- 🔲 Schedule Management
- 🔲 Booking Management

*(New features will be added to this collection incrementally)*

---

## 📖 API Response Format

All API responses follow a standardized format:

### Success Response
```json
{
  "success": true,
  "timestamp": "2025-12-21T08:00:00Z",
  "data": { /* response data */ },
  "message": "Operation successful message"
}
```

### Error Response
```json
{
  "success": false,
  "timestamp": "2025-12-21T08:00:00Z",
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": [ /* validation errors if applicable */ ]
  },
  "path": "/api/endpoint/path"
}
```

---

## 🔍 Common Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `EMAIL_ALREADY_EXISTS` | 409 | Email already registered |
| `INVALID_EMAIL` | 400 | Email format invalid |
| `INVALID_PASSWORD` | 400 | Password doesn't meet policy |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected server error |

---

## 💡 Tips

1. **Run tests in order** - Some scenarios depend on previous ones (e.g., duplicate email test requires successful registration first)

2. **Reset database** - For clean testing, reset the database between test runs:
   ```bash
   # Stop server, drop database, restart server
   ```

3. **Check logs** - Backend logs show detailed validation errors and stack traces

4. **Use Postman Tests** - Add automated assertions to Postman requests:
   ```javascript
   pm.test("Status code is 201", function () {
       pm.response.to.have.status(201);
   });
   
   pm.test("Response has success flag", function () {
       pm.expect(pm.response.json().success).to.be.true;
   });
   ```

---

## 🤝 Contributing

When adding new features:
1. Add requests to Postman collection
2. Add cURL commands to `curl-commands.md`
3. Document expected responses
4. Update this README with new test coverage

---

**Last Updated:** 2025-12-21  
**API Version:** 1.0.0
