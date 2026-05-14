package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.*;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CatalogDataMapper {
    ProductJpaEntity toJpaEntity(Product domain);
    Product toDomainModel(ProductJpaEntity entity);

    VariantJpaEntity toJpaEntity(Variant domain);
    Variant toDomainModel(VariantJpaEntity entity);

    // Auto map các Entity con khác nếu được gọi trực tiếp
    ProductTypeJpaEntity toJpaEntity(ProductType domain);
    ProductType toDomainModel(ProductTypeJpaEntity entity);

    OptionJpaEntity toJpaEntity(Option domain);
    Option toDomainModel(OptionJpaEntity entity);

    OptionValueJpaEntity toJpaEntity(OptionValue domain);
    OptionValue toDomainModel(OptionValueJpaEntity entity);

    VariantOptionJpaEntity toJpaEntity(VariantOption domain);
    VariantOption toDomainModel(VariantOptionJpaEntity entity);

    MediaJpaEntity toJpaEntity(Media domain);
    Media toDomainModel(MediaJpaEntity entity);

    @AfterMapping
    default void linkProductChildren(@MappingTarget ProductJpaEntity productJpa) {
        if (productJpa.getVariants() != null) productJpa.getVariants().forEach(v -> v.setProduct(productJpa));
        if (productJpa.getMedia() != null) productJpa.getMedia().forEach(m -> m.setProduct(productJpa));
    }

    @AfterMapping
    default void linkVariantChildren(@MappingTarget VariantJpaEntity variantJpa) {
        if (variantJpa.getOptions() != null) variantJpa.getOptions().forEach(o -> o.setVariant(variantJpa));
        if (variantJpa.getMedia() != null) variantJpa.getMedia().forEach(m -> m.setVariant(variantJpa));
    }
}