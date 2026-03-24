package fit.iuh.kttkpm_nhom15_be.promotions.application.usecases;

import fit.iuh.kttkpm_nhom15_be.promotions.application.commands.CreatePromotionCommand;
import fit.iuh.kttkpm_nhom15_be.promotions.application.commands.UpdatePromotionCommand;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.PromotionResponseDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.support.PromotionConfigValidator;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateAndUpdatePromotionUseCaseTest {

    @Test
    void createPromotionNormalizesCodeAndPersistsNewPromotion() {
        PromotionRepository promotionRepository = Mockito.mock(PromotionRepository.class);
        PromotionConfigValidator validator = Mockito.mock(PromotionConfigValidator.class);
        CreatePromotionUseCase useCase = new CreatePromotionUseCase(promotionRepository, validator);
        CreatePromotionCommand command = createCommand();

        when(validator.normalizeCode(" sale10 ")).thenReturn("SALE10");
        when(promotionRepository.findByCode("SALE10")).thenReturn(Optional.empty());
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> {
            Promotion promotion = invocation.getArgument(0);
            promotion.setId("promo-1");
            return promotion;
        });

        PromotionResponseDTO response = useCase.execute(command);

        verify(validator).validateForSave(
            command.getType(),
            command.getConfig(),
            command.getStartDate(),
            command.getEndDate(),
            command.getUsageLimit()
        );
        assertEquals("promo-1", response.id());
        assertEquals("SALE10", response.code());
        assertEquals(0, response.usedCount());
        assertEquals(true, response.active());
    }

    @Test
    void createPromotionRejectsDuplicateCodes() {
        PromotionRepository promotionRepository = Mockito.mock(PromotionRepository.class);
        PromotionConfigValidator validator = Mockito.mock(PromotionConfigValidator.class);
        CreatePromotionUseCase useCase = new CreatePromotionUseCase(promotionRepository, validator);

        when(validator.normalizeCode(" sale10 ")).thenReturn("SALE10");
        when(promotionRepository.findByCode("SALE10")).thenReturn(Optional.of(Promotion.builder().id("promo-1").build()));

        assertThrows(DataIntegrityViolationException.class, () -> useCase.execute(createCommand()));
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void updatePromotionMutatesExistingPromotionAndSavesIt() {
        PromotionRepository promotionRepository = Mockito.mock(PromotionRepository.class);
        PromotionConfigValidator validator = Mockito.mock(PromotionConfigValidator.class);
        UpdatePromotionUseCase useCase = new UpdatePromotionUseCase(promotionRepository, validator);
        Promotion existing = Promotion.builder()
            .id("promo-1")
            .code("OLD")
            .name("Old promo")
            .type(PromotionType.ORDER_DISCOUNT)
            .config(Map.of("discountAmount", 5, "minOrderValue", 0))
            .isActive(false)
            .build();
        UpdatePromotionCommand command = UpdatePromotionCommand.builder()
            .promotionId("promo-1")
            .code(" sale20 ")
            .name("Sale 20")
            .type(PromotionType.ORDER_DISCOUNT)
            .config(Map.of("discountAmount", 20, "minOrderValue", 100))
            .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
            .endDate(LocalDateTime.of(2026, 3, 31, 23, 59))
            .usageLimit(30)
            .active(true)
            .build();

        when(promotionRepository.findById("promo-1")).thenReturn(Optional.of(existing));
        when(validator.normalizeCode(" sale20 ")).thenReturn("SALE20");
        when(promotionRepository.findByCode("SALE20")).thenReturn(Optional.of(existing));
        when(promotionRepository.save(existing)).thenReturn(existing);

        PromotionResponseDTO response = useCase.execute(command);

        assertEquals("SALE20", existing.getCode());
        assertEquals("Sale 20", existing.getName());
        assertEquals(30, existing.getUsageLimit());
        assertEquals("SALE20", response.code());
        assertEquals(true, response.active());
    }

    @Test
    void updatePromotionRejectsDuplicateCodeOwnedByAnotherPromotion() {
        PromotionRepository promotionRepository = Mockito.mock(PromotionRepository.class);
        PromotionConfigValidator validator = Mockito.mock(PromotionConfigValidator.class);
        UpdatePromotionUseCase useCase = new UpdatePromotionUseCase(promotionRepository, validator);
        Promotion existing = Promotion.builder().id("promo-1").code("OLD").build();
        Promotion conflicting = Promotion.builder().id("promo-2").code("SALE20").build();

        when(promotionRepository.findById("promo-1")).thenReturn(Optional.of(existing));
        when(validator.normalizeCode(" sale20 ")).thenReturn("SALE20");
        when(promotionRepository.findByCode("SALE20")).thenReturn(Optional.of(conflicting));

        assertThrows(DataIntegrityViolationException.class, () -> useCase.execute(UpdatePromotionCommand.builder()
            .promotionId("promo-1")
            .code(" sale20 ")
            .name("Sale 20")
            .type(PromotionType.ORDER_DISCOUNT)
            .config(Map.of("discountAmount", 20, "minOrderValue", 100))
            .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
            .endDate(LocalDateTime.of(2026, 3, 31, 23, 59))
            .usageLimit(30)
            .active(true)
            .build()));
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    private CreatePromotionCommand createCommand() {
        return CreatePromotionCommand.builder()
            .code(" sale10 ")
            .name("Sale 10")
            .type(PromotionType.ORDER_DISCOUNT)
            .config(Map.of("discountAmount", 10, "minOrderValue", 100))
            .startDate(LocalDateTime.of(2026, 3, 1, 0, 0))
            .endDate(LocalDateTime.of(2026, 3, 31, 23, 59))
            .usageLimit(50)
            .active(true)
            .build();
    }
}
