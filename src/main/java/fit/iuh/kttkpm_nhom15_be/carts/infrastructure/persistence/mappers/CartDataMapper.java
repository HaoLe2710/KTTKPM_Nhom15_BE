package fit.iuh.kttkpm_nhom15_be.carts.infrastructure.persistence.mappers;


import fit.iuh.kttkpm_nhom15_be.carts.domain.models.Cart;
import fit.iuh.kttkpm_nhom15_be.carts.infrastructure.persistence.enities.CartJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CartDataMapper {
    CartJpaEntity toJpaEntity(Cart domain);
    Cart toDomainModel(CartJpaEntity entity);

    @AfterMapping
    default void linkItems(@MappingTarget CartJpaEntity cartJpa) {
        if (cartJpa.getItems() != null) cartJpa.getItems().forEach(i -> i.setCart(cartJpa));
    }
}