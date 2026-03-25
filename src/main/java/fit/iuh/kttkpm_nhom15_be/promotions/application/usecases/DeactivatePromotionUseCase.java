package fit.iuh.kttkpm_nhom15_be.promotions.application.usecases;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.PromotionResponseDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.exceptions.PromotionNotFoundException;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivatePromotionUseCase {
    private final PromotionRepository promotionRepository;

    @Transactional
    public PromotionResponseDTO execute(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new PromotionNotFoundException(promotionId));

        if (!promotion.isActive()) {
            return PromotionResponseDTO.from(promotion);
        }

        promotion.setActive(false);
        return PromotionResponseDTO.from(promotionRepository.save(promotion));
    }
}
