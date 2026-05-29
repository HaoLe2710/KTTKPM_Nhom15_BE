package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.AddAddressCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.AddressDTO;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.Address;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.AddressRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddAddressUseCaseTest {

    @Test
    void executeAddsAddressAndResetsOldDefaultWhenRequestMarkedAsDefault() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        AddAddressUseCase useCase = new AddAddressUseCase(addressRepository, userFacade);

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(addressRepository.save(any(Address.class))).thenReturn(Address.builder()
                .id("addr-1")
                .userId("user-1")
                .fullName("Nguyen Van A")
                .phone("0909000001")
                .address("12 Nguyen Trai")
                .city("Ho Chi Minh")
                .district("Quan 1")
                .ward("Ben Nghe")
                .isDefault(true)
                .build());

        AddressDTO result = useCase.execute(new AddAddressCommand(
                "user-1",
                "Nguyen Van A",
                "0909000001",
                "12 Nguyen Trai",
                "Ho Chi Minh",
                "Quan 1",
                "Ben Nghe",
                true
        ));

        ArgumentCaptor<Address> savedAddress = ArgumentCaptor.forClass(Address.class);

        verify(addressRepository).updateAllDefaultToFalse("user-1");
        verify(addressRepository).save(savedAddress.capture());

        assertEquals("addr-1", result.id());
        assertEquals("user-1", result.userId());
        assertEquals("Nguyen Van A", savedAddress.getValue().getFullName());
        assertTrue(savedAddress.getValue().isDefault());
    }

    @Test
    void executeAddsAddressWithoutResettingDefaultWhenRequestIsNotDefault() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        AddAddressUseCase useCase = new AddAddressUseCase(addressRepository, userFacade);

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(addressRepository.save(any(Address.class))).thenReturn(Address.builder()
                .id("addr-2")
                .userId("user-1")
                .fullName("Tran Thi B")
                .phone("0909000002")
                .address("34 Lê Lợi")
                .city("Da Nang")
                .district("Hai Chau")
                .ward("Thach Thang")
                .isDefault(false)
                .build());

        AddressDTO result = useCase.execute(new AddAddressCommand(
                "user-1",
                "Tran Thi B",
                "0909000002",
                "34 Lê Lợi",
                "Da Nang",
                "Hai Chau",
                "Thach Thang",
                false
        ));

        verify(addressRepository, never()).updateAllDefaultToFalse("user-1");
        assertEquals("addr-2", result.id());
        assertFalse(result.isDefault());
    }

    @Test
    void executeThrowsVietnameseExceptionWhenUserIsInactive() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        AddAddressUseCase useCase = new AddAddressUseCase(addressRepository, userFacade);

        when(userFacade.isUserActive("user-1")).thenReturn(false);

        ActionNotAllowedException ex = assertThrows(ActionNotAllowedException.class, () -> useCase.execute(
                new AddAddressCommand("user-1", "Nguyen Van A", "0909000001", "12 Nguyen Trai", "Ho Chi Minh", "Quan 1", "Ben Nghe", true)
        ));

        assertEquals("Tài khoản không hoạt động, không thể thêm địa chỉ.", ex.getMessage());
        verify(addressRepository, never()).updateAllDefaultToFalse(any());
        verify(addressRepository, never()).save(any());
    }
}
