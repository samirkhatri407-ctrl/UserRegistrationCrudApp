package com.example.userregistration.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        LocalDate dateOfBirth,
        String phoneNumber,
        String addressLine1,
        String addressLine2,
        String city,
        String stateProvince,
        String postalCode,
        String country,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean active
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getDateOfBirth(),
                user.getPhoneNumber(),
                user.getAddressLine1(),
                user.getAddressLine2(),
                user.getCity(),
                user.getStateProvince(),
                user.getPostalCode(),
                user.getCountry(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.isActive()
        );
    }
}
