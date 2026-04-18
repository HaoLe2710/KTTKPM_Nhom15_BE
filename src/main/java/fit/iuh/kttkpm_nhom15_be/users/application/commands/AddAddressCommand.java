package fit.iuh.kttkpm_nhom15_be.users.application.commands;

public record AddAddressCommand(
        String userId,
        String fullName,
        String phone,
        String address,
        String city,
        String district,
        String ward,
        boolean isDefault
) {
}
