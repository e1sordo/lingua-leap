package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.dto.UpsertVocabularyListRequestDto;
import es.e1sordo.lingualeap.models.VocabularyList;

import java.time.LocalDate;
import java.util.List;

public interface VocabularyListsService {
    List<VocabularyList> getAll();

    VocabularyList getBy(Long id);

    VocabularyList getByPos(String pos);

    VocabularyList getByDate(LocalDate date);

    VocabularyList getSmartListOfRecentlyAdded();

    VocabularyList getSmartListOfProblemWords();

    VocabularyList upsertList(UpsertVocabularyListRequestDto request);

    void removeWordFromList(Long listId, Long wordMeaningId);

    void delete(Long id);
}
