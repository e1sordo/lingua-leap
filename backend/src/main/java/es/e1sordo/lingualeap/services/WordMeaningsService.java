package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.dto.EditWordMeaningVariantsDto;
import es.e1sordo.lingualeap.dto.WordMeaningCollocationDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.models.WordMeaningCollocation;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics;

import java.util.List;

public interface WordMeaningsService {
    WordMeaningContext linkContext(final Long meaningId, WordMeaningContextDto request);

    WordMeaningCollocation linkCollocation(final Long meaningId, WordMeaningCollocationDto request);

    void editVariants(final Long meaningId, final EditWordMeaningVariantsDto request);

    void editImageUrl(final Long meaningId, final String newUrl);

    WordMeaningCollocation editCollocation(Long meaningId, Long collocationId, WordMeaningCollocationDto request);

    List<PartOfSpeechStatistics> countMeaningsByPartOfSpeech();

    void deleteCollocation(Long meaningId, Long collocationId);
}
