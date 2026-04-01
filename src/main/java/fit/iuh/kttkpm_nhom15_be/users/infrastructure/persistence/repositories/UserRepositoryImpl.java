package fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.entities.UserJpaEntity;
import fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.mappers.UserDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

interface JpaUserRepository extends JpaRepository<UserJpaEntity, String> {
    boolean existsByEmailOrPhone(String email, String phone);

    @Query("SELECT COUNT(u) > 0 FROM UserJpaEntity u WHERE (u.email = :email OR u.phone = :phone) AND u.id <> :id")
    boolean existsByEmailOrPhoneExcludingId(@Param("email") String email, @Param("phone") String phone, @Param("id") String id);
    @Query("SELECT u FROM UserJpaEntity u WHERE :keyword IS NULL OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "u.phone LIKE CONCAT('%', :keyword, '%')")
    Page<UserJpaEntity> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    Optional<UserJpaEntity> findByEmail(String email);
    Optional<UserJpaEntity> findByPhone(String phone);
}

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserDataMapper userDataMapper;

    @Override
    public boolean existsByEmailOrPhone(String email, String phone) {
        return jpaUserRepository.existsByEmailOrPhone(email, phone);
    }

    @Override
    public boolean existsByEmailOrPhoneExcludingId(String email, String phone, String id) {
        return jpaUserRepository.existsByEmailOrPhoneExcludingId(email, phone, id);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = userDataMapper.toJpaEntity(user);
        UserJpaEntity savedEntity = jpaUserRepository.save(entity);
        return userDataMapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<User> findById(String id) {
        return jpaUserRepository.findById(id).map(userDataMapper::toDomainModel);
    }

    @Override
    public void deleteById(String id) {
        jpaUserRepository.deleteById(id);
    }
    @Override
    public Page<User> findAll(String keyword, Pageable pageable) {
        // Lấy Page<Entity> từ DB, sau đó dùng hàm map() biến thành Page<Domain> thông qua mapper có sẵn
        return jpaUserRepository.searchUsers(keyword, pageable)
                .map(userDataMapper::toDomainModel);
    }
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .map(userDataMapper::toDomainModel); // Map từ Entity sang Domain Model
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return jpaUserRepository.findByPhone(phone)
                .map(userDataMapper::toDomainModel);
    }
}