package fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.Address;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.AddressRepository;
import fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.entities.AddressJpaEntity;
import fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.entities.UserJpaEntity;
import fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.mappers.AddressDataMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

interface JpaAddressRepository extends JpaRepository<AddressJpaEntity, String> {

    @Query("SELECT a FROM AddressJpaEntity a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<AddressJpaEntity> findByUserId(@Param("userId") String userId);

    @Query("SELECT a FROM AddressJpaEntity a WHERE a.id = :id AND a.user.id = :userId")
    Optional<AddressJpaEntity> findByIdAndUserId(@Param("id") String id, @Param("userId") String userId);

    @Modifying
    @Query("UPDATE AddressJpaEntity a SET a.isDefault = false WHERE a.user.id = :userId")
    void updateAllDefaultToFalse(@Param("userId") String userId);
}

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {

    private final JpaAddressRepository jpaAddressRepository;
    private final AddressDataMapper addressDataMapper;
    private final EntityManager entityManager;

    @Override
    public Address save(Address address) {
        AddressJpaEntity entity = addressDataMapper.toJpaEntity(address);
        entity.setUser(entityManager.getReference(UserJpaEntity.class, address.getUserId()));
        AddressJpaEntity savedEntity = jpaAddressRepository.save(entity);
        return addressDataMapper.toDomainModel(savedEntity);
    }

    @Override
    public List<Address> findByUserId(String userId) {
        return jpaAddressRepository.findByUserId(userId).stream()
                .map(addressDataMapper::toDomainModel)
                .toList();
    }

    @Override
    public Optional<Address> findByIdAndUserId(String id, String userId) {
        return jpaAddressRepository.findByIdAndUserId(id, userId)
                .map(addressDataMapper::toDomainModel);
    }

    @Override
    public void updateAllDefaultToFalse(String userId) {
        jpaAddressRepository.updateAllDefaultToFalse(userId);
    }

    @Override
    public void delete(Address address) {
        jpaAddressRepository.deleteById(address.getId());
    }
}
