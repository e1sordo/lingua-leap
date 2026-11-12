package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.WordMeaningDto;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.Language;
import es.e1sordo.lingualeap.models.LanguageCourse;
import es.e1sordo.lingualeap.models.VocabularyList;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningCollocation;
import es.e1sordo.lingualeap.models.WordMeaningCollocationTranslation;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.models.WordToAddLater;
import es.e1sordo.lingualeap.repositories.ForeignWordsRepository;
import es.e1sordo.lingualeap.repositories.WordsToAddLaterRepository;
import es.e1sordo.lingualeap.services.ForeignWordsService;
import es.e1sordo.lingualeap.services.LanguageCoursesService;
import es.e1sordo.lingualeap.services.NotFoundException;
import es.e1sordo.lingualeap.services.SpacedRepetitionService;
import es.e1sordo.lingualeap.services.VocabularyListsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
public class ForeignWordsServiceImpl implements ForeignWordsService {

    private final VocabularyListsService vocabularyListsService;
    private final SpacedRepetitionService spacedRepetitionService;
    private final LanguageCoursesService languageCoursesService;
    private final ForeignWordsRepository repository;
    private final WordsToAddLaterRepository wordsToAddLaterRepository;

    @Override
    @Transactional
    public void createWord(final Long courseId, final CreateWordRequestDto request) {
        final LanguageCourse course = languageCoursesService.getCourseById(courseId);

        final String trimmedWord = Objects.requireNonNull(request.word(), "Word is required").trim();
        if (trimmedWord.isEmpty()) {
            throw new IllegalArgumentException("Word cannot be empty");
        }

        final var word = repository.findByCourseIdAndWord(courseId, trimmedWord).orElseGet(() -> {
            final var foreignWord = new ForeignWord();
            foreignWord.setAdded(request.addedDate());
            foreignWord.setWord(trimmedWord);
            foreignWord.setCourse(course);
            return foreignWord;
        });

        final var meaningsToAddToSpacedRepetition = new ArrayList<WordMeaning>();

        for (WordMeaningDto meaningDto : request.meanings()) {
            final Map<String, String> normalizedTranslations = normalizeTranslations(meaningDto.translations());

            validateTranslations(course, normalizedTranslations);

            final var meaning = getWordMeaning(meaningDto, word);
            for (final var entry : normalizedTranslations.entrySet()) {
                meaning.putTranslation(languageOf(course, entry.getKey()), entry.getValue());
            }

            final boolean meaningAlreadyExist = word.getMeanings().stream()
                    .anyMatch(existedMeaning -> sameTranslations(existedMeaning, normalizedTranslations));
            if (meaningAlreadyExist) {
                continue;
            }

            if (meaningDto.lists() != null) {
                for (final var listDto : meaningDto.lists()) {
                    final var list = (listDto.id() != null && listDto.id() > 0)
                            ? vocabularyListsService.getBy(courseId, listDto.id())
                            : newListOfCurrentCourse(course, listDto.name());

                    list.setName(listDto.name());
                    list.addMeaning(meaning);

                    meaning.addList(list);
                }
            }

            addMeaningToSmartListOfRecentlyAdded(courseId, meaning);
            meaningsToAddToSpacedRepetition.add(meaning);

            if (meaningDto.collocations() != null) {
                for (final var collocationDto : meaningDto.collocations()) {
                    final String trimmedPattern = collocationDto.pattern().trim();
                    if (trimmedPattern.isBlank()) {
                        continue;
                    }

                    final var collocation = new WordMeaningCollocation();
                    collocation.setPattern(trimmedPattern);
                    collocation.setWordMeaning(meaning);

                    for (final var entry : normalizeTranslations(collocationDto.translations()).entrySet()) {
                        final var translation = new WordMeaningCollocationTranslation(
                                collocation, languageOf(course, entry.getKey()), entry.getValue());
                        collocation.getTranslations().add(translation);
                    }

                    meaning.addCollocation(collocation);
                }
            }

            if (meaningDto.contexts() != null) {
                for (final var contextDto : meaningDto.contexts()) {
                    final var context = new WordMeaningContext();

                    final String sentence = contextDto.sentence().trim();
                    if (sentence.isBlank()) {
                        continue;
                    } else {
                        context.setSentence(sentence);
                    }

                    final String translation = contextDto.translation().trim();
                    if (translation.isBlank()) {
                        continue;
                    } else {
                        context.setTranslation(translation);
                    }

                    context.setWordMeaning(meaning);

                    meaning.addContext(context);
                }
            }

            word.addMeaning(meaning);
        }

        final ForeignWord persistedWord = repository.save(word);

        meaningsToAddToSpacedRepetition.forEach(spacedRepetitionService::addWordMeaning);
        wordsToAddLaterRepository.findByCourseIdAndWordIgnoreCase(courseId, persistedWord.getWord())
                .ifPresent(wordsToAddLaterRepository::delete);
    }

    private VocabularyList newListOfCurrentCourse(final LanguageCourse course, final String name) {
        final var list = new VocabularyList();
        list.setName(name);
        list.setCourse(course);
        list.setSmart(false);
        list.setAdded(LocalDate.now());
        return list;
    }

    private void addMeaningToSmartListOfRecentlyAdded(final Long courseId, final WordMeaning meaning) {
        final VocabularyList smartList = vocabularyListsService.getSmartListOfRecentlyAdded(courseId);
        meaning.addList(smartList);
    }

