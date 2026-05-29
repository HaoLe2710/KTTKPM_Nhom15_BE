package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.*;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CatalogDataMapper {
    ProductJpaEntity toJpaEntity(Product domain);

    @Mapping(target = "isCustomizable", source = "customizable")
    @Mapping(target = "isActive", source = "active")
    Product toDomainModel(ProductJpaEntity entity);

    VariantJpaEntity toJpaEntity(Variant domain);

    @Mapping(target = "productId", expression = "java(entity.getProduct() != null ? entity.getProduct().getId() : null)")
    @Mapping(target = "isActive", source = "active")
    Variant toDomainModel(VariantJpaEntity entity);

    // Auto map các Entity con khác nếu được gọi trực tiếp
    ProductTypeJpaEntity toJpaEntity(ProductType domain);
    ProductType toDomainModel(ProductTypeJpaEntity entity);

    OptionJpaEntity toJpaEntity(Option domain);
    Option toDomainModel(OptionJpaEntity entity);

    OptionValueJpaEntity toJpaEntity(OptionValue domain);

    @Mapping(target = "optionId", expression = "java(entity.getOption() != null ? entity.getOption().getId() : null)")
    @Mapping(target = "isActive", source = "active")
    OptionValue toDomainModel(OptionValueJpaEntity entity);

    VariantOptionJpaEntity toJpaEntity(VariantOption domain);

    @Mapping(target = "variantId", expression = "java(entity.getVariant() != null ? entity.getVariant().getId() : null)")
    @Mapping(target = "optionValueId", expression = "java(entity.getOptionValue() != null ? entity.getOptionValue().getId() : null)")
    VariantOption toDomainModel(VariantOptionJpaEntity entity);

    MediaJpaEntity toJpaEntity(Media domain);

    @Mapping(target = "productId", expression = "java(entity.getProduct() != null ? entity.getProduct().getId() : null)")
    @Mapping(target = "variantId", expression = "java(entity.getVariant() != null ? entity.getVariant().getId() : null)")
    @Mapping(target = "isPrimary", source = "primary")
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
