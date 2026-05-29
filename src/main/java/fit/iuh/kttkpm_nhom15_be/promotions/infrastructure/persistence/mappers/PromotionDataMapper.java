package fit.iuh.kttkpm_nhom15_be.promotions.infrastructure.persistence.mappers;

import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.infrastructure.persistence.entities.PromotionJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionDataMapper {
    PromotionJpaEntity toJpaEntity(Promotion domain);

    @Mapping(target = "isActive", source = "active")
    Promotion toDomainModel(PromotionJpaEntity entity);
}
