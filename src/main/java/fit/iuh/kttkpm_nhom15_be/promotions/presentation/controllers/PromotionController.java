package fit.iuh.kttkpm_nhom15_be.promotions.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.promotions.application.commands.CreatePromotionCommand;
import fit.iuh.kttkpm_nhom15_be.promotions.application.commands.UpdatePromotionCommand;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.AppliedPromotionDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartItemDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.PromotionResponseDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.interfaces.PromotionFacade;
import fit.iuh.kttkpm_nhom15_be.promotions.application.usecases.CreatePromotionUseCase;
import fit.iuh.kttkpm_nhom15_be.promotions.application.usecases.DeactivatePromotionUseCase;
import fit.iuh.kttkpm_nhom15_be.promotions.application.usecases.DeletePromotionUseCase;
import fit.iuh.kttkpm_nhom15_be.promotions.application.usecases.ListPromotionsUseCase;
import fit.iuh.kttkpm_nhom15_be.promotions.application.usecases.UpdatePromotionUseCase;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import fit.iuh.kttkpm_nhom15_be.promotions.presentation.requests.CreatePromotionRequest;
import fit.iuh.kttkpm_nhom15_be.promotions.presentation.requests.UpdatePromotionRequest;
import fit.iuh.kttkpm_nhom15_be.promotions.presentation.requests.ValidatePromotionRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final CreatePromotionUseCase createPromotionUseCase;
    private final UpdatePromotionUseCase updatePromotionUseCase;
    private final DeactivatePromotionUseCase deactivatePromotionUseCase;
    private final DeletePromotionUseCase deletePromotionUseCase;
    private final ListPromotionsUseCase listPromotionsUseCase;
    private final PromotionFacade promotionFacade;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromotionResponseDTO>> listPromotions(
        @RequestParam(required = false) PromotionType type,
        @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        return ResponseEntity.ok(listPromotionsUseCase.execute(type, activeOnly));
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponseDTO>> listActivePromotions(
        @RequestParam(required = false) PromotionType type
    ) {
        if (type == PromotionType.ORDER_DISCOUNT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order vouchers are scoped to customer accounts.");
        }

        List<PromotionResponseDTO> promotions = listPromotionsUseCase.execute(type, true);
        if (type == null) {
            promotions = promotions.stream()
                .filter(promotion -> promotion.type() != PromotionType.ORDER_DISCOUNT)
                .toList();
        }
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/my-vouchers")
    public ResponseEntity<List<PromotionResponseDTO>> listMyVouchers(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(listPromotionsUseCase.executeAssignedOrderVouchers(userId));
    }

    @PostMapping("/validate")
    public ResponseEntity<AppliedPromotionDTO> validatePromotion(
        @AuthenticationPrincipal String userId,
        @Valid @RequestBody ValidatePromotionRequest request
    ) {
        OrderCartDTO cart = new OrderCartDTO(
            request.getSubtotal(),
            request.getItems().stream()
                .map(item -> new OrderCartItemDTO(
                    item.getVariantId(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal()
                ))
                .toList()
        );

        return ResponseEntity.ok(promotionFacade.validateOrderDiscountAndCalculate(request.getPromotionCode(), cart, userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponseDTO> createPromotion(@Valid @RequestBody CreatePromotionRequest request) {
        CreatePromotionCommand command = CreatePromotionCommand.builder()
            .code(request.getCode())
            .name(request.getName())
            .type(request.getType())
            .config(request.getConfig())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .usageLimit(request.getUsageLimit())
            .active(request.isActive())
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(createPromotionUseCase.execute(command));
    }

    @PutMapping("/{promotionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponseDTO> updatePromotion(
        @PathVariable String promotionId,
        @Valid @RequestBody UpdatePromotionRequest request
    ) {
        UpdatePromotionCommand command = UpdatePromotionCommand.builder()
            .promotionId(promotionId)
            .code(request.getCode())
            .name(request.getName())
            .type(request.getType())
            .config(request.getConfig())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .usageLimit(request.getUsageLimit())
            .active(request.isActive())
            .build();
        return ResponseEntity.ok(updatePromotionUseCase.execute(command));
    }

    @PostMapping("/{promotionId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponseDTO> deactivatePromotion(@PathVariable String promotionId) {
        return ResponseEntity.ok(deactivatePromotionUseCase.execute(promotionId));
    }

    @DeleteMapping("/{promotionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deletePromotion(@PathVariable String promotionId) {
        deletePromotionUseCase.execute(promotionId);
        return ResponseEntity.ok(new MessageResponse("Khuyến mãi đã được xóa thành công"));
    }
}
