package es.e1sordo.lingualeap.repositories;

import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

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
}
