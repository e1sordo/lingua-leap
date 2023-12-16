package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.VocabularyList;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VocabularyListsRepository extends ListCrudRepository<VocabularyList, Long> {
    Optional<VocabularyList> findByNameAndSmartIsTrue(String name);
}
