package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.repositories.projections.CustomGroupByQueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForeignWordsRepository extends ListCrudRepository<ForeignWord, Long> {

    @Query(value = "SELECT new es.e1sordo.lingualeap.repositories.projections.CustomGroupByQueryResult(w.added, COUNT(w)) " +
                   "FROM ForeignWord w " +
                   "WHERE w.course.id = :courseId AND w.added >= :minDate " +
                   "GROUP BY w.added")
    List<CustomGroupByQueryResult> getAddedWordsStatistics(@Param("minDate") LocalDate minDate,
                                                           @Param("courseId") Long courseId);

    Page<ForeignWord> findByCourseIdOrderByAddedDesc(Long courseId, Pageable pageable);

    List<ForeignWord> findFirst10ByCourseIdAndWordContaining(Long courseId, String word);

    Optional<ForeignWord> findByCourseIdAndWord(Long courseId, String word);

    Optional<ForeignWord> findByCourseIdAndWordIgnoreCase(Long courseId, String word);

    boolean existsByCourseIdAndWord(Long courseId, String word);

    long countByCourseId(Long courseId);
}
