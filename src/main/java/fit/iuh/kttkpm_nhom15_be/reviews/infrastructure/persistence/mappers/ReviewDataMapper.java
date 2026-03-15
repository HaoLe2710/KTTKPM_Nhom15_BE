package fit.iuh.kttkpm_nhom15_be.reviews.infrastructure.persistence.mappers;

import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import fit.iuh.kttkpm_nhom15_be.reviews.infrastructure.persistence.entities.ReviewJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewDataMapper {
    ReviewJpaEntity toJpaEntity(Review domain);
    Review toDomainModel(ReviewJpaEntity entity);
}