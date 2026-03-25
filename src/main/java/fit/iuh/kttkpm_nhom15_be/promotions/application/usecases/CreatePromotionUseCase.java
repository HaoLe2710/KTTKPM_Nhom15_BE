package fit.iuh.kttkpm_nhom15_be.promotions.application.usecases;

import fit.iuh.kttkpm_nhom15_be.promotions.application.commands.CreatePromotionCommand;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.PromotionResponseDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.support.PromotionConfigValidator;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePromotionUseCase {
    private final PromotionRepository promotionRepository;
    private final PromotionConfigValidator promotionConfigValidator;

    @Transactional
    public PromotionResponseDTO execute(CreatePromotionCommand command) {
        String normalizedCode = promotionConfigValidator.normalizeCode(command.getCode());
        promotionConfigValidator.validateForSave(
            command.getType(),
            command.getConfig(),
            command.getStartDate(),
            command.getEndDate(),
            command.getUsageLimit()
        );

        promotionRepository.findByCode(normalizedCode).ifPresent(existing -> {
            throw new DataIntegrityViolationException("Promotion code already exists: " + normalizedCode);
        });

        Promotion promotion = Promotion.builder()
            .code(normalizedCode)
            .name(command.getName())
            .type(command.getType())
            .config(command.getConfig())
            .startDate(command.getStartDate())
            .endDate(command.getEndDate())
            .usageLimit(command.getUsageLimit())
            .usedCount(0)
            .isActive(command.isActive())
            .build();

        return PromotionResponseDTO.from(promotionRepository.save(promotion));
    }
}
