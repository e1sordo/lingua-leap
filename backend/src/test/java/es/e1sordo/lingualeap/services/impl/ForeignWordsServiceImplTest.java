package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.word.UpsertWordRequestDto;
import es.e1sordo.lingualeap.dto.word.VocabularyListDto;
import es.e1sordo.lingualeap.dto.word.WordMeaningContextDto;
import es.e1sordo.lingualeap.dto.word.WordMeaningDto;
import es.e1sordo.lingualeap.enums.GrammaticalGender;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.repositories.ForeignWordsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningContextsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningsRepository;
import es.e1sordo.lingualeap.services.ForeignWordsService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ForeignWordsServiceImplTest {

    @Autowired
    private ForeignWordsRepository wordsRepository;
    @Autowired
    private WordMeaningsRepository meaningsRepository;
    @Autowired
    private WordMeaningContextsRepository meaningContextsRepository;
    @Autowired
    private ForeignWordsService service;

    @BeforeEach
    void setUp() {
        List.of(
                wordsRepository,
                meaningsRepository,
                meaningContextsRepository
        ).forEach(CrudRepository::deleteAll);
    }

    @Test
    public void whenNullId_thenCreateEntity() {
        // given
        final var countBeforeUpsert = wordsRepository.count();

        final var meanings = List.of(
                new WordMeaningDto(
                        null,
                        PartOfSpeech.ADJECTIVE,
                        null,
                        "http://image.png",
                        "скучный",
                        "boring",
                        "definition",
                        3,
                        LearningStatus.NEW,
                        List.of(
                                new VocabularyListDto(null, "My First List")
                        ),
                        List.of(
                                new WordMeaningContextDto(
                                        null,
                                        "originalExample1",
                                        "sentenceExample1"
                                ),
                                new WordMeaningContextDto(
                                        null,
                                        "originalExample2",
                                        "sentenceExample2"
                                )
                        )
                )
        );
        final var request = new UpsertWordRequestDto(
                null,
                LocalDate.of(2022, 11, 5),
                "aburrido",
                meanings
        );

        // when
        service.upsertWord(request);

        // then
        assertEquals(0, countBeforeUpsert);

        final ForeignWord entity = wordsRepository.findByWord("aburrido").get();
        assertNotNull(entity.getId());

        assertEquals(1, meaningsRepository.count());
        assertEquals(2, meaningContextsRepository.count());
    }

    @Test
    @Transactional
    public void whenIdNotNull_thenUpdateEntity() {
        // given
        final var word = new ForeignWord(
                null, "hombre", LocalDate.of(2023, 5, 19), new ArrayList<>()
        );
        final var meaning = new WordMeaning(
                null,
                word,
                PartOfSpeech.ADJECTIVE,
                GrammaticalGender.NEUTER,
                "http://image.png",
                "вариант",
                "variant",
                "def",
                4,
                new ArrayList<>(),
                LearningStatus.NEW,
                new HashSet<>()
        );
        final var context = new WordMeaningContext(
                null, meaning, "original1", "translate1"
        );
        meaning.addContext(context);
        word.addMeaning(meaning);
        final ForeignWord persistedWord = wordsRepository.save(word);
        final WordMeaning persistedMeaning = persistedWord.getMeanings().get(0);
        final WordMeaningContext persistedContext = persistedMeaning.getContexts().get(0);

        final var meaningsRequest = List.of(
                new WordMeaningDto(
                        persistedMeaning.getId(),
                        PartOfSpeech.NOUN,
                        GrammaticalGender.MASCULINE,
                        "https://image.gif",
                        "скучный",
                        "boring",
                        "definition",
                        3,
                        LearningStatus.NEW,
                        List.of(
                                new VocabularyListDto(null, "My First List")
                        ),
                        List.of(
                                new WordMeaningContextDto(
                                        persistedContext.getId(),
                                        "originalExample1",
                                        "sentenceExample1"
                                ),
                                new WordMeaningContextDto(
                                        null,
                                        "originalExample2",
                                        "sentenceExample2"
                                )
                        )
                )
        );
        final var wordRequest = new UpsertWordRequestDto(
                persistedWord.getId(),
                LocalDate.of(2022, 11, 5),
                "aburrido",
                meaningsRequest
        );

        // when
        service.upsertWord(wordRequest);

        // then
        assertTrue(wordsRepository.findByWord("hombre").isEmpty());

        final ForeignWord actualEntity = wordsRepository.findByWord("aburrido").get();
        assertNotNull(actualEntity.getId());

        assertEquals(1, meaningsRepository.count());
        assertEquals(2, meaningContextsRepository.count());

//        final var expectedContext1 = new WordMeaningContext(
//                2L, null, "originalExample1", "sentenceExample1"
//        );
//        final var expectedContext2 = new WordMeaningContext(
//                3L, null, "originalExample2", "originalExample2"
//        );
//        final var expectedMeaning = new WordMeaning(
//                2L,
//                null,
//                PartOfSpeech.NOUN,
//                GrammaticalGender.MASCULINE,
//                "https://image.gif",
//                "скучный",
//                "boring",
//                "definition",
//                3,
//                List.of(expectedContext1, expectedContext2)
//        );
//        final var expectedWord = new ForeignWord(
//                persistedWord.getId(), "aburrido", LocalDate.of(2022, 11, 5), List.of(expectedMeaning)
//        );
//        expectedMeaning.setWord(expectedWord);
//        expectedContext1.setWordMeaning(expectedMeaning);
//        expectedContext2.setWordMeaning(expectedMeaning);
//
//        assertEquals(expectedWord, actualEntity);
    }
}
