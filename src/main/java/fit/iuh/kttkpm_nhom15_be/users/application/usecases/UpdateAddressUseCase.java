package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateAddressCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.AddressDTO;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.AddressNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.Address;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateAddressUseCase {

    private final AddressRepository addressRepository;
    private final UserFacade userFacade;

    @Transactional
    public AddressDTO execute(UpdateAddressCommand command) {
        validateActiveUser(command.userId());

        Address existingAddress = addressRepository.findByIdAndUserId(command.id(), command.userId())
                .orElseThrow(() -> new AddressNotFoundException(command.id()));

        existingAddress.setFullName(command.fullName());
        existingAddress.setPhone(command.phone());
        existingAddress.setAddress(command.address());
        existingAddress.setCity(command.city());
        existingAddress.setDistrict(command.district());
        existingAddress.setWard(command.ward());
        existingAddress.setDefault(command.isDefault());

        if (command.isDefault()) {
            addressRepository.updateAllDefaultToFalse(command.userId());
        }

        Address savedAddress = addressRepository.save(existingAddress);
        return AddressDTO.from(savedAddress);
    }

    private void validateActiveUser(String userId) {
        if (!userFacade.isUserActive(userId)) {
            throw new ActionNotAllowedException("Tài khoản không hoạt động, không thể cập nhật địa chỉ.");
        }
    }
}
