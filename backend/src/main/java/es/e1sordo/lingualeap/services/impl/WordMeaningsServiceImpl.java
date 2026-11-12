package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.EditWordMeaningTranslationsDto;
import es.e1sordo.lingualeap.dto.WordMeaningCollocationDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.Language;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningCollocation;
import es.e1sordo.lingualeap.models.WordMeaningCollocationTranslation;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics;
import es.e1sordo.lingualeap.repositories.WordMeaningCollocationsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningContextsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningsRepository;
import es.e1sordo.lingualeap.services.NotFoundException;
import es.e1sordo.lingualeap.services.WordMeaningsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    public List<WordMeaning> getAllByPos(final Long courseId, final PartOfSpeech pos) {
        return meaningsRepository.findAllByWordCourseIdAndPos(courseId, pos);
    }

    @Override
    public List<WordMeaning> getAllByDate(final Long courseId, final LocalDate date) {
        return meaningsRepository.findAllByDate(courseId, date);
    }

    @Override
    public WordMeaning getById(final Long courseId, final Long meaningId) {
        final WordMeaning wordMeaning = meaningsRepository.findById(meaningId)
                .orElseThrow(() -> new NotFoundException("Meaning not found: " + meaningId));
        if (!Objects.equals(wordMeaning.getWord().getCourse().getId(), courseId)) {
            throw new NotFoundException("Meaning not found: " + meaningId);
        }
        return wordMeaning;
    }

    @Override
    @Transactional
    public WordMeaningContext linkContext(final Long courseId, final Long meaningId, final WordMeaningContextDto request) {
        final WordMeaning wordMeaning = getById(courseId, meaningId);

        final WordMeaningContext wordMeaningContext = new WordMeaningContext();
        wordMeaningContext.setSentence(request.sentence());
        wordMeaningContext.setTranslation(request.translation());

        wordMeaning.addContext(wordMeaningContext);
        wordMeaningContext.setWordMeaning(wordMeaning);

        return contextsRepository.save(wordMeaningContext);
    }

    @Override
    @Transactional
    public WordMeaningCollocation linkCollocation(final Long courseId, final Long meaningId, final WordMeaningCollocationDto request) {
        if (!hasText(request.pattern())) {
            throw new IllegalArgumentException("Pattern is required");
        }

        final WordMeaning wordMeaning = getById(courseId, meaningId);

        final WordMeaningCollocation wordMeaningCollocation = new WordMeaningCollocation();
        wordMeaningCollocation.setPattern(request.pattern());
        applyCollocationTranslations(wordMeaning, wordMeaningCollocation, request.translations());

        wordMeaning.addCollocation(wordMeaningCollocation);
        wordMeaningCollocation.setWordMeaning(wordMeaning);

        return collocationsRepository.save(wordMeaningCollocation);
    }

    private void applyCollocationTranslations(final WordMeaning wordMeaning,
                                              final WordMeaningCollocation collocation,
                                              final Map<String, String> translations) {
        if (translations == null) {
            return;
        }
        for (final var entry : translations.entrySet()) {
            final Language language = languageOf(wordMeaning, entry.getKey());
            final String text = entry.getValue() == null ? "" : entry.getValue().trim();
            if (hasText(text)) {
                collocation.putTranslation(language, text);
            }
        }
    }

    private Language languageOf(final WordMeaning wordMeaning, final String code) {
        final String normalizedCode = code.trim().toLowerCase();
        return wordMeaning.getWord().getCourse().getTranslationLanguages().stream()
                .map(translationLanguage -> translationLanguage.getLanguage())
                .filter(language -> language.getCode().equals(normalizedCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Language '" + code + "' is not a translation language of this course"));
    }

    @Override
    @Transactional
    public void editTranslations(final Long courseId, final Long meaningId, final EditWordMeaningTranslationsDto request) {
        final WordMeaning wordMeaning = getById(courseId, meaningId);

        final String primaryLanguageCode = wordMeaning.getWord().getCourse().getTranslationLanguages().stream()
                .filter(translationLanguage -> translationLanguage.getDisplayOrder() == 0)
                .map(translationLanguage -> translationLanguage.getLanguage().getCode())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Course has no primary translation language"));

        for (final var entry : request.translations().entrySet()) {
            final Language language = languageOf(wordMeaning, entry.getKey());
            final boolean isPrimary = language.getCode().equals(primaryLanguageCode);
            final String text = entry.getValue() == null ? "" : entry.getValue().trim();

            if (!hasText(text)) {
                if (isPrimary) {
                    throw new IllegalArgumentException("Primary translation cannot be empty");
                }
                log.info("Removing translation in language '{}' of meaning {}", language.getCode(), meaningId);
                wordMeaning.removeTranslation(language.getCode());
            } else {
                log.info("Changing translation in language '{}' of meaning {} to '{}'", language.getCode(), meaningId, text);
                wordMeaning.putTranslation(language, text);
            }
        }

        meaningsRepository.save(wordMeaning);
    }

    @Override
    @Transactional
    public void editDefinition(final Long courseId, final Long meaningId, final String newDefinition) {
        final WordMeaning wordMeaning = getById(courseId, meaningId);

        if (!newDefinition.equals(wordMeaning.getImageUrl())) {
            log.info("Changing definition of meaning {}", meaningId);
            wordMeaning.setDefinition(newDefinition);
        }

        meaningsRepository.save(wordMeaning);
    }

    @Override
    @Transactional
    public void editImageUrl(final Long courseId, final Long meaningId, final String newUrl) {
        final WordMeaning wordMeaning = getById(courseId, meaningId);

        if (!newUrl.equals(wordMeaning.getImageUrl())) {
            log.info("Changing image url of meaning {} from '{}' to '{}'", meaningId, wordMeaning.getImageUrl(), newUrl);
            wordMeaning.setImageUrl(newUrl);
        }

        meaningsRepository.save(wordMeaning);
    }

    @Override
    @Transactional
    public WordMeaningCollocation editCollocation(final Long courseId,
                                                  final Long meaningId,
                                                  final Long collocationId,
                                                  final WordMeaningCollocationDto request) {
        final WordMeaning wordMeaning = getById(courseId, meaningId);

        final WordMeaningCollocation collocation = collocationsRepository.findById(collocationId)
                .orElseThrow(() -> new NotFoundException("Collocation not found: " + collocationId));
        if (!Objects.equals(collocation.getWordMeaning().getId(), meaningId)) {
            throw new NotFoundException("Collocation not found: " + collocationId);
        }

        if (request.pattern() != null) {
            log.info("Changing pattern of collocation {} from '{}' to '{}'", collocationId, collocation.getPattern(), request.pattern());
            collocation.setPattern(request.pattern().trim());
        }

        if (request.translations() != null) {
            for (final var entry : request.translations().entrySet()) {
                final Language language = languageOf(wordMeaning, entry.getKey());
                final String text = entry.getValue() == null ? "" : entry.getValue().trim();
                if (hasText(text)) {
                    log.info("Changing translation in language '{}' of collocation {} to '{}'", language.getCode(), collocationId, text);
                    collocation.putTranslation(language, text);
                } else {
                    log.info("Removing translation in language '{}' of collocation {}", language.getCode(), collocationId);
                    collocation.removeTranslation(language.getCode());
                }
            }
        }

        return collocationsRepository.save(collocation);
    }

    @Override
    public List<PartOfSpeechStatistics> countMeaningsByPartOfSpeech(final Long courseId) {
        return meaningsRepository.countMeaningsByPartOfSpeech(courseId)
                .stream()
                .filter(o -> o.pos() != null)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCollocation(final Long courseId, final Long meaningId, final Long collocationId) {
        getById(courseId, meaningId);

        final WordMeaningCollocation collocation = collocationsRepository.findById(collocationId)
                .orElseThrow(() -> new NotFoundException("Collocation not found: " + collocationId));
        if (!Objects.equals(collocation.getWordMeaning().getId(), meaningId)) {
            throw new NotFoundException("Collocation not found: " + collocationId);
        }

        collocationsRepository.delete(collocation);
    }
}
