package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.dto.AddressDTO;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.Address;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.AddressRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetAddressesUseCaseTest {

    @Test
    void executeReturnsAddressesForActiveUser() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        GetAddressesUseCase useCase = new GetAddressesUseCase(addressRepository, userFacade);

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(addressRepository.findByUserId("user-1")).thenReturn(List.of(
                Address.builder()
                        .id("addr-1")
                        .userId("user-1")
                        .fullName("Nguyen Van A")
                        .phone("0909000001")
                        .address("12 Nguyen Trai")
                        .city("Ho Chi Minh")
                        .district("Quan 1")
                        .ward("Ben Nghe")
                        .isDefault(true)
                        .build(),
                Address.builder()
                        .id("addr-2")
                        .userId("user-1")
                        .fullName("Tran Thi B")
                        .phone("0909000002")
                        .address("34 Lê Lợi")
                        .city("Da Nang")
                        .district("Hai Chau")
                        .ward("Thach Thang")
                        .isDefault(false)
                        .build()
        ));

        List<AddressDTO> result = useCase.execute("user-1");

        assertEquals(2, result.size());
        assertEquals("addr-1", result.get(0).id());
        assertEquals("addr-2", result.get(1).id());
        verify(addressRepository).findByUserId("user-1");
    }

    @Test
    void executeThrowsVietnameseExceptionWhenUserIsInactive() {
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        GetAddressesUseCase useCase = new GetAddressesUseCase(addressRepository, userFacade);

        when(userFacade.isUserActive("user-1")).thenReturn(false);

        ActionNotAllowedException ex = assertThrows(ActionNotAllowedException.class,
                () -> useCase.execute("user-1"));

        assertEquals("Tài khoản không hoạt động, không thể xem địa chỉ.", ex.getMessage());
        verify(addressRepository, never()).findByUserId("user-1");
    }
}
