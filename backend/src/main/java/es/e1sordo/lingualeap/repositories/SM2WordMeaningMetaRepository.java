package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.SM2WordMeaningMeta;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SM2WordMeaningMetaRepository extends ListCrudRepository<SM2WordMeaningMeta, Long> {
    Integer countByPlannedReviewLessThanEqual(LocalDate dateToCompare);

    List<SM2WordMeaningMeta> findByPlannedReviewLessThanEqual(LocalDate dateToCompare);

    Optional<SM2WordMeaningMeta> findByWordId(Long wordId);
}
