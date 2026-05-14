package fit.iuh.kttkpm_nhom15_be.users.application.dto;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.Address;

public record AddressDTO(
        String id,
        String userId,
        String fullName,
        String phone,
        String address,
        String city,
        String district,
        String ward,
        boolean isDefault
) {
    public static AddressDTO from(Address address) {
        return new AddressDTO(
                address.getId(),
                address.getUserId(),
                address.getFullName(),
                address.getPhone(),
                address.getAddress(),
                address.getCity(),
                address.getDistrict(),
                address.getWard(),
                address.isDefault()
        );
    }
}
