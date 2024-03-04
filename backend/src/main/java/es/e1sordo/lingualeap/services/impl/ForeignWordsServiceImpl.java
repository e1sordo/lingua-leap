package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.WordMeaningDto;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.VocabularyList;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningCollocation;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.models.WordToAddLater;
import es.e1sordo.lingualeap.repositories.ForeignWordsRepository;
import es.e1sordo.lingualeap.repositories.WordsToAddLaterRepository;
import es.e1sordo.lingualeap.services.ForeignWordsService;
import es.e1sordo.lingualeap.services.SpacedRepetitionService;
import es.e1sordo.lingualeap.services.VocabularyListsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class ForeignWordsServiceImpl implements ForeignWordsService {

    private final VocabularyListsService vocabularyListsService;
    private final SpacedRepetitionService spacedRepetitionService;
    private final ForeignWordsRepository repository;
    private final WordsToAddLaterRepository wordsToAddLaterRepository;

    @Override
    public void createWord(CreateWordRequestDto request) {
        final var word = new ForeignWord();

        word.setAdded(request.addedDate());
        word.setWord(request.word());

        for (WordMeaningDto meaningDto : request.meanings()) {
            final var meaning = getWordMeaning(meaningDto, word);

            if (meaningDto.lists() != null) {
                for (final var listDto : meaningDto.lists()) {
                    final var list = (listDto.id() != null && listDto.id() > 0)
                            ? vocabularyListsService.getBy(listDto.id())
                            : new VocabularyList();

                    list.setName(listDto.name());
                    list.addMeaning(meaning);

                    meaning.addList(list);
                }
            }

            addMeaningToSmartListOfRecentlyAdded(meaning);

            if (meaningDto.collocations() != null) {
                for (final var collocationDto : meaningDto.collocations()) {
                    final String trimmedPattern = collocationDto.pattern().trim();
                    if (trimmedPattern.isBlank()) {
                        continue;
                    }

                    final var collocation = new WordMeaningCollocation();

                    collocation.setPattern(trimmedPattern);
                    collocation.setTranslationRussian(collocationDto.translationRussian().trim());
                    collocation.setTranslationEnglish(collocationDto.translationEnglish().trim());
                    collocation.setWordMeaning(meaning);

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

        addMeaningToSpacedRepetition(persistedWord.getMeanings());
        wordsToAddLaterRepository.findByWordIgnoreCase(persistedWord.getWord())
                .ifPresent(wordsToAddLaterRepository::delete);
    }

    private void addMeaningToSmartListOfRecentlyAdded(final WordMeaning meaning) {
        final VocabularyList smartList = vocabularyListsService.getSmartListOfRecentlyAdded();
        meaning.addList(smartList);
    }

    private static WordMeaning getWordMeaning(final WordMeaningDto meaningDto, final ForeignWord word) {
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
        meaning.setRussianVariant(ofNullable(meaningDto.russianVariant())
                .orElseThrow(() -> new IllegalArgumentException("Missing russian variant")));
        meaning.setEnglishVariant(ofNullable(meaningDto.englishVariant())
                .orElseThrow(() -> new IllegalArgumentException("Missing english variant")));
        meaning.setDefinition(meaningDto.definition());
        meaning.setFrequency(meaningDto.frequency());
        meaning.setLearningStatus(LearningStatus.NEW);
        meaning.setWord(word);

        return meaning;
    }

    @Override
    public Page<ForeignWord> getRecentlyAdded(final int page, final int pageSize) {
        final Pageable pageable = Pageable.ofSize(pageSize).withPage(page);
        return repository.findByOrderByAddedDesc(pageable);
    }

    @Override
    public List<ForeignWord> getAutoSuggestions(final String query) {
        return repository.findFirst10ByWordContaining(query);
    }

    @Override
    public ForeignWord getBy(final String word) {
        return repository.findByWord(word).get();
    }

    @Override
    public boolean checkIfExist(final String word) {
        return repository.existsByWord(word);
    }

    @Override
    public long getTotalNumber() {
        return repository.count();
    }

    @Override
    public void createWordToAddLater(final String word) {
        final String trimmedWord = word.trim();

        if (repository.findByWord(trimmedWord).isPresent()) {
            return;
        }

        final int timesAdded = wordsToAddLaterRepository.findByWordIgnoreCase(trimmedWord)
                .map(WordToAddLater::getTimesAdded)
                .orElse(1);
        wordsToAddLaterRepository.save(new WordToAddLater(word.trim(), timesAdded));
    }

    @Override
    public List<WordToAddLater> getAllWordsToAddLater() {
        return wordsToAddLaterRepository.findAll();
    }

    @Override
    public void deleteWordsToAddLater(final String word) {
        final String trimmedWord = word.trim();
        wordsToAddLaterRepository.deleteAllById(List.of(trimmedWord, trimmedWord.toLowerCase()));
    }

    @Override
    public Map<LocalDate, Integer> getSummaryGraph() {
        final LocalDate today = LocalDate.now();
        final Map<LocalDate, Integer> summary = new HashMap<>();

        final var repositoryResult = repository.getAddedWordsStatistics(today.minusYears(1));
        for (var result : repositoryResult) {
            summary.put(result.date(), result.count().intValue());
        }

        return summary;
    }

    @Override
    public void delete(final String word) {
        final ForeignWord foreignWord = getBy(word);
        foreignWord.getMeanings().forEach(meaning -> spacedRepetitionService.deleteWordMeaning(meaning.getId()));
        repository.delete(foreignWord);
    }

    private void addMeaningToSpacedRepetition(List<WordMeaning> meanings) {
        meanings.forEach(spacedRepetitionService::addWordMeaning);
    }
}
