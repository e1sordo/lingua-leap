package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.dto.UpsertVocabularyListRequestDto;
import es.e1sordo.lingualeap.models.VocabularyList;

import java.time.LocalDate;
import java.util.List;

public interface VocabularyListsService {
    List<VocabularyList> getAll(Long courseId);

    VocabularyList getBy(Long courseId, Long id);

    VocabularyList getByPos(Long courseId, String pos);

    VocabularyList getByDate(Long courseId, LocalDate date);

    VocabularyList getSmartListOfRecentlyAdded(Long courseId);

    VocabularyList getSmartListOfProblemWords(Long courseId);

    VocabularyList upsertList(Long courseId, UpsertVocabularyListRequestDto request);

    void removeWordFromList(Long courseId, Long listId, Long wordMeaningId);

    void delete(Long courseId, Long id);
}
