package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.word.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.word.WordMeaningDto;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.VocabularyList;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.models.WordToAddLater;
import es.e1sordo.lingualeap.repositories.ForeignWordsRepository;
import es.e1sordo.lingualeap.repositories.WordsToAddLaterRepository;
import es.e1sordo.lingualeap.services.ForeignWordsService;
import es.e1sordo.lingualeap.services.SpacedRepetitionService;
import es.e1sordo.lingualeap.services.VocabularyListsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

            if (meaningDto.contexts() != null) {
                for (final var contextDto : meaningDto.contexts()) {
                    final var context = new WordMeaningContext();

                    context.setSentence(contextDto.sentence().trim());
                    context.setTranslation(contextDto.translation().trim());
                    context.setWordMeaning(meaning);

                    meaning.addContext(context);
                }
            }

            word.addMeaning(meaning);
        }

        final ForeignWord persistedWord = repository.save(word);

        addMeaningToSpacedRepetition(persistedWord.getMeanings());
        wordsToAddLaterRepository.deleteById(persistedWord.getWord());
    }

    private static WordMeaning getWordMeaning(final WordMeaningDto meaningDto, final ForeignWord word) {
        final var meaning = new WordMeaning();

        meaning.setPos(meaningDto.pos());
        if (PartOfSpeech.NOUN == meaningDto.pos()) {
            meaning.setGender(meaningDto.gender());
        }
        meaning.setImageUrl(meaningDto.imageUrl());
        meaning.setRussianVariant(meaningDto.russianVariant());
        meaning.setEnglishVariant(meaningDto.englishVariant());
        meaning.setDefinition(meaningDto.definition());
        meaning.setFrequency(meaningDto.frequency());
        meaning.setLearningStatus(LearningStatus.NEW);
        meaning.setWord(word);

        return meaning;
    }

    @Override
    public List<ForeignWord> getRecentlyAdded() {
        return repository.findFirst100ByOrderByAddedDesc();
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
        wordsToAddLaterRepository.save(new WordToAddLater(word));
    }

    @Override
    public List<WordToAddLater> getAllWordsToAddLater() {
        return wordsToAddLaterRepository.findAll();
    }

    @Override
    public void deleteWordsToAddLater(final String word) {
        wordsToAddLaterRepository.deleteById(word);
    }

    private void addMeaningToSpacedRepetition(List<WordMeaning> meanings) {
        meanings.forEach(spacedRepetitionService::addWordMeaning);
    }
}
