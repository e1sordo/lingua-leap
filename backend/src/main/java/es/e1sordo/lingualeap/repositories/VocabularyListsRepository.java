package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.VocabularyList;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabularyListsRepository extends ListCrudRepository<VocabularyList, Long> {
}
