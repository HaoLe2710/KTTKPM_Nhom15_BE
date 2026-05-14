package fit.iuh.kttkpm_nhom15_be.carts.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.carts.domain.models.Cart;

import java.util.Optional;

public interface CartRepository {
    Optional<Cart> findActiveCart(String userId);
    Cart save(Cart cart);
}
