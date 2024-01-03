package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.SM2WordMeaningMeta;
import es.e1sordo.lingualeap.repositories.projections.CustomQueryResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SM2WordMeaningMetaRepository extends ListCrudRepository<SM2WordMeaningMeta, Long> {
    Integer countByPlannedReviewLessThanEqual(LocalDate dateToCompare);

    @Query(value = "SELECT new es.e1sordo.lingualeap.repositories.projections.CustomQueryResult(m.plannedReview, COUNT(m)) " +
                   "FROM SM2WordMeaningMeta m " +
                   "WHERE m.plannedReview <= :maxDate " +
                   "GROUP BY m.plannedReview")
    List<CustomQueryResult> getPlannedWordsStatistics(@Param("maxDate") LocalDate maxDate);

    List<SM2WordMeaningMeta> findByPlannedReviewLessThanEqual(LocalDate dateToCompare);

    Optional<SM2WordMeaningMeta> findByWordId(Long wordId);
}
