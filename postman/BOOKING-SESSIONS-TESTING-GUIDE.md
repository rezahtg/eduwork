# Postman Testing Guide - Booking & Sessions

## 📦 Collection Overview

The **Booking-Sessions.postman_collection.json** contains comprehensive tests for the new Schedule & Booking Module.

**Total Requests**: 11
- 4 Session Discovery (public)
- 4 Booking Flow (authenticated)
- 3 Test Scenarios (edge cases)

---

## 🚀 Quick Start

### 1. Import Collection

```bash
File → Import → Select: Booking-Sessions.postman_collection.json
```

### 2. Set Environment Variables

Before testing, set these collection variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `baseUrl` | API base URL | `http://localhost:8080/api/v1` |
| `studentAccessToken` | JWT from login | Get from Identity-Module collection |
| `sessionId` | Session to book | Get from "Search Sessions" request |
| `bookingId` | Created booking | Auto-saved after "Book Session" |

### 3. Login First

Use the **Identity-Module** collection to login as a student:

```
POST /auth/login
{
  "email": "student@eduwork.com",
  "password": "password"
}

→ Copy accessToken
→ Set {{studentAccessToken}} variable
```

---

## 📋 Test Flow

### Flow 1: Happy Path (Complete Booking)

**Step 1: Search Sessions**
```http
GET /sessions?sessionType=GROUP&minPrice=50000&maxPrice=200000
```
- Copy a `sessionId` from response
- Set {{sessionId}} variable

**Step 2: Get Session Details**
```http
GET /sessions/{{sessionId}}
```
- Verify session has available slots
- Check price and capacity

**Step 3: Book Session**
```http
POST /bookings
Authorization: Bearer {{studentAccessToken}}
{
  "sessionId": "{{sessionId}}"
}
```
- Response contains `bookingId` (auto-saved to variable)
- Note the payment deadline (24 hours from now)

**Step 4: Confirm Payment**
```http
PUT /bookings/{{bookingId}}/confirm-payment
{
  "paymentReference": "PAY-12345"
}
```
- Booking status → PAID
- If GROUP session and minimum reached → CONFIRMED

**Step 5: View My Bookings**
```http
GET /bookings/my
```
- See all your bookings with financial details

---

### Flow 2: Cancellation with Refund

**Prerequisites**: Complete Flow 1 first

**Step 1: Cancel Booking**
```http
DELETE /bookings/{{bookingId}}
```

**Expected Response**:
```json
"Booking cancelled. Refund of IDR 89,250.00 will be processed."
```

**Refund Calculation Example**:
- Original payment: 150,000 IDR
- Platform fee (15%, non-refundable): 22,500 IDR
- Refundable base: 127,500 IDR
- Cancelled 5 days before (70% tier): 89,250 IDR refund

---

## 🧪 Edge Case Testing

### Test 1: Concurrent Booking (Optimistic Locking)

**Goal**: Verify race condition prevention on last slot

**Setup**:
1. Find a session with **1 slot remaining**:
   ```http
   GET /sessions
   → Find session where: currentEnrollment = maxStudents - 1
   ```

2. Set {{sessionId}} to that session

**Test**:
1. Open "Concurrent Booking Test" request in **2 Postman tabs**
2. Click "Send" in both tabs **simultaneously** (within 100ms)

**Expected Result**:
- Tab 1: ✅ `201 Created` - Booking successful
- Tab 2: ❌ `409 Conflict` or `400 Bad Request`
  - Error: "Session is full" or "OptimisticLockException"

**Why it works**:
- Both tabs read Session (version=5, enrollment=9/10)
- Tab 1 saves first → version becomes 6 ✅
- Tab 2 tries to save → version mismatch ❌ Exception

---

### Test 2: Duplicate Booking Prevention

**Goal**: Verify student can't book same session twice

**Test**:
1. Book a session (first time)
   ```http
   POST /bookings
   { "sessionId": "abc-123" }
   → Should succeed
   ```

2. Book **same session** again
   ```http
   POST /bookings
   { "sessionId": "abc-123" }
   → Should fail
   ```

**Expected Error**:
```json
{
  "error": "DuplicateBookingException",
  "message": "Student already has a booking for this session"
}
```

---

### Test 3: Payment Deadline Enforcement

**Goal**: Verify payment can't be confirmed after 24 hours

**Option A - Wait 24 Hours** (not practical):
1. Book a session
2. Wait 24+ hours
3. Try to confirm payment → Should fail

