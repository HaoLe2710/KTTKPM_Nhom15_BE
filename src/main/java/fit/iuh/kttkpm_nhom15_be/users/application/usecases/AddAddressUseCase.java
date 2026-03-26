package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.AddAddressCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.AddressDTO;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.Address;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddAddressUseCase {

    private final AddressRepository addressRepository;
    private final UserFacade userFacade;

    @Transactional
    public AddressDTO execute(AddAddressCommand command) {
        validateActiveUser(command.userId());

        if (command.isDefault()) {
            addressRepository.updateAllDefaultToFalse(command.userId());
        }

        Address savedAddress = addressRepository.save(Address.builder()
                .userId(command.userId())
                .fullName(command.fullName())
                .phone(command.phone())
                .address(command.address())
                .city(command.city())
                .district(command.district())
                .ward(command.ward())
                .isDefault(command.isDefault())
                .build());

        return AddressDTO.from(savedAddress);
    }

    private void validateActiveUser(String userId) {
        if (!userFacade.isUserActive(userId)) {
            throw new ActionNotAllowedException("Tài khoản không hoạt động, không thể thêm địa chỉ.");
        }
    }
}
