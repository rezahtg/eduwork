# Eduwork E2E Flow - Sequence Diagrams

Complete user journey diagrams for all platform scenarios.

---

## Journey 1: Student Registration & Onboarding

```mermaid
sequenceDiagram
    autonumber
    actor S as Student
    participant API as Eduwork API
    participant DB as Database
    participant Email as Email Service
    participant Admin as Admin

    Note over S,Admin: Registration Flow
    S->>API: POST /auth/register (email, password, role=STUDENT)
    API->>DB: Create user (status=PENDING_VERIFICATION)
    API->>Email: Send verification email
    API-->>S: 201 Created + token

    S->>Email: Click verification link
    S->>API: POST /auth/verify-email (token)
    API->>DB: Update user (status=ACTIVE, profileComplete=FALSE)
    API-->>S: 200 Email verified

    Note over S,Admin: Profile Completion (Required for full access)
    S->>API: GET /schedules (try to search)
    API-->>S: 403 Profile incomplete

    S->>API: PUT /users/me/profile (fullName, grade, school)
    API->>DB: Update user_profiles
    API->>DB: Update users (profileComplete=TRUE)
    API-->>S: 200 Profile updated

    Note over S,Admin: Student KYC Submission
    S->>API: POST /users/me/kyc (documentType=STUDENT_CARD, file)
    API->>DB: Create user_kyc_documents (status=PENDING)
    API-->>S: 201 KYC submitted
    
    Admin->>API: GET /admin/kyc/pending
    API-->>Admin: List of pending KYC
    Admin->>API: POST /admin/kyc/{id}/approve
    API->>DB: Update KYC (status=APPROVED)
    API->>Email: Notify student
    API-->>Admin: 200 Approved
```

---

## Journey 2: Mentor Registration & KYC

```mermaid
sequenceDiagram
    autonumber
    actor M as Mentor
    participant API as Eduwork API
    participant DB as Database
    participant Email as Email Service
    participant Admin as Admin

    Note over M,Admin: Registration (same as student)
    M->>API: POST /auth/register (email, password, role=MENTOR)
    API->>DB: Create user (status=PENDING_VERIFICATION)
    API->>Email: Send verification email
    API-->>M: 201 Created

    M->>API: POST /auth/verify-email (token)
    API->>DB: Update user (status=ACTIVE)
    API-->>M: 200 Verified

    Note over M,Admin: Profile + KYC (Required to create schedules)
    M->>API: PUT /users/me/profile (fullName, expertise, experience)
    API->>DB: Update user_profiles
    API-->>M: 200 Updated

    M->>API: POST /users/me/kyc (type=ID_CARD, file)
    API-->>M: 201 ID submitted
    M->>API: POST /users/me/kyc (type=TEACHING_CERT, file)
    API-->>M: 201 Certificate submitted

    M->>API: POST /users/me/bank-accounts (bank, account, holder)
    API->>DB: Create user_bank_accounts
    API-->>M: 201 Bank added

    Note over M,Admin: Admin Review
    Admin->>API: GET /admin/kyc/pending
    Admin->>API: POST /admin/kyc/{id}/approve (ID)
    Admin->>API: POST /admin/kyc/{id}/approve (Certificate)
    API->>DB: Update all KYC to APPROVED
    API->>DB: Update user (kycStatus=VERIFIED)
    API->>Email: Notify mentor "You can now create schedules"
```

---

## Journey 3: Mentor Creates Schedule

