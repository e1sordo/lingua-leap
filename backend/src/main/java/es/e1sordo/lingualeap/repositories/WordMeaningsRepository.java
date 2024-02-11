package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WordMeaningsRepository extends ListCrudRepository<WordMeaning, Long> {

    @Query("""
            SELECT new es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics(m.pos, COUNT(m))
            FROM WordMeaning m
            GROUP BY m.pos
            """)
    List<PartOfSpeechStatistics> countMeaningsByPartOfSpeech();

    List<WordMeaning> findAllByPos(PartOfSpeech pos);

    @Query("""
            SELECT m
            FROM WordMeaning m
            WHERE m.word.added = :date
            """)
    List<WordMeaning> findAllByDate(LocalDate date);
}
