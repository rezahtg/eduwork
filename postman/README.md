# Eduwork Platform - Postman API Testing Guide

## 📦 Quick Start

### 1. Import Files into Postman

1. Open Postman
2. Click **Import** button
3. Import both files:
   - `Eduwork-API.postman_collection.json` - The API collection
   - `Eduwork-Local.postman_environment.json` - Local environment variables
4. Select **Eduwork - Local** environment from the dropdown (top right)

### 2. Start Backend Services

```bash
# Start Docker services (PostgreSQL, Redis, MailHog)
cd eduwork
docker-compose up -d

# Start Spring Boot application
cd backend
mvn spring-boot:run
```

### 3. Verify Setup

- **Backend**: http://localhost:8080/api/v1
- **MailHog** (Email): http://localhost:8025
- **Swagger UI**: http://localhost:8080/swagger-ui.html

---

## 🎯 Testing Workflows

### Basic Registration & Login Flow

Run requests in this order:

1. **Register Student** → Creates new student account
2. **Verify Email** → Get token from MailHog, set `verificationToken` variable
3. **Login** → Tokens automatically saved to environment
4. **Complete Student Profile** → Uses saved access token

### Complete E2E Test Flow

```
1. Authentication Flow:
   ├─ Register Student
   ├─ Register Mentor
   ├─ Verify Email (check MailHog)
   ├─ Login (tokens auto-saved)
   └─ Logout

2. Password Reset Flow:
   ├─ Request Password Reset
   ├─ Reset Password (get token from MailHog)
   └─ Login with new password

3. Profile Management:
   ├─ Complete Student Profile
   └─ Complete Mentor Profile

4. Brute Force Protection Test:
   ├─ Failed Login Attempt 1-4
   └─ Failed Login Attempt 5 (Account Locked - 403)
```

---

## 🔐 Automatic Token Management

### How It Works

1. **Login Request** automatically:
   - Validates response (200 status, success flag)
   - Extracts `accessToken` and `refreshToken`
   - Saves tokens to environment variables
   - Logs success message to console

2. **Protected Requests** (Profile endpoints):
   - **Pre-request Script** auto-injects: `Authorization: Bearer {{accessToken}}`
   - No manual header configuration needed

3. **Logout Request**:
   - Clears tokens from environment
   - Ready for next login

### Manual Token Management

If you need to manually set tokens:

```javascript
// In Pre-request Script or Tests
pm.environment.set('accessToken', 'your-token-here');
pm.environment.set('refreshToken', 'your-refresh-token');
```

---

## 📧 Email Verification Tokens

### Getting Tokens from MailHog

1. Open MailHog: http://localhost:8025
2. Find the verification/reset email
3. Copy the token from email body or URL
4. Set environment variable:
   - For email verification: Set `verificationToken`
   - For password reset: Set `resetToken`

### Alternative: Check Backend Logs

Tokens are also printed in Spring Boot console:

```
Verification token: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

---

## ✅ Test Scripts Explained

Every request includes test scripts that validate responses:

### Example: Login Request Tests

```javascript
// Validate status code
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// Validate response structure
pm.test("Login successful", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData.success).to.be.true;
    pm.expect(jsonData.data).to.have.property('accessToken');
});