**Option B - Manual Database Edit** (for testing):
1. Book a session
2. Update database:
   ```sql
   UPDATE bookings 
   SET payment_deadline = NOW() - INTERVAL '1 hour'
   WHERE id = 'your-booking-id';
   ```
3. Try to confirm payment

**Expected Error**:
```json
{
  "error": "PaymentDeadlineExpiredException",
  "message": "Payment deadline has expired: 2026-01-24T14:00:00"
}
```

**Option C - Code Change** (temporary for testing):
```java
// In Session.java, line ~142
private LocalDateTime calculatePaymentDeadline() {
    return LocalDateTime.now().plusMinutes(1);  // Change to 1 minute
}
```

---

## 📊 Response Examples

### Search Sessions Response
```json
[
  {
    "id": "uuid",
    "scheduleId": "uuid",
    "startTime": "2026-02-01T10:00:00",
    "endTime": "2026-02-01T12:00:00",
    "sessionType": "GROUP",
    "pricePerStudent": 150000,
    "currency": "IDR",
    "minStudents": 3,
    "maxStudents": 10,
    "status": "OPEN",
    "currentEnrollment": 5,
    "availableSlots": 5,
    "createdAt": "2026-01-25T10:00:00"
  }
]
```

### Book Session Response
```json
{
  "bookingId": "uuid",
  "message": "Session booked successfully. Please complete payment within 24 hours."
}
```

### Get My Bookings Response
```json
[
  {
    "id": "uuid",
    "sessionId": "uuid",
    "studentId": "uuid",
    "amountPaid": 150000,
    "platformFee": 22500,
    "mentorPayout": 127500,
    "currency": "IDR",
    "status": "PAID",
    "paymentDeadline": "2026-01-26T14:00:00",
    "paymentReference": "PAY-12345",
    "refundAmount": null,
    "bookedAt": "2026-01-25T14:00:00",
    "paidAt": "2026-01-25T14:05:00",
    "cancelledAt": null
  }
]
```

---

## ✅ Testing Checklist

### Basic Functionality
- [ ] Search all sessions (public, no auth required)
- [ ] Filter sessions by type (ONE_ON_ONE, GROUP)
- [ ] Filter sessions by price range
- [ ] Get specific session details
- [ ] Book a session (requires auth)
- [ ] Confirm payment
- [ ] View my bookings
- [ ] Cancel booking

### Business Rules
- [ ] Platform fee is 15% of payment
- [ ] Mentor payout is 85% of payment
- [ ] Payment deadline is 24 hours from booking
- [ ] Refund calculation matches policy
- [ ] GROUP session needs minimum students

### Edge Cases
- [ ] Concurrent booking on last slot (one fails)
- [ ] Duplicate booking prevented
- [ ] Payment confirmation after deadline fails
- [ ] Booking full session fails
- [ ] Cancel non-existent booking fails

### Performance
- [ ] Search sessions returns within 500ms
- [ ] Booking creation within 1s
- [ ] Concurrent requests don't cause overbooking

---

## 🔧 Troubleshooting

### Issue: "Unauthorized" on all requests

**Solution**: Login first and copy JWT token
```bash
1. Use Identity-Module collection
2. POST /auth/login
3. Copy accessToken from response
4. Set {{studentAccessToken}} variable
```

---

### Issue: "Session not found"

**Solution**: Use valid session ID
```bash
1. Run "Search Sessions" first
2. Copy an ID from response
3. Set {{sessionId}} variable
```

---

### Issue: "Session is full"

**Solution**: Find session with available slots
```bash
GET /sessions
→ Look for: availableSlots > 0
→ Or: currentEnrollment < maxStudents
```

---

### Issue: Port 8080 not responding

**Solution**: Start the Spring Boot application
```bash
cd backend
mvn spring-boot:run

→ Wait for: "Started EduworkApplication"
```

---

## 🎯 Success Criteria

**All tests passing means**:
✅ Can discover and search sessions  
✅ Can book available sessions  
✅ Optimistic locking prevents overbooking  
✅ Payment confirmation works  
✅ Refund calculation accurate  
✅ Authorization enforced  
✅ Business rules validated

---

## 📝 Notes

- **Authentication**: All booking endpoints require valid JWT token
- **Public Endpoints**: Session search and details are public (no auth)
- **Auto-Saving**: `bookingId` is automatically saved after booking
- **Refund Policy**: Platform fee (15%) is always non-refundable
- **Concurrent Testing**: Best tested with 2 Postman tabs or Postman Runner

---

**Collection Ready!** 🎉  
**Start Testing**: Import → Set Variables → Run Flow 1
