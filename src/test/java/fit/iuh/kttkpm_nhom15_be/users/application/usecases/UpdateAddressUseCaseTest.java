package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateAddressCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.AddressDTO;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.AddressNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.Address;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.AddressRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateAddressUseCaseTest {

    @Test
    void executeUpdatesAddressAndResetsPreviousDefaultWhenNeeded() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        UpdateAddressUseCase useCase = new UpdateAddressUseCase(addressRepository, userFacade);

        Address existingAddress = Address.builder()
                .id("addr-1")
                .userId("user-1")
                .fullName("Nguyen Van A")
                .phone("0909000001")
                .address("12 Nguyen Trai")
                .city("Ho Chi Minh")
                .district("Quan 1")
                .ward("Ben Nghe")
                .isDefault(false)
                .build();

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(addressRepository.findByIdAndUserId("addr-1", "user-1")).thenReturn(Optional.of(existingAddress));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressDTO result = useCase.execute(new UpdateAddressCommand(
                "addr-1",
                "user-1",
                "Le Thi C",
                "0909000003",
                "56 Vo Van Tan",
                "Ho Chi Minh",
                "Quan 3",
                "Ward 6",
                true
        ));

        ArgumentCaptor<Address> savedAddress = ArgumentCaptor.forClass(Address.class);

        verify(addressRepository).updateAllDefaultToFalse("user-1");
        verify(addressRepository).save(savedAddress.capture());

        assertEquals("Le Thi C", savedAddress.getValue().getFullName());
        assertEquals("0909000003", savedAddress.getValue().getPhone());
        assertEquals("56 Vo Van Tan", savedAddress.getValue().getAddress());
        assertTrue(savedAddress.getValue().isDefault());
        assertEquals("Le Thi C", result.fullName());
    }

    @Test
    void executeUpdatesAddressWithoutResettingDefaultWhenNotMarkedAsDefault() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        UpdateAddressUseCase useCase = new UpdateAddressUseCase(addressRepository, userFacade);

        Address existingAddress = Address.builder()
                .id("addr-2")
                .userId("user-1")
                .fullName("Tran Thi B")
                .phone("0909000002")
                .address("34 Le Loi")
                .city("Da Nang")
                .district("Hai Chau")
                .ward("Thach Thang")
                .isDefault(true)
                .build();

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(addressRepository.findByIdAndUserId("addr-2", "user-1")).thenReturn(Optional.of(existingAddress));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressDTO result = useCase.execute(new UpdateAddressCommand(
                "addr-2",
                "user-1",
                "Tran Thi B",
                "0909000002",
                "99 Bach Dang",
                "Da Nang",
                "Hai Chau",
                "Hai Chau 1",
                false
        ));

        verify(addressRepository, never()).updateAllDefaultToFalse("user-1");
        assertEquals("99 Bach Dang", result.address());
        assertFalse(result.isDefault());
    }

    @Test
    void executeThrowsVietnameseExceptionWhenAddressDoesNotExist() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        UpdateAddressUseCase useCase = new UpdateAddressUseCase(addressRepository, userFacade);

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(addressRepository.findByIdAndUserId("missing-address", "user-1")).thenReturn(Optional.empty());

        AddressNotFoundException ex = assertThrows(AddressNotFoundException.class, () -> useCase.execute(
                new UpdateAddressCommand("missing-address", "user-1", "A", "0909", "abc", "HCM", "Q1", "W1", true)
        ));

        assertEquals("Không tìm thấy địa chỉ với ID: missing-address", ex.getMessage());
        verify(addressRepository, never()).updateAllDefaultToFalse(any());
        verify(addressRepository, never()).save(any());
    }

    @Test
    void executeThrowsVietnameseExceptionWhenUserIsInactive() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        UpdateAddressUseCase useCase = new UpdateAddressUseCase(addressRepository, userFacade);

        when(userFacade.isUserActive("user-1")).thenReturn(false);

        ActionNotAllowedException ex = assertThrows(ActionNotAllowedException.class, () -> useCase.execute(
                new UpdateAddressCommand("addr-1", "user-1", "A", "0909", "abc", "HCM", "Q1", "W1", true)
        ));

        assertEquals("Tài khoản không hoạt động, không thể cập nhật địa chỉ.", ex.getMessage());
        verify(addressRepository, never()).findByIdAndUserId(any(), any());
        verify(addressRepository, never()).save(any());
    }
}
