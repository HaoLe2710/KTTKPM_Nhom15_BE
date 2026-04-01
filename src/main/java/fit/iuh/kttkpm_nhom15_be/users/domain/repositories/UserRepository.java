package fit.iuh.kttkpm_nhom15_be.users.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface UserRepository {
    boolean existsByEmailOrPhone(String email, String phone);
    boolean existsByEmailOrPhoneExcludingId(String email, String phone, String id);
    User save(User user);
    Optional<User> findById(String id);
    void deleteById(String id);
    Page<User> findAll(String keyword, Pageable pageable);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
}