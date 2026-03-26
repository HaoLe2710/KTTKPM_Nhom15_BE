package fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.mappers;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.Address;
import fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.entities.AddressJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressDataMapper {

    @Mapping(target = "user", ignore = true)
    AddressJpaEntity toJpaEntity(Address domain);

    @Mapping(target = "userId", source = "user.id")
    Address toDomainModel(AddressJpaEntity entity);
}
