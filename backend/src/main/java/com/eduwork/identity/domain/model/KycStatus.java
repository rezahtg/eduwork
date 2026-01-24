package com.eduwork.identity.domain.model;

/**
 * Status of KYC document verification.
 */
public enum KycStatus {
    PENDING, // Uploaded, awaiting review
    APPROVED, // Verified and approved
    REJECTED // Rejected (invalid/unclear)
}
