package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.WordMeaningCollocation;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordMeaningCollocationsRepository extends ListCrudRepository<WordMeaningCollocation, Long> {
}
