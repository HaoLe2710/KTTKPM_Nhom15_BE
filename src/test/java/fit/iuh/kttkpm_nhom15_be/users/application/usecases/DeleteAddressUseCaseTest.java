package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.AddressNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.Address;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.AddressRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteAddressUseCaseTest {

    @Test
    void executeDeletesAddressSuccessfullyWhenUserOwnsAddress() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        DeleteAddressUseCase useCase = new DeleteAddressUseCase(addressRepository, userFacade);

        Address address = Address.builder()
                .id("addr-1")
                .userId("user-1")
                .fullName("Nguyen Van A")
                .build();

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(addressRepository.findByIdAndUserId("addr-1", "user-1")).thenReturn(Optional.of(address));

        useCase.execute("addr-1", "user-1");

        verify(addressRepository).delete(address);
    }

    @Test
    void executeThrowsVietnameseExceptionWhenAddressDoesNotExist() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        DeleteAddressUseCase useCase = new DeleteAddressUseCase(addressRepository, userFacade);

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(addressRepository.findByIdAndUserId("missing-address", "user-1")).thenReturn(Optional.empty());

        AddressNotFoundException ex = assertThrows(AddressNotFoundException.class,
                () -> useCase.execute("missing-address", "user-1"));

        assertEquals("Không tìm thấy địa chỉ với ID: missing-address", ex.getMessage());
        verify(addressRepository, never()).delete(any());
    }

    @Test
    void executeThrowsVietnameseExceptionWhenUserIsInactive() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        DeleteAddressUseCase useCase = new DeleteAddressUseCase(addressRepository, userFacade);

        when(userFacade.isUserActive("user-1")).thenReturn(false);

        ActionNotAllowedException ex = assertThrows(ActionNotAllowedException.class,
                () -> useCase.execute("addr-1", "user-1"));

        assertEquals("Tài khoản không hoạt động, không thể xóa địa chỉ.", ex.getMessage());
        verify(addressRepository, never()).findByIdAndUserId(any(), any());
        verify(addressRepository, never()).delete(any());
    }
}
