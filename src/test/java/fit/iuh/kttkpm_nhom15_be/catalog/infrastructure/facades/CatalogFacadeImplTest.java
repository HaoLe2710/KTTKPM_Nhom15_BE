package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.exceptions.ProductUnavailableException;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Variant;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.StockRestoreItem;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogFacadeImplTest {

    @Test
    void validateAndGetSnapshotsThrowsWhenRequestedQuantityExceedsStock() {
        VariantRepository variantRepository = Mockito.mock(VariantRepository.class);
        ProductRepository productRepository = Mockito.mock(ProductRepository.class);
        OptionValueRepository optionValueRepository = Mockito.mock(OptionValueRepository.class);
        OptionRepository optionRepository = Mockito.mock(OptionRepository.class);
        CatalogFacadeImpl facade = new CatalogFacadeImpl(
            variantRepository,
            productRepository,
            optionValueRepository,
            optionRepository
        );

        when(variantRepository.findById("variant-1")).thenReturn(Optional.of(Variant.builder()
            .id("variant-1")
            .productId("product-1")
            .sku("SKU-1")
            .price(BigDecimal.valueOf(120_000))
            .stockQuantity(1)
            .isActive(true)
            .build()));

        assertThrows(ProductUnavailableException.class, () -> facade.validateAndGetSnapshots(List.of(
            CartItemDTO.builder()
                .variantId("variant-1")
                .quantity(2)
                .build()
        )));
    }

    @Test
    void deductStockThrowsWhenAtomicUpdateFails() {
        VariantRepository variantRepository = Mockito.mock(VariantRepository.class);
        ProductRepository productRepository = Mockito.mock(ProductRepository.class);
        OptionValueRepository optionValueRepository = Mockito.mock(OptionValueRepository.class);
        OptionRepository optionRepository = Mockito.mock(OptionRepository.class);
        CatalogFacadeImpl facade = new CatalogFacadeImpl(
            variantRepository,
            productRepository,
            optionValueRepository,
            optionRepository
        );

        when(variantRepository.deductStock("variant-1", 2)).thenReturn(false);

        assertThrows(ProductUnavailableException.class, () -> facade.deductStock(List.of(
            CartItemDTO.builder()
                .variantId("variant-1")
                .quantity(2)
                .build()
        )));
    }

    @Test
    void restoreStockDelegatesToRepository() {
        VariantRepository variantRepository = Mockito.mock(VariantRepository.class);
        ProductRepository productRepository = Mockito.mock(ProductRepository.class);
        OptionValueRepository optionValueRepository = Mockito.mock(OptionValueRepository.class);
        OptionRepository optionRepository = Mockito.mock(OptionRepository.class);
        CatalogFacadeImpl facade = new CatalogFacadeImpl(
            variantRepository,
            productRepository,
            optionValueRepository,
            optionRepository
        );

        when(variantRepository.restoreStock("variant-1", 2)).thenReturn(true);

        facade.restoreStock(List.of(new StockRestoreItem("variant-1", 2)));

        verify(variantRepository).restoreStock("variant-1", 2);
    }

    @Test
    void restoreStockThrowsWhenVariantCannotBeUpdated() {
        VariantRepository variantRepository = Mockito.mock(VariantRepository.class);
        ProductRepository productRepository = Mockito.mock(ProductRepository.class);
        OptionValueRepository optionValueRepository = Mockito.mock(OptionValueRepository.class);
        OptionRepository optionRepository = Mockito.mock(OptionRepository.class);
        CatalogFacadeImpl facade = new CatalogFacadeImpl(
            variantRepository,
            productRepository,
            optionValueRepository,
            optionRepository
        );

        when(variantRepository.restoreStock("variant-1", 2)).thenReturn(false);

        assertThrows(ApiValidationException.class, () -> facade.restoreStock(List.of(
            new StockRestoreItem("variant-1", 2)
        )));
    }
}
