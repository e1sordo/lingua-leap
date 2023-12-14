package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.WordToAddLater;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordsToAddLaterRepository extends ListCrudRepository<WordToAddLater, String> {
}
