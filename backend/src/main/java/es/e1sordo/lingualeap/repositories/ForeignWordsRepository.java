package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.ForeignWord;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForeignWordsRepository extends ListCrudRepository<ForeignWord, Long> {
    List<ForeignWord> findFirst100ByOrderByAddedDesc();

    Optional<ForeignWord> findByWord(String word);

    boolean existsByWord(String word);
}
