package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.models.ForeignWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForeignWordsRepository extends ListCrudRepository<ForeignWord, Long> {
    Page<ForeignWord> findByOrderByAddedDesc(Pageable pageable);

    List<ForeignWord> findFirst10ByWordContaining(String word);

    Optional<ForeignWord> findByWord(String word);

    boolean existsByWord(String word);
}
