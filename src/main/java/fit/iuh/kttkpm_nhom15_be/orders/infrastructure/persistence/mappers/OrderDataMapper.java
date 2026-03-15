package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.mappers;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.entities.OrderJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderDataMapper {
    OrderJpaEntity toJpaEntity(Order domain);
    Order toDomainModel(OrderJpaEntity entity);

    @AfterMapping
    default void linkItems(@MappingTarget OrderJpaEntity orderJpa) {
        if (orderJpa.getItems() != null) orderJpa.getItems().forEach(i -> i.setOrder(orderJpa));
    }
}