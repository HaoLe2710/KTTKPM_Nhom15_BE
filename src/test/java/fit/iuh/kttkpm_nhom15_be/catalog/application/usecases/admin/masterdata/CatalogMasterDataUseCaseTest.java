package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Option;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.OptionValue;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.ProductType;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductTypeRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantOptionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CatalogMasterDataUseCaseTest {

    @Test
    void createProductTypeRejectsDuplicateCode() {
        ProductTypeRepository productTypeRepository = mock(ProductTypeRepository.class);
        when(productTypeRepository.existsByCode("MAKEUP")).thenReturn(true);

        CreateProductTypeUseCase useCase = new CreateProductTypeUseCase(productTypeRepository);
        ProductTypeRequest request = ProductTypeRequest.builder().code("MAKEUP").name("Make up").build();

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
        verify(productTypeRepository, never()).save(any(ProductType.class));
    }

    @Test
    void createOptionRejectsDuplicateCode() {
        OptionRepository optionRepository = mock(OptionRepository.class);
        OptionValueRepository optionValueRepository = mock(OptionValueRepository.class);
        when(optionRepository.existsByCode("SIZE")).thenReturn(true);

        CreateOptionUseCase useCase = new CreateOptionUseCase(optionRepository, optionValueRepository);
        OptionRequest request = OptionRequest.builder().code("SIZE").name("Size").values(List.of("S", "M")).build();

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
        verify(optionRepository, never()).save(any(Option.class));
        verify(optionValueRepository, never()).save(any(OptionValue.class));
    }

    @Test
    void updateProductTypeRejectsDuplicateCodeFromOtherRecord() {
        ProductTypeRepository productTypeRepository = mock(ProductTypeRepository.class);
        when(productTypeRepository.findById("type-1")).thenReturn(Optional.of(ProductType.builder().id("type-1").code("OLD").name("Old").build()));
        when(productTypeRepository.findByCode("DUP")).thenReturn(Optional.of(ProductType.builder().id("type-2").code("DUP").name("Dup").build()));

        UpdateProductTypeUseCase useCase = new UpdateProductTypeUseCase(productTypeRepository);
        ProductTypeRequest request = ProductTypeRequest.builder().code("DUP").name("New name").build();

        assertThrows(IllegalArgumentException.class, () -> useCase.execute("type-1", request));
        verify(productTypeRepository, never()).save(any(ProductType.class));
    }

    @Test
    void deleteProductTypeRejectsWhenUsedByProducts() {
        ProductTypeRepository productTypeRepository = mock(ProductTypeRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        when(productTypeRepository.findById("type-1")).thenReturn(Optional.of(ProductType.builder().id("type-1").build()));
        when(productRepository.existsByTypeId("type-1")).thenReturn(true);

        DeleteProductTypeUseCase useCase = new DeleteProductTypeUseCase(productTypeRepository, productRepository);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute("type-1"));
        verify(productTypeRepository, never()).deleteById("type-1");
    }

    @Test
    void deleteOptionRejectsWhenReferencedByVariants() {
        OptionRepository optionRepository = mock(OptionRepository.class);
        OptionValueRepository optionValueRepository = mock(OptionValueRepository.class);
        VariantOptionRepository variantOptionRepository = mock(VariantOptionRepository.class);

        when(optionRepository.findById("opt-1")).thenReturn(Optional.of(Option.builder().id("opt-1").build()));
        when(optionValueRepository.findByOptionId("opt-1")).thenReturn(List.of(
                OptionValue.builder().id("val-1").optionId("opt-1").isActive(true).build()
        ));
        when(variantOptionRepository.existsByOptionValueIds(List.of("val-1"))).thenReturn(true);

        DeleteOptionUseCase useCase = new DeleteOptionUseCase(optionRepository, optionValueRepository, variantOptionRepository);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute("opt-1"));
        verify(optionValueRepository, never()).deleteByOptionId("opt-1");
        verify(optionRepository, never()).deleteById("opt-1");
    }
}
