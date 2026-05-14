package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.dto.AddressDTO;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAddressesUseCase {

    private final AddressRepository addressRepository;
    private final UserFacade userFacade;

    @Transactional(readOnly = true)
    public List<AddressDTO> execute(String userId) {
        validateActiveUser(userId);

        return addressRepository.findByUserId(userId).stream()
                .map(AddressDTO::from)
                .toList();
    }

    private void validateActiveUser(String userId) {
        if (!userFacade.isUserActive(userId)) {
            throw new ActionNotAllowedException("Tài khoản không hoạt động, không thể xem địa chỉ.");
        }
    }
}
