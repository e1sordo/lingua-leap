package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.enums.SmartListType;
import es.e1sordo.lingualeap.models.VocabularyList;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VocabularyListsRepository extends ListCrudRepository<VocabularyList, Long> {

    List<VocabularyList> findAllByCourseId(Long courseId);

    Optional<VocabularyList> findByCourseIdAndId(Long courseId, Long id);

    Optional<VocabularyList> findByCourseIdAndSmartType(Long courseId, SmartListType smartType);
}
