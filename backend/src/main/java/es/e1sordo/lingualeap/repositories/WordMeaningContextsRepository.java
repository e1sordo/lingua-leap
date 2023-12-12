package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.WordMeaningContext;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordMeaningContextsRepository extends ListCrudRepository<WordMeaningContext, Long> {
}
