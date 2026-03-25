package fit.iuh.kttkpm_nhom15_be.carts.application.usecases;

import fit.iuh.kttkpm_nhom15_be.carts.application.commands.AddToCartCommand;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.Cart;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.CartItem;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.CartStatus;
import fit.iuh.kttkpm_nhom15_be.carts.domain.repositories.CartRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.VariantInfoDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddToCartUseCaseTest {

    @Test
    void executeCreatesActiveCartWhenUserHasNoCart() {
        CartRepository cartRepository = Mockito.mock(CartRepository.class);
        CatalogFacade catalogFacade = Mockito.mock(CatalogFacade.class);
        AddToCartUseCase useCase = new AddToCartUseCase(cartRepository, catalogFacade);

        when(catalogFacade.checkAvailabilityAndPrice("variant-1", 2))
            .thenReturn(VariantInfoDTO.builder().price(new BigDecimal("125.00")).name("Sneaker").build());
        when(cartRepository.findActiveCart("user-1")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartSummaryDTO result = useCase.execute(new AddToCartCommand("user-1", "variant-1", 2));

        ArgumentCaptor<Cart> savedCart = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, times(2)).save(savedCart.capture());
        assertEquals(2, result.totalItems());
        assertEquals(new BigDecimal("250.00"), result.subtotal());
        assertEquals(CartStatus.ACTIVE, savedCart.getValue().getStatus());
        assertEquals(1, savedCart.getValue().getItems().size());
        assertEquals("variant-1", savedCart.getValue().getItems().get(0).getVariantId());
    }

    @Test
    void executeMergesQuantityWhenVariantAlreadyExistsInCart() {
        CartRepository cartRepository = Mockito.mock(CartRepository.class);
        CatalogFacade catalogFacade = Mockito.mock(CatalogFacade.class);
        AddToCartUseCase useCase = new AddToCartUseCase(cartRepository, catalogFacade);
        Cart existingCart = Cart.builder()
            .userId("user-1")
            .status(CartStatus.ACTIVE)
            .items(new ArrayList<>(java.util.List.of(CartItem.builder()
                .variantId("variant-1")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build())))
            .build();

        when(catalogFacade.checkAvailabilityAndPrice("variant-1", 3))
            .thenReturn(VariantInfoDTO.builder().price(new BigDecimal("120.00")).name("Sneaker").build());
        when(cartRepository.findActiveCart("user-1")).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(existingCart)).thenReturn(existingCart);

        CartSummaryDTO result = useCase.execute(new AddToCartCommand("user-1", "variant-1", 3));

        verify(cartRepository).save(existingCart);
        assertEquals(4, result.totalItems());
        assertEquals(new BigDecimal("480.00"), result.subtotal());
        assertEquals(4, existingCart.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("120.00"), existingCart.getItems().get(0).getUnitPrice());
    }
}
