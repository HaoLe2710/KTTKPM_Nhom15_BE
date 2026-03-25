package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Variant;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.VariantOption;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantOptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogProductUseCaseTest {

    @Test
    void createCompositeProductPersistsProductVariantsAndVariantOptions() {
        ProductRepository productRepository = Mockito.mock(ProductRepository.class);
        VariantRepository variantRepository = Mockito.mock(VariantRepository.class);
        VariantOptionRepository variantOptionRepository = Mockito.mock(VariantOptionRepository.class);
        CreateCompositeProductUseCase useCase = new CreateCompositeProductUseCase(productRepository, variantRepository, variantOptionRepository);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId("product-1");
            return product;
        });
        when(variantRepository.save(any(Variant.class))).thenAnswer(invocation -> {
            Variant variant = invocation.getArgument(0);
            variant.setId("variant-" + variant.getSku().toLowerCase());
            return variant;
        });
        when(variantOptionRepository.save(any(VariantOption.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String productId = useCase.execute(CompositeProductRequestDTO.builder()
            .typeId("type-1")
            .name("Running Shoe")
            .descriptionMd("Lightweight")
            .isCustomizable(false)
            .variants(List.of(
                CompositeProductRequestDTO.VariantRequestDTO.builder()
                    .sku("SKU-1")
                    .price(new BigDecimal("99.99"))
                    .stockQuantity(10)
                    .options(List.of(
                        CompositeProductRequestDTO.OptionAssignmentDTO.builder().optionId("option-1").valueId("value-1").build(),
                        CompositeProductRequestDTO.OptionAssignmentDTO.builder().optionId("option-2").valueId("value-2").build()
                    ))
                    .build()
            ))
            .build());

        ArgumentCaptor<Product> savedProduct = ArgumentCaptor.forClass(Product.class);
        ArgumentCaptor<Variant> savedVariant = ArgumentCaptor.forClass(Variant.class);
        ArgumentCaptor<VariantOption> savedVariantOption = ArgumentCaptor.forClass(VariantOption.class);

        verify(productRepository).save(savedProduct.capture());
        verify(variantRepository).save(savedVariant.capture());
        verify(variantOptionRepository, Mockito.times(2)).save(savedVariantOption.capture());

        assertEquals("product-1", productId);
        assertTrue(savedProduct.getValue().getSlug().startsWith("running-shoe-"));
        assertTrue(savedProduct.getValue().isActive());
        assertEquals("product-1", savedVariant.getValue().getProductId());
        assertEquals(List.of("value-1", "value-2"), savedVariantOption.getAllValues().stream()
            .map(VariantOption::getOptionValueId)
            .toList());
    }

    @Test
    void getProductDetailsReturnsRepositoryResult() {
        ProductRepository productRepository = Mockito.mock(ProductRepository.class);
        GetProductDetailsUseCase useCase = new GetProductDetailsUseCase(productRepository);
        Optional<Product> product = Optional.of(Product.builder().id("product-1").name("Running Shoe").build());

        when(productRepository.findById("product-1")).thenReturn(product);

        assertSame(product, useCase.execute("product-1"));
    }

    @Test
    void listProductsSummaryDelegatesFiltersAndPagination() {
        ProductRepository productRepository = Mockito.mock(ProductRepository.class);
        ListProductsSummaryUseCase useCase = new ListProductsSummaryUseCase(productRepository);
        Page<ProductSummaryDTO> expectedPage = new PageImpl<>(List.of(
            ProductSummaryDTO.builder().id("product-1").name("Running Shoe").lowestPrice(new BigDecimal("99.99")).build()
        ));

        when(productRepository.findProductsSummary("type-1", new BigDecimal("10.00"), new BigDecimal("100.00"), 2, 5))
            .thenReturn(expectedPage);

        Page<ProductSummaryDTO> result = useCase.execute("type-1", new BigDecimal("10.00"), new BigDecimal("100.00"), 2, 5);

        assertSame(expectedPage, result);
    }

    @Test
    void updateVariantPricingPatchesPriceAndStockWhenPriceIsPresent() {
        VariantRepository variantRepository = Mockito.mock(VariantRepository.class);
        UpdateVariantPricingUseCase useCase = new UpdateVariantPricingUseCase(variantRepository);
        UpdateVariantPricingUseCase.PatchVariantRequest request = new UpdateVariantPricingUseCase.PatchVariantRequest();
        request.setPrice(new BigDecimal("149.99"));
        request.setAddedStock(7);

        useCase.execute("variant-1", request);

        verify(variantRepository).patchPriceAndStock("variant-1", new BigDecimal("149.99"), 7);
    }

    @Test
    void updateVariantPricingRejectsRequestsWithoutExplicitPrice() {
        VariantRepository variantRepository = Mockito.mock(VariantRepository.class);
        UpdateVariantPricingUseCase useCase = new UpdateVariantPricingUseCase(variantRepository);
        UpdateVariantPricingUseCase.PatchVariantRequest request = new UpdateVariantPricingUseCase.PatchVariantRequest();
        request.setAddedStock(7);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute("variant-1", request));
        verify(variantRepository, never()).patchPriceAndStock(any(), any(), Mockito.anyInt());
    }
}
