package fit.iuh.kttkpm_nhom15_be.users.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.users.domain.models.Address;

import java.util.List;
import java.util.Optional;

public interface AddressRepository {
    Address save(Address address);
    List<Address> findByUserId(String userId);
    Optional<Address> findByIdAndUserId(String id, String userId);
    void updateAllDefaultToFalse(String userId);
    void delete(Address address);
}
