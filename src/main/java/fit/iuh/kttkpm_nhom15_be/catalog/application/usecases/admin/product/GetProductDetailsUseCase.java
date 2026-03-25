package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetProductDetailsUseCase {

    private final ProductRepository productRepository;

    public Optional<Product> execute(String id) {
        // Find by ID normally leverages implicit fetch graphs via the domain mapper/ JPA model
        return productRepository.findById(id);
    }
}
