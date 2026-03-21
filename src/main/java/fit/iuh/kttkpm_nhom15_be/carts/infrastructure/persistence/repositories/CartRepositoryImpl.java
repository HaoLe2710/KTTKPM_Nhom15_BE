package fit.iuh.kttkpm_nhom15_be.carts.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.carts.domain.models.Cart;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.CartStatus;
import fit.iuh.kttkpm_nhom15_be.carts.domain.repositories.CartRepository;
import fit.iuh.kttkpm_nhom15_be.carts.infrastructure.persistence.enities.CartJpaEntity;
import fit.iuh.kttkpm_nhom15_be.carts.infrastructure.persistence.mappers.CartDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 1. Giao tiếp với Database qua Spring Data JPA
interface JpaCartRepository extends JpaRepository<CartJpaEntity, String> {
  Optional<CartJpaEntity> findByUserIdAndStatus(String userId, CartStatus status);
}

// 2. Implement domain repository
@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {

  private final JpaCartRepository jpaCartRepository;
  private final CartDataMapper cartDataMapper;

  @Override
  public Optional<Cart> findActiveCart(String userId) {
    return jpaCartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
      .map(cartDataMapper::toDomainModel);
  }

  @Override
  public Cart save(Cart cart) {
    CartJpaEntity entity = cartDataMapper.toJpaEntity(cart);
    CartJpaEntity savedEntity = jpaCartRepository.save(entity);
    return cartDataMapper.toDomainModel(savedEntity);
  }
}
