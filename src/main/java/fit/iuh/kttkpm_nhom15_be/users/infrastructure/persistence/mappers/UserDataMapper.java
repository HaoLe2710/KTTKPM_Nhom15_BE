package fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.mappers;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.entities.UserJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserDataMapper {
    UserJpaEntity toJpaEntity(User domain);
    User toDomainModel(UserJpaEntity entity);

    @AfterMapping
    default void linkAddresses(@MappingTarget UserJpaEntity userJpa) {
        if (userJpa.getAddresses() != null) {
            userJpa.getAddresses().forEach(addr -> addr.setUser(userJpa));
        }
    }
}