// Extract and save tokens
if (pm.response.code === 200) {
    const jsonData = pm.response.json();
    pm.environment.set('accessToken', jsonData.data.accessToken);
    console.log('✅ Login successful - Tokens saved');
}
```

### View Test Results

- After running a request, click **Test Results** tab
- All tests should show green checkmarks ✅
- Check console for debugging logs

---

## 🧪 Running Test Collections

### Run Entire Collection

1. Click **Collections** → **Eduwork Platform API**
2. Click **Run** button
3. Select requests to run
4. Click **Run Eduwork Platform API**

**Note**: Some requests require manual setup (like email verification tokens)

### Run Specific Folders

Test individual features:

1. **1. Authentication** - Full auth flow
2. **2. Password Reset** - Reset password flow
3. **3. Profile Management** - Profile completion
4. **4. Brute Force Protection Tests** - Security tests

---

## 🛡️ Brute Force Protection Testing

### Test Scenario

The collection includes 5 sequential failed login attempts to test account locking:

1. **Attempts 1-4**: Should return `401 Unauthorized`
2. **Attempt 5**: Should return `403 Forbidden` with `ACCOUNT_LOCKED` error

### Expected Behavior

After 5 failed attempts:

```json
{
  "success": false,
  "error": {
    "code": "ACCOUNT_LOCKED",
    "message": "Account is temporarily locked. Try again in 30 minutes."
  }
}
```

### Account Lock Details

- **Max Attempts**: 5
- **Lock Duration**: 30 minutes
- **Auto-unlock**: After 30 minutes
- **IP Rate Limit**: 10 attempts per 15 minutes

### Reset Locked Account

To reset for testing:

1. Wait 30 minutes (auto-unlock)
2. Or manually unlock in database:
   ```sql
   UPDATE users SET locked_until = NULL WHERE email = 'student@example.com';
   ```
3. Or use Redis CLI:
   ```bash
   docker exec -it eduwork-redis redis-cli
   DEL login:locked:student@example.com
   DEL login:attempts:account:student@example.com
   ```

---

## 🔧 Environment Variables Reference

| Variable | Description | Auto-Set |
|----------|-------------|----------|
| `baseUrl` | API base URL | Manual |
| `accessToken` | JWT access token | Auto (Login) |
| `refreshToken` | JWT refresh token | Auto (Login) |
| `userId` | Current user ID | Auto (Register) |
| `userEmail` | Current user email | Auto (Register) |
| `verificationToken` | Email verification token | Manual (from MailHog) |
| `resetToken` | Password reset token | Manual (from MailHog) |
| `testStudentEmail` | Test student email | Manual |
| `testMentorEmail` | Test mentor email | Manual |
| `testPassword` | Test account password | Manual |

---

## 🚀 Advanced Usage

### Custom Pre-request Scripts

Add to collection/folder level for all requests:

```javascript
// Auto-inject token for all protected endpoints
const token = pm.environment.get('accessToken');
if (token && !pm.request.headers.has('Authorization')) {
    pm.request.headers.add({
        key: 'Authorization',
        value: 'Bearer ' + token
    });
}
```

### Custom Test Scripts

Global assertions for all requests:

```javascript
// Validate response time
pm.test("Response time is less than 2000ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(2000);
});

// Validate JSON structure
pm.test("Response is valid JSON", function () {
    pm.response.to.be.json;
});
```

### Console Debugging

View detailed logs in **Postman Console** (bottom left icon):

```javascript
console.log('Request URL:', pm.request.url.toString());
console.log('Request Body:', pm.request.body);
console.log('Response:', pm.response.json());
```

---

## 📊 Response Format

All API responses follow this standardized format:

### Success Response

```json
{
  "success": true,
  "timestamp": "2026-01-22T15:00:00Z",
  "data": { /* response data */ },
  "message": "Operation successful"
}
```

### Error Response

```json
{
  "success": false,
  "timestamp": "2026-01-22T15:00:00Z",
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": [ /* validation errors if applicable */ ]
  },
  "path": "/api/v1/auth/register"
}
```

---

## 🐛 Troubleshooting

### Issue: "No response" or connection refused

**Solution**:
```bash
# Verify backend is running
curl http://localhost:8080/api/v1/actuator/health

# Check Docker services
docker-compose ps

# Restart if needed
docker-compose restart
mvn spring-boot:run
```

### Issue: 401 Unauthorized on protected endpoints

**Solution**:
1. Check if `accessToken` is set in environment
2. Run **Login** request to refresh token
3. Verify token in Authorization header

### Issue: Email verification token not working

**Solution**:
1. Check MailHog: http://localhost:8025
2. Verify `verificationToken` variable is set correctly
3. Token expires in 15 minutes - request new one if expired

### Issue: Account locked unexpectedly

**Solution**:
```bash
# Clear Redis lock
docker exec -it eduwork-redis redis-cli DEL "login:locked:your-email@example.com"

# Or wait 30 minutes for auto-unlock
```

---

## 📝 Best Practices

1. **Use Environment Variables**: Never hardcode tokens or emails in requests
2. **Check Console Logs**: Review test outputs and debug logs
3. **Run Tests Sequentially**: Some requests depend on previous ones (e.g., Login before Profile)
4. **Clear Sensitive Data**: Clear tokens before sharing collection
5. **Update Environment**: Keep email addresses and passwords current

---

## 🎓 Learning Resources

- **Postman Documentation**: https://learning.postman.com/
- **Postman Test Scripts**: https://learning.postman.com/docs/writing-scripts/test-scripts/
- **Environment Variables**: https://learning.postman.com/docs/sending-requests/variables/

---

**Happy Testing! 🚀**

For issues or questions, check the API documentation at http://localhost:8080/swagger-ui.html
