package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.EditWordMeaningVariantsDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics;
import es.e1sordo.lingualeap.repositories.WordMeaningContextsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningsRepository;
import es.e1sordo.lingualeap.services.WordMeaningsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordMeaningsServiceImpl implements WordMeaningsService {

    private final WordMeaningsRepository meaningsRepository;
    private final WordMeaningContextsRepository contextsRepository;

    @Override
    public WordMeaningContext linkContext(final Long meaningId, final WordMeaningContextDto request) {
        final WordMeaning wordMeaning = meaningsRepository.findById(meaningId).orElseThrow(NoSuchElementException::new);

        final WordMeaningContext wordMeaningContext = new WordMeaningContext();
        wordMeaningContext.setSentence(request.sentence());
        wordMeaningContext.setTranslation(request.translation());

        wordMeaning.addContext(wordMeaningContext);
        wordMeaningContext.setWordMeaning(wordMeaning);

        return contextsRepository.save(wordMeaningContext);
    }

    @Override
    public void editVariants(final Long meaningId, final EditWordMeaningVariantsDto request) {
        final WordMeaning wordMeaning = meaningsRepository.findById(meaningId).orElseThrow(NoSuchElementException::new);

        if (request.russian() != null) {
            log.info("Changing russian variant of meaning {} from '{}' to '{}'", meaningId, wordMeaning.getRussianVariant(), request.russian());
            wordMeaning.setRussianVariant(request.russian().trim());
        }

        if (request.english() != null) {
            log.info("Changing english variant of meaning {} from '{}' to '{}'", meaningId, wordMeaning.getEnglishVariant(), request.english());
            wordMeaning.setEnglishVariant(request.english().trim());
        }

        meaningsRepository.save(wordMeaning);
    }

    @Override
    public List<PartOfSpeechStatistics> countMeaningsByPartOfSpeech() {
        return meaningsRepository.countMeaningsByPartOfSpeech();
    }
}
