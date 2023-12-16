package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.dto.word.UpsertVocabularyListRequestDto;
import es.e1sordo.lingualeap.models.VocabularyList;

import java.util.List;

public interface VocabularyListsService {
    List<VocabularyList> getAll();

    VocabularyList getBy(Long id);

    VocabularyList getSmartListOfRecentlyAdded();

    VocabularyList getSmartListOfProblemWords();

    VocabularyList upsertList(UpsertVocabularyListRequestDto request);
}
