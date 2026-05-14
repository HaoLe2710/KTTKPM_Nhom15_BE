package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Option;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.OptionValue;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.ProductType;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductTypeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogMasterDataUseCaseTest {

    @Test
    void createOptionPersistsOptionAndItsValues() {
        OptionRepository optionRepository = Mockito.mock(OptionRepository.class);
        OptionValueRepository optionValueRepository = Mockito.mock(OptionValueRepository.class);
        CreateOptionUseCase useCase = new CreateOptionUseCase(optionRepository, optionValueRepository);

        when(optionRepository.save(any(Option.class))).thenAnswer(invocation -> {
            Option option = invocation.getArgument(0);
            option.setId("option-1");
            return option;
        });
        when(optionValueRepository.save(any(OptionValue.class))).thenAnswer(invocation -> {
            OptionValue value = invocation.getArgument(0);
            value.setId("value-" + value.getValue().toLowerCase());
            return value;
        });

        OptionResponse response = useCase.execute(OptionRequest.builder()
            .code("size")
            .name("Size")
            .values(List.of("S", "M"))
            .build());

        assertEquals("option-1", response.getId());
        assertEquals(2, response.getValues().size());
        assertEquals("value-s", response.getValues().get(0).getId());
        assertEquals("S", response.getValues().get(0).getValue());
        verify(optionValueRepository, Mockito.times(2)).save(any(OptionValue.class));
    }

    @Test
    void createProductTypeReturnsMappedSavedEntity() {
        ProductTypeRepository repository = Mockito.mock(ProductTypeRepository.class);
        CreateProductTypeUseCase useCase = new CreateProductTypeUseCase(repository);

        when(repository.save(any(ProductType.class))).thenAnswer(invocation -> {
            ProductType productType = invocation.getArgument(0);
            productType.setId("type-1");
            return productType;
        });

        ProductTypeResponse response = useCase.execute(ProductTypeRequest.builder()
            .code("shoe")
            .name("Shoes")
            .build());

        assertEquals("type-1", response.getId());
        assertEquals("shoe", response.getCode());
        assertEquals("Shoes", response.getName());
    }

    @Test
    void getOptionsGroupsActiveValuesByOptionId() {
        OptionRepository optionRepository = Mockito.mock(OptionRepository.class);
        OptionValueRepository optionValueRepository = Mockito.mock(OptionValueRepository.class);
        GetOptionsUseCase useCase = new GetOptionsUseCase(optionRepository, optionValueRepository);

        when(optionRepository.findAll()).thenReturn(List.of(
            Option.builder().id("option-1").code("size").name("Size").build(),
            Option.builder().id("option-2").code("color").name("Color").build()
        ));
        when(optionValueRepository.findAll()).thenReturn(List.of(
            OptionValue.builder().id("value-1").optionId("option-1").value("S").isActive(true).build(),
            OptionValue.builder().id("value-2").optionId("option-1").value("M").isActive(false).build(),
            OptionValue.builder().id("value-3").optionId("option-2").value("Red").isActive(true).build()
        ));

        List<OptionResponse> responses = useCase.execute();

        assertEquals(2, responses.size());
        assertEquals(1, responses.get(0).getValues().size());
        assertEquals("S", responses.get(0).getValues().get(0).getValue());
        assertEquals(1, responses.get(1).getValues().size());
        assertEquals("Red", responses.get(1).getValues().get(0).getValue());
    }

    @Test
    void getProductTypesMapsAllRepositoryResults() {
        ProductTypeRepository repository = Mockito.mock(ProductTypeRepository.class);
        GetProductTypesUseCase useCase = new GetProductTypesUseCase(repository);

        when(repository.findAll()).thenReturn(List.of(
            ProductType.builder().id("type-1").code("shoe").name("Shoes").build(),
            ProductType.builder().id("type-2").code("bag").name("Bags").build()
        ));

        List<ProductTypeResponse> responses = useCase.execute();

        assertEquals(2, responses.size());
        assertEquals("shoe", responses.get(0).getCode());
        assertEquals("Bags", responses.get(1).getName());
        assertTrue(responses.stream().map(ProductTypeResponse::getId).toList().contains("type-1"));
    }
}
