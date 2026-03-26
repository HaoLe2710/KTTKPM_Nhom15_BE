package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

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
public class DeleteAddressUseCase {

    private final AddressRepository addressRepository;
    private final UserFacade userFacade;

    @Transactional
    public void execute(String addressId, String userId) {
        validateActiveUser(userId);

        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        addressRepository.delete(address);
    }

    private void validateActiveUser(String userId) {
        if (!userFacade.isUserActive(userId)) {
            throw new ActionNotAllowedException("Tài khoản không hoạt động, không thể xóa địa chỉ.");
        }
    }
}