```mermaid
sequenceDiagram
    autonumber
    actor M as Mentor
    participant API as Eduwork API
    participant DB as Database
    participant ES as Elasticsearch

    Note over M,ES: Create Single Schedule
    M->>API: POST /schedules
    Note right of M: subject: "Matematika"<br/>title: "Kalkulus Dasar"<br/>price: 150000<br/>sessionType: PRIVATE<br/>startTime: "2025-01-15T09:00"<br/>endTime: "2025-01-15T10:30"
    
    API->>API: Validate quality constraints
    Note right of API: Check: Max 8 hrs/day<br/>Check: 15 min rest between sessions
    
    API->>DB: Create schedule (status=OPEN)
    API->>ES: Index schedule for discovery
    API-->>M: 201 Schedule created

    Note over M,ES: Create Recurring Schedule
    M->>API: POST /schedules
    Note right of M: recurrenceRule: "WEEKLY"<br/>for 4 weeks
    
    API->>DB: Create 4 schedule instances
    API->>ES: Index all schedules
    API-->>M: 201 Created (4 instances)

    Note over M,ES: Create Group Session
    M->>API: POST /schedules
    Note right of M: sessionType: GROUP<br/>minStudents: 3<br/>maxStudents: 8
    
    API->>DB: Create schedule
    API-->>M: 201 Group schedule created
```

---

## Journey 4: Student Discovers & Books Schedule

```mermaid
sequenceDiagram
    autonumber
    actor S as Student
    participant API as Eduwork API
    participant ES as Elasticsearch
    participant DB as Database
    participant M as Mentor

    Note over S,M: Discovery
    S->>API: GET /schedules?subject=Matematika&priceMax=200000
    API->>ES: Search schedules
    ES-->>API: Matching results
    API-->>S: 200 Schedule list with mentor profiles

    S->>API: GET /schedules/{id}
    API-->>S: 200 Schedule details + mentor info

    Note over S,M: Pre-booking Discussion
    S->>API: POST /discussions/pre-booking
    Note right of S: scheduleId, message: "Bisa fokus integral?"
    API->>DB: Create pre_booking_discussion
    API->>DB: Create message
    API->>API: Send notification to mentor
    API-->>S: 201 Discussion started

    M->>API: GET /discussions/{id}/messages
    M->>API: POST /discussions/{id}/messages
    Note right of M: "Bisa, kita mulai dari dasar"
    API-->>M: 201 Message sent
    API->>API: Notify student

    Note over S,M: Booking
    S->>API: POST /bookings
    Note right of S: scheduleId, bookingType: PRIVATE
    API->>DB: Create booking (status=PENDING_PAYMENT)
    API->>DB: Create transaction (status=PENDING)
    API->>DB: Update discussion (status=BOOKED)
    API-->>S: 201 Booking + payment info
    Note left of API: Returns bank account for transfer
```

---

## Journey 5: Payment Flow (Manual Transfer)

```mermaid
sequenceDiagram
    autonumber
    actor S as Student
    participant API as Eduwork API
    participant DB as Database
    participant Admin as Admin
    participant M as Mentor

    Note over S,M: Student Makes Transfer
    S->>S: Transfer via mobile banking
    S->>API: POST /payments/{txId}/proof (screenshot)
    API->>DB: Create payment_proof
    API->>DB: Update transaction (status=PROOF_SUBMITTED)
    API->>API: Notify admin
    API-->>S: 200 Proof submitted

    Note over S,M: Admin Verification
    Admin->>API: GET /admin/payments/pending-verification
    API-->>Admin: List with proof images
    
    alt Proof Valid
        Admin->>API: POST /admin/payments/{txId}/verify
        API->>DB: Update transaction (status=HELD)
        API->>DB: Update booking (status=CONFIRMED)
        API->>API: Notify student "Payment confirmed"
        API->>API: Notify mentor "New booking confirmed"
        API-->>Admin: 200 Verified
    else Proof Invalid
        Admin->>API: POST /admin/payments/{txId}/reject
        Note right of Admin: reason: "Amount tidak sesuai"
        API->>DB: Update payment_proof (rejected)
        API->>API: Notify student "Please resubmit"
        API-->>Admin: 200 Rejected
        S->>API: POST /payments/{txId}/proof (new screenshot)
    end
```

---

## Journey 6: Live Session Execution

