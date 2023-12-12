package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.models.WordMeaning;

import java.util.List;

public interface SpacedRepetitionService {
    int getTodayCount();

    List<WordMeaning> getTodayWords();

    void updateWordMeaningInfo(Long wordMeaningId, int userGrade);

    void addWordMeaning(WordMeaning meaning);
}
