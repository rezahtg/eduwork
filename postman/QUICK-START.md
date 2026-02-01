# Quick Setup Guide

## 📦 Import into Postman

1. **Import Environments** (3 files):
   - `Eduwork-Student.postman_environment.json`
   - `Eduwork-Mentor.postman_environment.json`
   - `Eduwork-Public.postman_environment.json`

2. **Import Collection** (1 file):
   - `Eduwork-Platform.postman_collection.json`

## ⚡ Quick Test (2 minutes)

### Test as Mentor
1. Select **Eduwork - Mentor** environment (top-right dropdown)
2. Run: `🔐 Authentication > Register` (creates mentor@example.com)
3. Check MailHog: http://localhost:8025
4. Copy verification token → Set in environment variable
5. Run: `🔐 Authentication > Verify Email`
6. Run: `📅 Schedule Module > Create Schedule`
   - ✅ **Auto-logs in automatically!**
7. Run: `📅 Schedule Module > Publish Schedule`
   - ✅ **Uses saved scheduleId automatically!**

### Test as Student (Different Window)
1. **Switch environment** to **Eduwork - Student**
2. Run: `🔐 Authentication > Register`
3. Verify email (MailHog)
4. Run: `🔍 Schedule Discovery > Search Schedules`
   - ✅ **Auto-logs in as student!**
   - ✅ **Sees published schedules from mentor!**

### Test Public Access
1. **Switch environment** to **Eduwork - Public**
2. Run: `🔍 Schedule Discovery > Search Schedules`
   - ✅ **No authentication - works immediately!**

## 🎯 Key Benefits

| Before | After |
|--------|-------|
| Manually login as student | Switch to Student environment |
| Copy/paste access token | Auto-injected |
| Logout | Just switch environment |
| Manually login as mentor | Switch to Mentor environment |
| Copy/paste schedule IDs | Auto-captured |

## 🔄 Typical Workflow

```bash
# Morning: Test mentor features
Select "Eduwork - Mentor"
→ All requests auto-authenticate as mentor
→ Create schedules, publish, etc.

# Afternoon: Test student features  
Select "Eduwork - Student"
→ All requests auto-authenticate as student
→ Search schedules, book sessions, etc.

# Evening: Test public access
Select "Eduwork - Public"
→ No authentication
→ Test public endpoints
```

## 📝 Environment Variables Reference

### Student & Mentor Environments
- `baseUrl`: http://localhost:8080/api/v1
- `userRole`: STUDENT or MENTOR
- `userEmail`: Auto-filled (student@example.com or mentor@example.com)
- `userPassword`: Auto-filled (SecurePass123!)
- `accessToken`: Auto-managed ✨
- `refreshToken`: Auto-managed ✨
- `userId`: Auto-captured from registration
- `scheduleId`: Auto-captured from schedule creation
- `sessionId`: Auto-captured from login

### Public Environment
- `baseUrl`: http://localhost:8080/api/v1
- `userRole`: GUEST
- (No auth variables)

## 🆕 Adding New Features

When you add a new module (e.g., Booking Module):

1. **Create folder** in collection:
   ```
   📁 Booking Module (STUDENT)
      └── Create Booking
      └── Cancel Booking
   ```

2. **Add requests** - auth is automatic!

3. **Add tests** to capture IDs:
   ```javascript
   pm.environment.set('bookingId', jsonData.id);
   ```

4. **No changes to environments needed!**

## ⚠️ Important Notes

- **First time setup**: Run Register → Verify Email once per environment
- **MailHog**: Get verification tokens from http://localhost:8025
- **Token refresh**: Automatic (built into collection)
- **Parallel testing**: Open multiple Postman instances with different environments

## 🎉 You're Ready!

Just **switch environments** and start testing - no more manual login/logout! 🚀
