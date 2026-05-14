package fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.mappers;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.entities.UserJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserDataMapper {

    @Mapping(target = "password", source = "password")
    UserJpaEntity toJpaEntity(User domain);

    @Mapping(target = "password", source = "password")
    // CHỈ GIỮ LẠI MỘT DÒNG DUY NHẤT CHO isActive
    @Mapping(target = "isActive", source = "active")
    User toDomainModel(UserJpaEntity entity);

    @AfterMapping
    default void linkAddresses(@MappingTarget UserJpaEntity userJpa) {
        if (userJpa.getAddresses() != null) {
            userJpa.getAddresses().forEach(addr -> addr.setUser(userJpa));
        }
    }
}