package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.EditWordMeaningVariantsDto;
import es.e1sordo.lingualeap.dto.WordMeaningCollocationDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningCollocation;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics;
import es.e1sordo.lingualeap.repositories.WordMeaningCollocationsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningContextsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningsRepository;
import es.e1sordo.lingualeap.services.WordMeaningsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordMeaningsServiceImpl implements WordMeaningsService {

    private final WordMeaningsRepository meaningsRepository;
    private final WordMeaningContextsRepository contextsRepository;
    private final WordMeaningCollocationsRepository collocationsRepository;

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
    public WordMeaningCollocation linkCollocation(final Long meaningId, final WordMeaningCollocationDto request) {
        if (!hasText(request.pattern())) {
            throw new IllegalArgumentException("Pattern is required");
        }

        final WordMeaning wordMeaning = meaningsRepository.findById(meaningId).orElseThrow(NoSuchElementException::new);

        final WordMeaningCollocation wordMeaningCollocation = new WordMeaningCollocation();
        wordMeaningCollocation.setPattern(request.pattern());
        wordMeaningCollocation.setTranslationRussian(request.translationRussian());
        wordMeaningCollocation.setTranslationEnglish(request.translationEnglish());

        wordMeaning.addCollocation(wordMeaningCollocation);
        wordMeaningCollocation.setWordMeaning(wordMeaning);

        return collocationsRepository.save(wordMeaningCollocation);
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
    public void editImageUrl(final Long meaningId, final String newUrl) {
        final WordMeaning wordMeaning = meaningsRepository.findById(meaningId).orElseThrow(NoSuchElementException::new);

        if (hasText(newUrl) && !newUrl.equals(wordMeaning.getImageUrl())) {
            log.info("Changing image url of meaning {} from '{}' to '{}'", meaningId, wordMeaning.getImageUrl(), newUrl);
            wordMeaning.setImageUrl(newUrl);
        }

        meaningsRepository.save(wordMeaning);
    }

    @Override
    public WordMeaningCollocation editCollocation(final Long meaningId, final Long collocationId, final WordMeaningCollocationDto request) {
        final WordMeaningCollocation collocation = collocationsRepository.findById(collocationId).orElseThrow(NoSuchElementException::new);
        if (!Objects.equals(collocation.getWordMeaning().getId(), meaningId)) {
            throw new IllegalArgumentException("Collocation does not belong to this meaning");
        }

        if (request.pattern() != null) {
            log.info("Changing pattern of collocation {} from '{}' to '{}'", collocationId, collocation.getPattern(), request.pattern());
            collocation.setPattern(request.pattern().trim());
        }

        if (request.translationRussian() != null) {
            log.info("Changing russian translation of collocation {} from '{}' to '{}'", collocationId, collocation.getTranslationRussian(), request.translationRussian());
            collocation.setTranslationRussian(request.translationRussian().trim());
        }

        if (request.translationEnglish() != null) {
            log.info("Changing english translation of collocation {} from '{}' to '{}'", collocationId, collocation.getTranslationEnglish(), request.translationEnglish());
            collocation.setTranslationEnglish(request.translationEnglish().trim());
        }

        return collocationsRepository.save(collocation);
    }

    @Override
    public List<PartOfSpeechStatistics> countMeaningsByPartOfSpeech() {
        return meaningsRepository.countMeaningsByPartOfSpeech();
    }

    @Override
    public void deleteCollocation(final Long meaningId, final Long collocationId) {
        final WordMeaningCollocation collocation = collocationsRepository.findById(collocationId).orElseThrow(NoSuchElementException::new);
        if (!Objects.equals(collocation.getWordMeaning().getId(), meaningId)) {
            throw new IllegalArgumentException("Collocation does not belong to this meaning");
        }

        collocationsRepository.delete(collocation);
    }
}
