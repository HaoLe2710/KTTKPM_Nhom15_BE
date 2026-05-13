package fit.iuh.kttkpm_nhom15_be.promotions.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.promotions.application.commands.CreatePromotionCommand;
import fit.iuh.kttkpm_nhom15_be.promotions.application.commands.UpdatePromotionCommand;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.PromotionResponseDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.usecases.CreatePromotionUseCase;
import fit.iuh.kttkpm_nhom15_be.promotions.application.usecases.DeactivatePromotionUseCase;
import fit.iuh.kttkpm_nhom15_be.promotions.application.usecases.DeletePromotionUseCase;
import fit.iuh.kttkpm_nhom15_be.promotions.application.usecases.UpdatePromotionUseCase;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import fit.iuh.kttkpm_nhom15_be.promotions.presentation.requests.CreatePromotionRequest;
import fit.iuh.kttkpm_nhom15_be.promotions.presentation.requests.UpdatePromotionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final CreatePromotionUseCase createPromotionUseCase;
    private final UpdatePromotionUseCase updatePromotionUseCase;
    private final DeactivatePromotionUseCase deactivatePromotionUseCase;
    private final DeletePromotionUseCase deletePromotionUseCase;

    @PostMapping
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
    public ResponseEntity<PromotionResponseDTO> deactivatePromotion(@PathVariable String promotionId) {
        return ResponseEntity.ok(deactivatePromotionUseCase.execute(promotionId));
    }

    @DeleteMapping("/{promotionId}")
    public ResponseEntity<MessageResponse> deletePromotion(@PathVariable String promotionId) {
        deletePromotionUseCase.execute(promotionId);
        return ResponseEntity.ok(new MessageResponse("Khuyen mai da duoc xoa thanh cong"));
    }
}
