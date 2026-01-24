package com.eduwork.identity.domain.model;

/**
 * Types of KYC documents that can be uploaded.
 */
public enum KycDocumentType {
    ID_CARD, // National ID card
    PASSPORT, // Passport
    STUDENT_CARD, // Student ID card (for students)
    CERTIFICATE, // Teaching certificate (for mentors)
    DEGREE // Academic degree (for mentors)
}
