package com.eduwork.identity.application.mapper;

import com.eduwork.identity.application.dto.UserResponseDTO;
import com.eduwork.identity.domain.model.User;

/**
 * Mapper for converting between User domain entity and DTOs.
 * Follows Clean Architecture: Application layer knows about domain,
 * but domain doesn't know about DTOs.
 */
public class UserMapper {

    /**
     * Maps User domain entity to UserResponseDTO.
     * Excludes sensitive information like password hash.
     * 
     * @param user domain entity
     * @return response DTO for API
     */
    public static UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .profileComplete(user.isProfileComplete())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
