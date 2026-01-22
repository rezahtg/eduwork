package com.eduwork.identity.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Mentor profile value object.
 * Contains mentor-specific professional information.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentorProfile {

    @Column(name = "mentor_bio", columnDefinition = "TEXT")
    private String mentorBio; // Professional bio (max 1000 chars)

    @Column(name = "qualifications", length = 500)
    private String qualifications; // Degrees, certifications summary

    @Column(name = "education", length = 500)
    private String education; // University, degree, graduation year

    @Column(name = "certifications", length = 500)
    private String certifications; // Professional certifications

    @Column(name = "portfolio_website", length = 200)
    private String portfolioWebsite; // Portfolio/LinkedIn URL

    @Column(name = "hourly_rate_idr")
    private Long hourlyRateIdr; // Hourly rate in Indonesian Rupiah (whole number)

    /**
     * Creates a mentor profile with required and optional fields.
     *
     * @param mentorBio        professional biography
     * @param qualifications   education qualifications summary
     * @param education        detailed education (university, degree, year)
     * @param certifications   professional certifications
     * @param portfolioWebsite portfolio or LinkedIn URL
     * @param hourlyRateIdr    hourly rate in IDR (optional)
     * @return new mentor profile
     */
    public static MentorProfile create(
            String mentorBio,
            String qualifications,
            String education,
            String certifications,
            String portfolioWebsite,
            Long hourlyRateIdr) {

        validateMentorBio(mentorBio);
        validateQualifications(qualifications);
        validateEducation(education);
        validateCertifications(certifications);
        validatePortfolioWebsite(portfolioWebsite);
        validateHourlyRate(hourlyRateIdr);

        MentorProfile profile = new MentorProfile();
        profile.mentorBio = mentorBio.trim();
        profile.qualifications = qualifications.trim();
        profile.education = education.trim();
        profile.certifications = certifications != null ? certifications.trim() : null;
        profile.portfolioWebsite = portfolioWebsite != null ? portfolioWebsite.trim() : null;
        profile.hourlyRateIdr = hourlyRateIdr;
        return profile;
    }

    private static void validateMentorBio(String bio) {
        if (bio == null || bio.trim().isEmpty()) {
            throw new IllegalArgumentException("Mentor bio is required");
        }
        if (bio.length() > 1000) {
            throw new IllegalArgumentException("Mentor bio must not exceed 1000 characters");
        }
    }

    private static void validateQualifications(String qualifications) {
        if (qualifications == null || qualifications.trim().isEmpty()) {
            throw new IllegalArgumentException("Qualifications are required");
        }
        if (qualifications.length() > 500) {
            throw new IllegalArgumentException("Qualifications must not exceed 500 characters");
        }
    }

    private static void validateEducation(String education) {
        if (education == null || education.trim().isEmpty()) {
            throw new IllegalArgumentException("Education information is required");
        }
        if (education.length() > 500) {
            throw new IllegalArgumentException("Education must not exceed 500 characters");
        }
    }

    private static void validateCertifications(String certifications) {
        if (certifications != null && certifications.length() > 500) {
            throw new IllegalArgumentException("Certifications must not exceed 500 characters");
        }
    }

    private static void validatePortfolioWebsite(String website) {
        if (website != null) {
            if (website.length() > 200) {
                throw new IllegalArgumentException("Portfolio website URL must not exceed 200 characters");
            }
            // Basic URL validation
            if (!website.matches("^https?://.*")) {
                throw new IllegalArgumentException(
                        "Portfolio website must be a valid URL starting with http:// or https://");
            }
        }
    }

    private static void validateHourlyRate(Long rate) {
        if (rate != null && rate < 0) {
            throw new IllegalArgumentException("Hourly rate cannot be negative");
        }
    }
}
