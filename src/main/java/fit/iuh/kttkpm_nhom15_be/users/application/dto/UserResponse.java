package fit.iuh.kttkpm_nhom15_be.users.application.dto;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;

public record UserResponse(
        String id,
        String email,
        String phone,
        String fullName,
        String avatarUrl,
        UserRole role,
        boolean isActive
) {

    public static UserResponse fromDomain(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getRole(),
                user.isActive()
        );
    }
}