    private Map<String, String> normalizeTranslations(final Map<String, String> translations) {
        if (translations == null) {
            return Map.of();
        }
        final Map<String, String> normalized = new LinkedHashMap<>();
        translations.forEach((code, text) -> {
            if (code != null && text != null && hasText(text)) {
                normalized.put(code.trim().toLowerCase(), text.trim());
            }
        });
        return normalized;
    }

    private void validateTranslations(final LanguageCourse course, final Map<String, String> translations) {
        final List<String> courseLanguages = course.getTranslationLanguages().stream()
                .map(translationLanguage -> translationLanguage.getLanguage().getCode())
                .toList();

        for (final String code : translations.keySet()) {
            if (!courseLanguages.contains(code)) {
                throw new IllegalArgumentException("Language '" + code + "' is not a translation language of this course");
            }
        }

        final String primaryLanguage = courseLanguages.get(0);
        final String primaryTranslation = translations.get(primaryLanguage);
        if (primaryTranslation == null || primaryTranslation.isBlank()) {
            throw new IllegalArgumentException("Missing primary translation in language '" + primaryLanguage + "'");
        }
    }

    private Language languageOf(final LanguageCourse course, final String code) {
        return course.getTranslationLanguages().stream()
                .map(translationLanguage -> translationLanguage.getLanguage())
                .filter(language -> language.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Language '" + code + "' is not a translation language of this course"));
    }

    private boolean sameTranslations(final WordMeaning existedMeaning, final Map<String, String> normalizedTranslations) {
        for (final var entry : normalizedTranslations.entrySet()) {
            if (!Objects.equals(Optional.ofNullable(existedMeaning.getTranslation(entry.getKey())).orElse(""),
                    entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static WordMeaning getWordMeaning(final WordMeaningDto meaningDto,
                                              final ForeignWord word) {
        final var meaning = new WordMeaning();

        final PartOfSpeech partOfSpeech = meaningDto.pos();
        if (partOfSpeech == null) {
            throw new IllegalArgumentException("Missing part of speech");
        }

        meaning.setPos(partOfSpeech);
        if (PartOfSpeech.NOUN == partOfSpeech) {
            meaning.setGender(meaningDto.gender());
        }
        meaning.setImageUrl(meaningDto.imageUrl());
        meaning.setDefinition(meaningDto.definition());
        meaning.setFrequency(meaningDto.frequency());
        meaning.setLearningStatus(LearningStatus.NEW);
        meaning.setWord(word);

        return meaning;
    }

    @Override
    public Page<ForeignWord> getRecentlyAdded(final Long courseId, final int page, final int pageSize) {
        final Pageable pageable = Pageable.ofSize(pageSize).withPage(page);
        return repository.findByCourseIdOrderByAddedDesc(courseId, pageable);
    }

    @Override
    public List<ForeignWord> getAutoSuggestions(final Long courseId, final String query) {
        return repository.findFirst10ByCourseIdAndWordContaining(courseId, query);
    }

    @Override
    public ForeignWord getBy(final Long courseId, final String word) {
        return repository.findByCourseIdAndWord(courseId, word)
                .orElseThrow(() -> new NotFoundException("Word not found: " + word));
    }

    @Override
    public boolean checkIfExist(final Long courseId, final String word) {
        return repository.existsByCourseIdAndWord(courseId, word);
    }

    @Override
    public long getTotalNumber(final Long courseId) {
        return repository.countByCourseId(courseId);
    }

    @Override
    @Transactional
    public void createWordToAddLater(final Long courseId, final String word) {
        final LanguageCourse course = languageCoursesService.getCourseById(courseId);
        final String trimmedWord = word.trim();

        if (repository.findByCourseIdAndWordIgnoreCase(courseId, trimmedWord).isPresent()) {
            return;
        }

        final var wordToAddLater = wordsToAddLaterRepository.findByCourseIdAndWordIgnoreCase(courseId, trimmedWord)
                .orElseGet(() -> new WordToAddLater(course, trimmedWord));
        wordToAddLater.incrementTimesAdded();
        wordsToAddLaterRepository.save(wordToAddLater);
    }

    @Override
    public List<WordToAddLater> getAllWordsToAddLater(final Long courseId) {
        return wordsToAddLaterRepository.findAllByCourseId(courseId);
    }

    @Override
    @Transactional
    public void deleteWordsToAddLater(final Long courseId, final String word) {
        final String trimmedWord = word.trim();
        wordsToAddLaterRepository.findByCourseIdAndWordIgnoreCase(courseId, trimmedWord)
                .ifPresent(wordsToAddLaterRepository::delete);
    }

    @Override
    public Map<LocalDate, Integer> getSummaryGraph(final Long courseId) {
        final LocalDate today = LocalDate.now();
        final Map<LocalDate, Integer> summary = new HashMap<>();

        final var repositoryResult = repository.getAddedWordsStatistics(today.minusYears(1), courseId);
        for (var result : repositoryResult) {
            summary.put(result.date(), result.count().intValue());
        }

        return summary;
    }

    @Override
    @Transactional
    public void delete(final Long courseId, final String word) {
        final ForeignWord foreignWord = getBy(courseId, word);
        foreignWord.getMeanings().forEach(meaning -> spacedRepetitionService.deleteWordMeaning(meaning.getId()));
        repository.delete(foreignWord);
    }
}
