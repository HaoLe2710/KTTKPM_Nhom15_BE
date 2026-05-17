package fit.iuh.kttkpm_nhom15_be.users.application.commands;

public record UpdateProfileCommand(
        String userId,
        String email,
        String phone,
        String fullName,
        String avatarUrl,
        String avatarOriginalFilename,
        String avatarContentType,
        byte[] avatarBytes
) {
}
