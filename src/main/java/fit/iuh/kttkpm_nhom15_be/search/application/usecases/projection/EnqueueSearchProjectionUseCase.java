package fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection;

import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnqueueSearchProjectionUseCase {

  private final SearchProjectionRepository searchProjectionRepository;

  public void execute(String productId, String reason) {
    searchProjectionRepository.enqueueProjectionTask(productId, reason);
  }
}
