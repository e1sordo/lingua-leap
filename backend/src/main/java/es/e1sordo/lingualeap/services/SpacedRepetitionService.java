package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.models.WordMeaning;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SpacedRepetitionService {
    int getTodayCount(Long courseId);

    List<WordMeaning> getTodayWords(Long courseId);

    void updateWordMeaningInfo(Long courseId, Long wordMeaningId, int userGrade);

    void addWordMeaning(WordMeaning meaning);

    Map<LocalDate, Integer> getSummaryGraph(Long courseId);

    void deleteWordMeaning(Long wordMeaningId);
}
