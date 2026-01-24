package com.eduwork.identity.domain.model;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Student profile value object.
 * Contains student-specific information.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentProfile {

    private Integer gradeLevel; // 1-12
    private String schoolName; // max 200 chars
    private String studentBio; // max 500 chars (optional)

    /**
     * Creates a student profile with required fields.
     *
     * @param gradeLevel grade level (1-12)
     * @param schoolName school name
     * @param studentBio optional bio
     * @return new student profile
     */
    public static StudentProfile create(Integer gradeLevel, String schoolName, String studentBio) {
        validateGradeLevel(gradeLevel);
        validateSchoolName(schoolName);
        validateBio(studentBio);

        StudentProfile profile = new StudentProfile();
        profile.gradeLevel = gradeLevel;
        profile.schoolName = schoolName.trim();
        profile.studentBio = studentBio != null ? studentBio.trim() : null;
        return profile;
    }

    private static void validateGradeLevel(Integer gradeLevel) {
        if (gradeLevel == null) {
            throw new IllegalArgumentException("Grade level is required");
        }
        if (gradeLevel < 1 || gradeLevel > 12) {
            throw new IllegalArgumentException("Grade level must be between 1 and 12");
        }
    }

    private static void validateSchoolName(String schoolName) {
        if (schoolName == null || schoolName.trim().isEmpty()) {
            throw new IllegalArgumentException("School name is required");
        }
        if (schoolName.length() > 200) {
            throw new IllegalArgumentException("School name must not exceed 200 characters");
        }
    }

    private static void validateBio(String bio) {
        if (bio != null && bio.length() > 500) {
            throw new IllegalArgumentException("Bio must not exceed 500 characters");
        }
    }
}
