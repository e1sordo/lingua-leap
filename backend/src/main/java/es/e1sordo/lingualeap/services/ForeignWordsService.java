package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.dto.CreateWordRequestDto;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.WordToAddLater;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ForeignWordsService {
    void createWord(CreateWordRequestDto upsertWordRequestDto);

    Page<ForeignWord> getRecentlyAdded(int page, int pageSize);

    List<ForeignWord> getAutoSuggestions(String query);

    ForeignWord getBy(String word);

    boolean checkIfExist(String word);

    long getTotalNumber();

    void createWordToAddLater(String word);

    List<WordToAddLater> getAllWordsToAddLater();

    void deleteWordsToAddLater(String word);

    Map<LocalDate, Integer> getSummaryGraph();
}
