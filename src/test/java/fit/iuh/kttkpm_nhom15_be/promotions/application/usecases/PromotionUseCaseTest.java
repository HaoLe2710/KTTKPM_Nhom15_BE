package fit.iuh.kttkpm_nhom15_be.promotions.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.application.interfaces.OrderFacade;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromotionUseCaseTest {

    @Test
    void deactivatePromotionIsIdempotentWhenAlreadyInactive() {
        PromotionRepository promotionRepository = Mockito.mock(PromotionRepository.class);
        Promotion promotion = samplePromotion();
        promotion.setActive(false);
        when(promotionRepository.findById("promo-1")).thenReturn(Optional.of(promotion));

        DeactivatePromotionUseCase useCase = new DeactivatePromotionUseCase(promotionRepository);

        assertFalse(useCase.execute("promo-1").active());
        verify(promotionRepository, never()).save(Mockito.any());
    }

    @Test
    void deletePromotionThrowsConflictWhenItHasBeenUsed() {
        PromotionRepository promotionRepository = Mockito.mock(PromotionRepository.class);
        OrderFacade orderFacade = Mockito.mock(OrderFacade.class);
        when(promotionRepository.findById("promo-1")).thenReturn(Optional.of(samplePromotion()));
        when(orderFacade.hasPromotionBeenUsed("promo-1")).thenReturn(true);

        DeletePromotionUseCase useCase = new DeletePromotionUseCase(promotionRepository, orderFacade);

        assertThrows(DataIntegrityViolationException.class, () -> useCase.execute("promo-1"));
        verify(promotionRepository, never()).deleteById("promo-1");
    }

    @Test
    void deletePromotionSucceedsWhenUnused() {
        PromotionRepository promotionRepository = Mockito.mock(PromotionRepository.class);
        OrderFacade orderFacade = Mockito.mock(OrderFacade.class);
        when(promotionRepository.findById("promo-1")).thenReturn(Optional.of(samplePromotion()));
        when(orderFacade.hasPromotionBeenUsed("promo-1")).thenReturn(false);

        DeletePromotionUseCase useCase = new DeletePromotionUseCase(promotionRepository, orderFacade);
        useCase.execute("promo-1");

        verify(promotionRepository).deleteById("promo-1");
    }

    private Promotion samplePromotion() {
        return Promotion.builder()
            .id("promo-1")
            .code("PROMO")
            .name("Promo")
            .type(PromotionType.ORDER_DISCOUNT)
            .config(Map.of("minOrderValue", 0, "discountAmount", 10))
            .startDate(LocalDateTime.now().minusDays(1))
            .endDate(LocalDateTime.now().plusDays(1))
            .isActive(true)
            .build();
    }
}
