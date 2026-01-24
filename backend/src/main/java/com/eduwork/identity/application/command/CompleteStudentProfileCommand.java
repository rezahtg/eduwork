package com.eduwork.identity.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command for completing student profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteStudentProfileCommand {
    private Integer gradeLevel;
    private String schoolName;
    private String bio;
}
