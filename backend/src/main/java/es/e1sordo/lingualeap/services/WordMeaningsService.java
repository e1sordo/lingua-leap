package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.dto.EditWordMeaningTranslationsDto;
import es.e1sordo.lingualeap.dto.WordMeaningCollocationDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningCollocation;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics;

import java.time.LocalDate;
import java.util.List;

public interface WordMeaningsService {
    List<WordMeaning> getAllByPos(Long courseId, PartOfSpeech pos);

    List<WordMeaning> getAllByDate(Long courseId, LocalDate date);

    WordMeaning getById(Long courseId, Long meaningId);

    WordMeaningContext linkContext(Long courseId, final Long meaningId, WordMeaningContextDto request);

    WordMeaningCollocation linkCollocation(Long courseId, final Long meaningId, WordMeaningCollocationDto request);

    void editTranslations(Long courseId, final Long meaningId, final EditWordMeaningTranslationsDto request);

    void editDefinition(Long courseId, final Long meaningId, final String newDefinition);

    void editImageUrl(Long courseId, final Long meaningId, final String newUrl);

    WordMeaningCollocation editCollocation(Long courseId, Long meaningId, Long collocationId, WordMeaningCollocationDto request);

    List<PartOfSpeechStatistics> countMeaningsByPartOfSpeech(Long courseId);

    void deleteCollocation(Long courseId, Long meaningId, Long collocationId);
}
