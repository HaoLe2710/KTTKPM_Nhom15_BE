package fit.iuh.kttkpm_nhom15_be.users.application.interfaces;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;

import java.util.Optional;

public interface UserFacade {
    boolean isUserActive(String userId);
    Optional<User> findByIdentifier(String identifier);
    void registerCustomer(String email, String phone, String password, String fullName);
    User createOAuth2User(String email, String fullName);
}