package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.word.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.word.WordMeaningContextDto;
import es.e1sordo.lingualeap.dto.word.WordMeaningDto;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.ForeignWord;
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
    @Transactional
    public void createEntity() {
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
        assertEquals(2, meaningContextsRepository.count());
    }
}
