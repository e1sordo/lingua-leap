package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.WordToAddLater;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WordsToAddLaterRepository extends ListCrudRepository<WordToAddLater, String> {
    Optional<WordToAddLater> findByWordIgnoreCase(String word);
}
