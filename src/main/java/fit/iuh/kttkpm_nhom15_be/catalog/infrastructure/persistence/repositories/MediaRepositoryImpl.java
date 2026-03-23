package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.MediaRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.MediaJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

interface JpaMediaRepository extends JpaRepository<MediaJpaEntity, String> {
}

@Repository
@RequiredArgsConstructor
public class MediaRepositoryImpl implements MediaRepository {

    private final JpaMediaRepository jpaRepository;
    private final CatalogDataMapper dataMapper;

    @Override
    public Media save(Media media) {
        MediaJpaEntity entity = dataMapper.toJpaEntity(media);
        return dataMapper.toDomainModel(jpaRepository.save(entity));
    }

    @Override
    public Optional<Media> findById(String id) {
        return jpaRepository.findById(id).map(dataMapper::toDomainModel);
    }
}
