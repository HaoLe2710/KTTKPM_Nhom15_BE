package fit.iuh.kttkpm_nhom15_be.users.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.AddAddressCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateAddressCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.AddressDTO;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.AddAddressUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.DeleteAddressUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.GetAddressesUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.UpdateAddressUseCase;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.AddressNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.AddAddressRequest;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.UpdateAddressRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddAddressUseCase addAddressUseCase;
    private final UpdateAddressUseCase updateAddressUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;
    private final GetAddressesUseCase getAddressesUseCase;

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAddresses(@RequestHeader("X-User-Id") String userId) {
        List<AddressDTO> response = getAddressesUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AddressDTO> addAddress(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddAddressRequest request
    ) {
        AddressDTO response = addAddressUseCase.execute(new AddAddressCommand(
                userId,
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

    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        AddressDTO response = updateAddressUseCase.execute(new UpdateAddressCommand(
                id,
                userId,
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId
    ) {
        deleteAddressUseCase.execute(id, userId);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAddressNotFound(AddressNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ActionNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleActionNotAllowed(ActionNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
}