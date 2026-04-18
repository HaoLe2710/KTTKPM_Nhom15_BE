package fit.iuh.kttkpm_nhom15_be.users.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.AddAddressCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateAddressCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.AddressDTO;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.AddAddressUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.DeleteAddressUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.UpdateAddressUseCase;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.AddAddressRequest;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.UpdateAddressRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddAddressUseCase addAddressUseCase;
    private final UpdateAddressUseCase updateAddressUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;

    // 1. Thêm địa chỉ mới
    @PostMapping
    public ResponseEntity<AddressDTO> addAddress(
            @AuthenticationPrincipal String userEmail,
            @Valid @RequestBody AddAddressRequest request
    ) {
        AddressDTO response = addAddressUseCase.execute(new AddAddressCommand(
                userEmail,
                request.getReceiverName(),
                request.getPhone(),
                request.getAddress(),
                request.getCity(),
                request.getDistrict(),
                request.getWard(),
                request.isDefault()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Cập nhật địa chỉ
    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable String id,
            @AuthenticationPrincipal String userEmail,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        AddressDTO response = updateAddressUseCase.execute(new UpdateAddressCommand(
                id,
                userEmail,
                request.getReceiverName(),
                request.getPhone(),
                request.getAddress(),
                request.getCity(),
                request.getDistrict(),
                request.getWard(),
                request.isDefault()
        ));
        return ResponseEntity.ok(response);
    }

    // 3. Xóa địa chỉ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable String id,
            @AuthenticationPrincipal String userEmail
    ) {
        deleteAddressUseCase.execute(id, userEmail);
        return ResponseEntity.ok().build();
    }

}