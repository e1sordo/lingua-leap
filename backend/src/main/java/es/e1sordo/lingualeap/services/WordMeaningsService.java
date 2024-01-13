package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.dto.EditWordMeaningVariantsDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics;

import java.util.List;

public interface WordMeaningsService {
    WordMeaningContext linkContext(final Long meaningId, WordMeaningContextDto request);

    void editVariants(final Long meaningId, final EditWordMeaningVariantsDto request);

    List<PartOfSpeechStatistics> countMeaningsByPartOfSpeech();
}
