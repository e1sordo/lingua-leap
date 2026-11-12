package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.WordToAddLater;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordsToAddLaterRepository extends ListCrudRepository<WordToAddLater, Long> {

    Optional<WordToAddLater> findByCourseIdAndWordIgnoreCase(Long courseId, String word);

    List<WordToAddLater> findAllByCourseId(Long courseId);

    void deleteByCourseIdAndWord(Long courseId, String word);
}
