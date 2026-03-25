package fit.iuh.kttkpm_nhom15_be.promotions.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.application.interfaces.OrderFacade;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.exceptions.PromotionNotFoundException;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePromotionUseCase {
    private final PromotionRepository promotionRepository;
    private final OrderFacade orderFacade;

    @Transactional
    public void execute(String promotionId) {
        promotionRepository.findById(promotionId)
            .orElseThrow(() -> new PromotionNotFoundException(promotionId));

        if (orderFacade.hasPromotionBeenUsed(promotionId)) {
            throw new DataIntegrityViolationException("Promotion has already been used and cannot be deleted.");
        }

        promotionRepository.deleteById(promotionId);
    }
}
