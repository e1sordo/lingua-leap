package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.models.WordMeaning;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SpacedRepetitionService {
    int getTodayCount();

    List<WordMeaning> getTodayWords();

    void updateWordMeaningInfo(Long wordMeaningId, int userGrade);

    void addWordMeaning(WordMeaning meaning);

    Map<LocalDate, Integer> getSummaryGraph();

    void deleteWordMeaning(Long wordMeaningId);
}
