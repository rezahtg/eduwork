package com.eduwork.identity.presentation.dto;

import com.eduwork.identity.domain.model.User;
import com.eduwork.identity.domain.model.UserStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for user profile information.
 * Includes both user data and profile-specific data (student or mentor).
 */
@Value
@Builder
public class UserProfileResponse {
    UUID id;
    String email;
    String phone;
    UserStatus status;
    boolean profileComplete;
    Instant emailVerifiedAt;
    Instant createdAt;

    // Profile data - only one will be populated
    StudentProfileData studentProfile;
    MentorProfileData mentorProfile;

    /**
     * Factory method to create response from domain User.
     */
    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .profileComplete(user.isProfileComplete())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .createdAt(user.getCreatedAt())
                .studentProfile(
                        user.getStudentProfile() != null ? StudentProfileData.from(user.getStudentProfile()) : null)
                .mentorProfile(user.getMentorProfile() != null ? MentorProfileData.from(user.getMentorProfile()) : null)
                .build();
    }

    @Value
    @Builder
    public static class StudentProfileData {
        Integer gradeLevel;
        String schoolName;
        String bio;

        public static StudentProfileData from(com.eduwork.identity.domain.model.StudentProfile profile) {
            return StudentProfileData.builder()
                    .gradeLevel(profile.getGradeLevel())
                    .schoolName(profile.getSchoolName())
                    .bio(profile.getStudentBio())
                    .build();
        }
    }

    @Value
    @Builder
    public static class MentorProfileData {
        String bio;
        String qualifications;
        String education;
        String certifications;
        String portfolioWebsite;
        Long hourlyRateIdr;

        public static MentorProfileData from(com.eduwork.identity.domain.model.MentorProfile profile) {
            return MentorProfileData.builder()
                    .bio(profile.getMentorBio())
                    .qualifications(profile.getQualifications())
                    .education(profile.getEducation())
                    .certifications(profile.getCertifications())
                    .portfolioWebsite(profile.getPortfolioWebsite())
                    .hourlyRateIdr(profile.getHourlyRateIdr())
                    .build();
        }
    }
}
