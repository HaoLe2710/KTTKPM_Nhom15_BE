package fit.iuh.kttkpm_nhom15_be.promotions.application.usecases;

import fit.iuh.kttkpm_nhom15_be.promotions.application.commands.UpdatePromotionCommand;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.PromotionResponseDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.support.PromotionConfigValidator;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.exceptions.PromotionNotFoundException;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePromotionUseCase {
    private final PromotionRepository promotionRepository;
    private final PromotionConfigValidator promotionConfigValidator;

    @Transactional
    public PromotionResponseDTO execute(UpdatePromotionCommand command) {
        Promotion existingPromotion = promotionRepository.findById(command.getPromotionId())
            .orElseThrow(() -> new PromotionNotFoundException(command.getPromotionId()));

        String normalizedCode = promotionConfigValidator.normalizeCode(command.getCode());
        promotionConfigValidator.validateForSave(
            command.getType(),
            command.getConfig(),
            command.getStartDate(),
            command.getEndDate(),
            command.getUsageLimit()
        );

        promotionRepository.findByCode(normalizedCode)
            .filter(found -> !found.getId().equals(existingPromotion.getId()))
            .ifPresent(found -> {
                throw new DataIntegrityViolationException("Promotion code already exists: " + normalizedCode);
            });

        existingPromotion.setCode(normalizedCode);
        existingPromotion.setName(command.getName());
        existingPromotion.setType(command.getType());
        existingPromotion.setConfig(command.getConfig());
        existingPromotion.setStartDate(command.getStartDate());
        existingPromotion.setEndDate(command.getEndDate());
        existingPromotion.setUsageLimit(command.getUsageLimit());
        existingPromotion.setActive(command.isActive());

        return PromotionResponseDTO.from(promotionRepository.save(existingPromotion));
    }
}
