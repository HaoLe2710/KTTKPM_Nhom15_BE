package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import java.util.Optional;

public interface MediaRepository {
    Media save(Media media);
    Optional<Media> findById(String id);
}
