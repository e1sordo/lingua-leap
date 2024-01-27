package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.WordMeaningCollocationDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.dto.WordMeaningDto;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.repositories.ForeignWordsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningCollocationsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningContextsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningsRepository;
import es.e1sordo.lingualeap.repositories.WordsToAddLaterRepository;
import es.e1sordo.lingualeap.services.ForeignWordsService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class ForeignWordsServiceImplTest {

    @Autowired
    private ForeignWordsRepository wordsRepository;
    @Autowired
    private WordMeaningsRepository meaningsRepository;
    @Autowired
    private WordMeaningCollocationsRepository meaningCollocationsRepository;
    @Autowired
    private WordMeaningContextsRepository meaningContextsRepository;
    @Autowired
    private WordsToAddLaterRepository wordsToAddLaterRepository;
    @Autowired
    private ForeignWordsService service;

    @BeforeEach
    void setUp() {
        List.of(
                wordsRepository,
                meaningsRepository,
                meaningCollocationsRepository,
                meaningContextsRepository,
                wordsToAddLaterRepository
        ).forEach(CrudRepository::deleteAll);
    }

    @Test
    @Transactional
    void createEntity() {
        // given
        final var countBeforeUpsert = wordsRepository.count();

        final var meanings = List.of(
                new WordMeaningDto(
                        null,
                        "aburrido",
                        PartOfSpeech.ADJECTIVE,
                        null,
                        "http://image.png",
                        "скучный",
                        "boring",
                        "definition",
                        3,
                        LearningStatus.NEW,
                        List.of(),
                        List.of(
                                new WordMeaningCollocationDto(
                                        null,
                                        "some words {|lowercase} around it",
                                        null,
                                        "перевод на русский",
                                        "translate on english"
                                )
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
        final var request = new CreateWordRequestDto(
                LocalDate.of(2022, 11, 5),
                "aburrido",
                meanings
        );

        // when
        service.createWord(request);

        // then
        assertEquals(0, countBeforeUpsert);

        final ForeignWord entity = wordsRepository.findByWord("aburrido").get();
        assertNotNull(entity.getId());

        assertEquals(1, meaningsRepository.count());
        assertEquals(1, meaningCollocationsRepository.count());
        assertEquals(2, meaningContextsRepository.count());
    }

    @Test
    void createWordToAddLater() {
        // create
        service.createWordToAddLater("justo");
        service.createWordToAddLater("todo");

        // delete
        service.deleteWordsToAddLater("justo");

        // getALl
        var actualResult = service.getAllWordsToAddLater();

        assertEquals(1, actualResult.size());
        assertEquals("todo", actualResult.get(0).getWord());
    }
}
