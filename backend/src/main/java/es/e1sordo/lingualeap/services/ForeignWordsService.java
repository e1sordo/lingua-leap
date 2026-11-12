package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.dto.CreateWordRequestDto;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.WordToAddLater;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ForeignWordsService {
    void createWord(Long courseId, CreateWordRequestDto upsertWordRequestDto);

    Page<ForeignWord> getRecentlyAdded(Long courseId, int page, int pageSize);

    List<ForeignWord> getAutoSuggestions(Long courseId, String query);

    ForeignWord getBy(Long courseId, String word);

    boolean checkIfExist(Long courseId, String word);

    long getTotalNumber(Long courseId);

    void createWordToAddLater(Long courseId, String word);

    List<WordToAddLater> getAllWordsToAddLater(Long courseId);

    void deleteWordsToAddLater(Long courseId, String word);

    Map<LocalDate, Integer> getSummaryGraph(Long courseId);

    void delete(Long courseId, String word);
}