```mermaid
sequenceDiagram
    autonumber
    participant Sys as System Scheduler
    participant API as Eduwork API
    participant DB as Database
    participant Meet as Google Meet / Zoom
    actor M as Mentor
    actor S as Student

    Note over Sys,S: Session Preparation (30 min before)
    Sys->>API: Trigger pre-session job
    API->>Meet: Create meeting
    Meet-->>API: Meeting URL + password
    API->>DB: Create live_session
    API->>API: Notify mentor + student with link
    
    Note over Sys,S: Session Start
    M->>API: POST /bookings/{id}/attendance
    Note right of M: action: CHECK_IN
    API->>DB: Record mentor attendance
    
    S->>API: POST /bookings/{id}/attendance
    Note right of S: action: CHECK_IN
    API->>DB: Record student attendance
    
    M->>Meet: Start session
    S->>Meet: Join session

    Note over Sys,S: In-Session Discussion
    S->>API: GET /bookings/{id}/discussion
    S->>API: POST /discussions/{id}/messages
    Note right of S: "Bisa jelaskan ulang bagian X?"

    Note over Sys,S: Session End
    M->>API: POST /bookings/{id}/attendance
    Note right of M: action: CHECK_OUT
    S->>API: POST /bookings/{id}/attendance
    Note right of S: action: CHECK_OUT
    API->>DB: Calculate duration
```

---

## Journey 7: Session Completion & Settlement

```mermaid
sequenceDiagram
    autonumber
    actor M as Mentor
    participant API as Eduwork API
    participant DB as Database
    actor S as Student
    participant Batch as Batch Job

    Note over M,Batch: Normal Completion
    M->>API: POST /bookings/{id}/complete
    API->>DB: Update booking (completedBy=MENTOR)
    API->>API: Notify student to confirm
    
    S->>API: POST /bookings/{id}/complete
    API->>DB: Update booking (status=COMPLETED)
    API->>DB: Update transaction (status=RELEASED)
    API->>DB: Calculate: mentorAmount = amount - 5% fee
    API->>API: Notify mentor "Payment released: Rp142.500"
    API-->>S: 200 Session completed

    Note over M,Batch: Auto-completion (48h timeout)
    M->>API: POST /bookings/{id}/complete
    Note right of M: Student doesn't respond...
    
    Batch->>API: Check pending completions > 48h
    API->>DB: Auto-complete booking
    API->>DB: Release payment to mentor
    API->>API: Notify both parties
```

---

## Journey 8: Dispute Resolution

```mermaid
sequenceDiagram
    autonumber
    actor S as Student
    participant API as Eduwork API
    participant DB as Database
    participant Admin as Admin
    actor M as Mentor

    Note over S,M: Student Raises Dispute
    S->>API: POST /disputes
    Note right of S: transactionId<br/>reason: SESSION_NOT_CONDUCTED<br/>description: "Mentor tidak hadir"<br/>evidenceUrls: [screenshots]
    
    API->>DB: Create dispute (status=OPEN)
    API->>DB: Keep transaction in HELD status
    API->>API: Urgent notification to admin
    API-->>S: 201 Dispute created

    Note over S,M: Admin Investigation
    Admin->>API: GET /admin/disputes
    API-->>Admin: Dispute list with details
    
    Admin->>Admin: Review evidence
    Note right of Admin: Check attendance records<br/>Check chat history<br/>Review screenshots

    alt Dispute Valid - Student Wins
        Admin->>API: POST /admin/disputes/{id}/resolve
        Note right of Admin: resolutionType: REFUND_FULL<br/>notes: "Mentor confirmed absent"
        API->>DB: Update dispute (status=RESOLVED)
        API->>DB: Update transaction (status=REFUNDED)
        API->>API: Notify student "Full refund processed"
        API->>API: Notify mentor "Dispute resolved against you"
        API->>DB: Record mentor warning
        
    else Dispute Invalid - Mentor Wins
        Admin->>API: POST /admin/disputes/{id}/resolve
        Note right of Admin: resolutionType: RELEASE_TO_MENTOR<br/>notes: "Student was absent"
        API->>DB: Update transaction (status=RELEASED)
        API->>API: Notify mentor "Payment released"
        API->>API: Notify student "Dispute closed"
        
    else Partial Resolution
        Admin->>API: POST /admin/disputes/{id}/resolve
        Note right of Admin: resolutionType: SPLIT<br/>refundAmount: 75000<br/>mentorAmount: 67500
        API->>DB: Process partial refund
        API->>DB: Release partial to mentor
    end
```

