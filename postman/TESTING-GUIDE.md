# Eduwork Platform - Postman Testing Guide

## 📦 Environment Structure

We have **3 separate environments** for seamless testing without manual login/logout:

| Environment | Purpose | Auto-Auth | Use Case |
|------------|---------|-----------|----------|
| **Eduwork - Student** | Student user testing | ✅ Yes | Profile completion, booking sessions, viewing schedules |
| **Eduwork - Mentor** | Mentor user testing | ✅ Yes | Creating schedules, managing bookings, KYC |
| **Eduwork - Public** | No authentication | ❌ No | Schedule search, public endpoints |

### Environment Variables

Each environment has these variables:

**Student & Mentor:**
- `baseUrl`: API base URL
- `userRole`: STUDENT or MENTOR
- `userEmail`: Auto-filled email
- `userPassword`: Auto-filled password  
- `accessToken`: JWT access token (auto-managed)
- `refreshToken`: JWT refresh token (auto-managed)
- `userId`, `sessionId`, `scheduleId`: Auto-captured IDs

**Public:**
- `baseUrl`: API base URL only
- No authentication variables

## 🚀 Quick Start

### 1. Import All Files

Import these 4 files into Postman:
1. `Eduwork-Student.postman_environment.json`
2. `Eduwork-Mentor.postman_environment.json`
3. `Eduwork-Public.postman_environment.json`
4. `Eduwork-Platform.postman_collection.json`

### 2. Select Environment

Click the environment dropdown (top right) and select:
- **Student** for student workflows
- **Mentor** for mentor workflows  
- **Public** for unauthenticated testing

### 3. Run Requests

**That's it!** Authentication is automatic:
- First request in a folder auto-logs in
- Token automatically injected in all requests
- Auto-refresh on token expiry
- No manual login/logout needed

## 📁 Collection Structure

```
Eduwork Platform API/
├── 🔐 Authentication & Registration
│   ├── Register (Auto-detects role from environment)
│   ├── Login (Auto-credentials from environment)
│   └── Email Verification, Password Reset
│
├── 👤 Profile Management
│   ├── Student Profile (Only in Student env)
│   └── Mentor Profile (Only in Mentor env)
│
├── 📅 Schedule Module (MENTOR)
│   ├── Create Schedule
│   ├── Update Schedule
│   ├── Publish/Cancel
│   └── Get My Schedules
│
├── 🔍 Schedule Discovery (PUBLIC)
│   ├── Search Schedules (No auth required)
│   └── Get Schedule Details
│
└── 🧪 Quality Tests
    └── Constraint validation tests
```

## 🤖 Automation Features

### Auto-Login
Every **authenticated request** automatically:
1. Checks if `accessToken` exists
2. If not, logs in using `userEmail` and `userPassword`
3. Saves tokens to environment
4. Proceeds with the original request

### Auto-Token Injection
All requests automatically add:
```
Authorization: Bearer {{accessToken}}
X-Session-Id: {{sessionId}}
```

### Auto-Refresh (TODO)
On 401 errors, automatically:
1. Uses `refreshToken` to get new tokens
2. Retries the original request

### Auto-Variable Capture
Responses automatically save:
- `userId` from registration/login
- `scheduleId` from schedule creation
- `sessionId` from login
- `verificationToken`, `resetToken` from emails

## 📖 Common Workflows

### Testing Student Flow
1. Select **Eduwork - Student** environment
2. Run "Register" (creates/uses student@example.com)
3. Run "Verify Email" (get token from MailHog)
4. Run "Complete Student Profile"
5. Run "Search Schedules" from Public folder
6. ✅ All requests auto-authenticated!

### Testing Mentor Flow
1. Select **Eduwork - Mentor** environment
2. Run "Register" (creates/uses mentor@example.com)
3. Run "Verify Email"
4. Run "Complete Mentor Profile"
5. Run "Create Schedule"
6. Run "Publish Schedule"
7. ✅ All requests auto-authenticated!

### Switch Between Users
Just **change the environment**:
- Switch to Student → next request uses student token
- Switch to Mentor → next request uses mentor token
- Switch to Public → no authentication

**No manual login/logout needed!**

## 🧪 Running Tests

### Run Entire Collection
1. Click "Eduwork Platform API" collection
2. Click "Run" button
3. Select environment
4. Click "Run Eduwork Platform API"
5. ✅ All tests execute automatically!

### Run Single Folder
1. Right-click any folder (e.g., "Schedule Module")
2. Select "Run folder"
3. ✅ Auto-login happens once, all requests execute!

### Run Single Request
Just click "Send" - authentication is automatic!

## 🎯 Best Practices

### For New Features
When adding new endpoints:

1. **Add to correct folder**:
   - Mentor-only → Schedule Module (MENTOR)
   - Student-only → New "Booking Module (STUDENT)"
   - Public → Discovery folders
   - Auth required → Any authenticated folder

2. **Add tests**:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// Auto-save IDs for chaining
if (pm.response.code === 200) {
    const data = pm.response.json();
    pm.environment.set('someId', data.id);
}
```

3. **Don't add auth headers manually** - they're auto-injected!

### Testing Multiple Scenarios
**Before (manual):**
```
1. Login as student
2. Test student endpoints
3. Logout
4. Login as mentor
5. Test mentor endpoints
6. Logout
```

**Now (automatic):**
```
1. Select Student env → test
2. Select Mentor env → test
3. Select Public env → test
```

## 🔧 Troubleshooting

### "401 Unauthorized" errors
- Check if environment has `userEmail` and `userPassword`
- Ensure user is registered and verified
- Check if accessToken expired (should auto-refresh)

### "User not found" during auto-login
- Run "Register" request first in that environment
- Run "Verify Email" after registration

### Variables not saving
- Check "Tests" tab has environment.set() calls
- Ensure environment is selected (not "No Environment")

### Token not injecting
- Check "Pre-request Script" tab exists
- Verify you're using the new collection (has auto-auth)

## 📝 Environment Setup Checklist

Before first use:

**Student Environment:**
- [ ] Import `Eduwork-Student.postman_environment.json`
- [ ] Select it in Postman
- [ ] Run "Register" request
- [ ] Get verification token from MailHog (http://localhost:8025)
- [ ] Run "Verify Email" request
- [ ] ✅ Ready! All other requests auto-login

**Mentor Environment:**
- [ ] Import `Eduwork-Mentor.postman_environment.json`
- [ ] Select it in Postman
- [ ] Run "Register" request  
- [ ] Get verification token from MailHog
- [ ] Run "Verify Email" request
- [ ] ✅ Ready! All other requests auto-login

**Public Environment:**
- [ ] Import `Eduwork-Public.postman_environment.json`
- [ ] ✅ Ready immediately (no auth needed)

## 🎉 Benefits

✅ **No manual login/logout** - switch environments instead
✅ **Test multiple users simultaneously** - use different environments  
✅ **Automatic token management** - never manually copy tokens
✅ **Cleaner request bodies** - no hardcoded emails/passwords
✅ **Easier collaboration** - team uses same environments
✅ **Future-proof** - new features follow same pattern

## 🆕 Adding New Modules

When implementing new modules (e.g., Booking Module):

1. **Create folder** in collection:
```
📁 Booking Module (STUDENT)
   └── Create Booking
   └── View My Bookings
   └── Cancel Booking
```

2. **Requests auto-inherit** auth from collection
3. **Add tests** to capture IDs:
```javascript
pm.environment.set('bookingId', jsonData.id);
```

4. **No changes needed** to environments!

---

**Last Updated:** 2026-01-25
**Version:** 2.0 (Multi-Environment)