---

## Journey 9: Group Session Flow

```mermaid
sequenceDiagram
    autonumber
    actor M as Mentor
    participant API as Eduwork API
    participant DB as Database
    actor S1 as Student 1
    actor S2 as Student 2
    actor S3 as Student 3

    Note over M,S3: Mentor Creates Group Schedule
    M->>API: POST /schedules
    Note right of M: sessionType: GROUP<br/>minStudents: 3<br/>maxStudents: 5<br/>price: 75000
    API->>DB: Create schedule
    API-->>M: 201 Created

    Note over M,S3: Students Book
    S1->>API: POST /bookings (scheduleId)
    API->>DB: Create booking
    API-->>S1: "Menunggu 2 peserta lagi"
    
    S2->>API: POST /bookings (scheduleId)
    API-->>S2: "Menunggu 1 peserta lagi"
    
    Note over M,S3: Session time approaches, min not reached
    API->>M: Notification "2/3 students, session in 24h"
    
    alt Extend Waiting
        M->>API: POST /schedules/{id}/extend-waiting
        Note right of M: extendUntil: +48 hours
        API->>DB: Update waiting_extended_until
        API->>API: Notify booked students
        API-->>M: 200 Extended
        
        S3->>API: POST /bookings (scheduleId)
        API->>DB: Create booking
        API-->>S3: "Group complete! Proceed to payment"
        API->>API: Notify all students
        
    else Start Anyway
        M->>API: POST /schedules/{id}/start-anyway
        API->>DB: Update schedule (proceed with 2)
        API->>API: Notify booked students "Session proceeds!"
        API-->>M: 200 Will proceed
    end
```

---

## Journey 10: Brute Force Protection

```mermaid
sequenceDiagram
    autonumber
    actor A as Attacker
    participant API as Eduwork API
    participant DB as Database
    participant Redis as Redis Cache

    Note over A,Redis: Failed Login Attempts
    loop 5 times
        A->>API: POST /auth/login (wrong password)
        API->>DB: Record login_attempt (success=FALSE)
        API->>Redis: Increment fail count for user
        API-->>A: 401 Invalid credentials
    end

    Note over A,Redis: Account Locked
    A->>API: POST /auth/login (correct password!)
    API->>Redis: Check fail count >= 5
    API->>DB: Set locked_until = NOW + 30 min
    API-->>A: 423 Account locked

    Note over A,Redis: IP Rate Limiting
    loop 10 times in 1 minute
        A->>API: POST /auth/login (any credentials)
        API->>Redis: Increment IP request count
    end
    
    A->>API: POST /auth/login
    API->>Redis: Check IP count > threshold
    API-->>A: 429 Too Many Requests
    Note left of API: Blocked for 5 minutes
```

---

## System Events Summary

| Event | Trigger | Actions |
|-------|---------|---------|
| Pre-session reminder | 30 min before | Create Meet/Zoom link, notify users |
| Session start check | At start time | Check attendance, send reminders |
| Auto-completion | 48h after mentor marks complete | Release payment if no student response |
| KYC reminder | 24h after registration | Email users with incomplete KYC |
| Group min warning | 24h before session | Notify mentor about group status |
| Dispute escalation | 72h no admin action | Alert senior admin |
| Payment settlement batch | Daily at midnight | Process all released transactions |
