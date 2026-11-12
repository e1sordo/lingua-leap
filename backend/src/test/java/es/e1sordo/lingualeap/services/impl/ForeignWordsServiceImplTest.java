package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.WordMeaningCollocationDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.dto.WordMeaningDto;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.models.CourseTranslationLanguage;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.Language;
import es.e1sordo.lingualeap.models.LanguageCourse;
import es.e1sordo.lingualeap.repositories.CourseTranslationLanguagesRepository;
import es.e1sordo.lingualeap.repositories.ForeignWordsRepository;
import es.e1sordo.lingualeap.repositories.LanguageCoursesRepository;
import es.e1sordo.lingualeap.repositories.LanguagesRepository;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private es.e1sordo.lingualeap.repositories.VocabularyListsRepository vocabularyListsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.SM2WordMeaningMetaRepository sm2Repository;
    @Autowired
    private LanguagesRepository languagesRepository;
    @Autowired
    private LanguageCoursesRepository coursesRepository;
    @Autowired
    private CourseTranslationLanguagesRepository courseLanguagesRepository;
    @Autowired
    private ForeignWordsService service;

    private LanguageCourse spanishCourse;
    private LanguageCourse englishCourse;

    @BeforeEach
    void setUp() {
        List.of(
                wordsRepository,
                meaningsRepository,
                meaningCollocationsRepository,
                meaningContextsRepository,
                vocabularyListsRepository,
                sm2Repository,
                wordsToAddLaterRepository,
                courseLanguagesRepository,
                coursesRepository
        ).forEach(CrudRepository::deleteAll);

        seedLanguages();
        spanishCourse = createCourse("es", List.of("ru", "en"));
        englishCourse = createCourse("en", List.of("ru"));
    }

    private void seedLanguages() {
        languagesRepository.save(new Language("es", "Spanish", "es-ES"));
        languagesRepository.save(new Language("en", "English", "en-US"));
        languagesRepository.save(new Language("fr", "French", "fr-FR"));
        languagesRepository.save(new Language("ru", "Russian", "ru-RU"));
    }

    private LanguageCourse createCourse(final String targetCode, final List<String> translationCodes) {
        final LanguageCourse course = new LanguageCourse();
        course.setTargetLanguage(languagesRepository.findById(targetCode).orElseThrow());
        course.setCreatedAt(LocalDate.now());

        for (int i = 0; i < translationCodes.size(); i++) {
            final var translationLanguage = new CourseTranslationLanguage();
            translationLanguage.setLanguage(languagesRepository.findById(translationCodes.get(i)).orElseThrow());
            translationLanguage.setDisplayOrder(i);
            course.addTranslationLanguage(translationLanguage);
        }
        return coursesRepository.save(course);
    }

    private WordMeaningDto meaningDto(final String russian, final String english) {
        final Map<String, String> translations = english == null
                ? Map.of("ru", russian)
                : Map.of("ru", russian, "en", english);
        return new WordMeaningDto(
                null,
                null,
                PartOfSpeech.ADJECTIVE,
                null,
                "http://image.png",
                translations,
                "definition",
                3,
                LearningStatus.NEW,
                List.of(),
                List.of(
                        new WordMeaningCollocationDto(
                                null,
                                "some words {|lowercase} around it",
                                null,
                                translations
                        )
                ),
                List.of(
                        new WordMeaningContextDto(null, "originalExample1", "sentenceExample1"),
                        new WordMeaningContextDto(null, "originalExample2", "sentenceExample2")
                )
        );
    }

    private CreateWordRequestDto requestOf(final String word, final WordMeaningDto... meanings) {
        return new CreateWordRequestDto(LocalDate.now(), word, List.of(meanings));
    }

    @Test
    @Transactional
    void createEntity() {
        // given
        final var countBeforeUpsert = wordsRepository.count();

        // when
        service.createWord(spanishCourse.getId(), requestOf("aburrido", meaningDto("скучный", "boring")));

        // then
        assertEquals(0, countBeforeUpsert);

        final ForeignWord entity = wordsRepository.findByCourseIdAndWord(spanishCourse.getId(), "aburrido").orElseThrow();
        assertNotNull(entity.getId());

        assertEquals(1, meaningsRepository.count());
        assertEquals(1, meaningCollocationsRepository.count());
        assertEquals(2, meaningContextsRepository.count());
    }

    @Test
    @Transactional
    void sameWordCanBeCreatedInDifferentCourses() {
        service.createWord(spanishCourse.getId(), requestOf("table", meaningDto("таблица", null)));
        service.createWord(englishCourse.getId(), requestOf("table", meaningDto("стол", null)));

        assertEquals(1, wordsRepository.countByCourseId(spanishCourse.getId()));
        assertEquals(1, wordsRepository.countByCourseId(englishCourse.getId()));
        assertEquals("таблица",
                wordsRepository.findByCourseIdAndWord(spanishCourse.getId(), "table").orElseThrow()
                        .getMeanings().get(0).getTranslation("ru"));
        assertEquals("стол",
                wordsRepository.findByCourseIdAndWord(englishCourse.getId(), "table").orElseThrow()
                        .getMeanings().get(0).getTranslation("ru"));
    }

    @Test
    @Transactional
    void repeatedCreationInSameCourseDoesNotDuplicateWord() {
        service.createWord(spanishCourse.getId(), requestOf("aburrido", meaningDto("скучный", "boring")));
        service.createWord(spanishCourse.getId(), requestOf("aburrido", meaningDto("унылый", "dull")));

        assertEquals(1, wordsRepository.countByCourseId(spanishCourse.getId()));
        assertEquals(2, meaningsRepository.findAllByWordCourseIdAndPos(spanishCourse.getId(), PartOfSpeech.ADJECTIVE).size());
    }

    @Test
    @Transactional
    void sameTranslationsDoNotCreateDuplicateMeaning() {
        service.createWord(spanishCourse.getId(), requestOf("aburrido", meaningDto("скучный", "boring")));
        service.createWord(spanishCourse.getId(), requestOf("aburrido", meaningDto("скучный", "boring")));

        assertEquals(1, meaningsRepository.count());
    }

    @Test
    @Transactional
    void differentTranslationsAllowNewMeaning() {
        service.createWord(spanishCourse.getId(), requestOf("aburrido", meaningDto("скучный", "boring")));
        service.createWord(spanishCourse.getId(), requestOf("aburrido", meaningDto("унылый", "dull")));

        assertEquals(2, meaningsRepository.count());
    }

    @Test
    @Transactional
    void emptyPrimaryTranslationIsRejected() {
        final var request = new CreateWordRequestDto(
                LocalDate.now(),
                "table",
                List.of(new WordMeaningDto(
                        null, null, PartOfSpeech.NOUN, null, null, Map.of("en", "table"),
                        null, 1, LearningStatus.NEW, List.of(), List.of(), List.of()))
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.createWord(englishCourse.getId(), request));
    }

    @Test
    @Transactional
    void secondaryTranslationCanBeMissing() {
        service.createWord(englishCourse.getId(), requestOf("table", meaningDto("стол", null)));

        final var word = wordsRepository.findByCourseIdAndWord(englishCourse.getId(), "table").orElseThrow();
        assertEquals("стол", word.getMeanings().get(0).getTranslation("ru"));
    }

    @Test
    @Transactional
    void languageOutsideCourseIsRejected() {
        final var request = new CreateWordRequestDto(
                LocalDate.now(),
                "table",
                List.of(new WordMeaningDto(
                        null, null, PartOfSpeech.NOUN, null, null,
                        Map.of("ru", "стол", "fr", "table"),
                        null, 1, LearningStatus.NEW, List.of(), List.of(), List.of()))
        );

        assertThrows(IllegalArgumentException.class,
                () -> service.createWord(englishCourse.getId(), request));
    }

    @Test
    @Transactional
    void courseScopedOperationsReturnOnlyActiveCourse() {
        service.createWord(spanishCourse.getId(), requestOf("hola", meaningDto("привет", null)));
        service.createWord(englishCourse.getId(), requestOf("hello", meaningDto("привет", null)));

        assertEquals(1, service.getTotalNumber(spanishCourse.getId()));
        assertEquals(1, service.getTotalNumber(englishCourse.getId()));
        assertTrue(service.checkIfExist(spanishCourse.getId(), "hola"));
        assertFalse(service.checkIfExist(spanishCourse.getId(), "hello"));
        assertTrue(service.getAutoSuggestions(spanishCourse.getId(), "ho").stream().anyMatch(w -> w.getWord().equals("hola")));
        assertTrue(service.getAutoSuggestions(englishCourse.getId(), "ho").stream().noneMatch(w -> w.getWord().equals("hola")));
        assertEquals(1, service.getRecentlyAdded(spanishCourse.getId(), 0, 100).getTotalElements());
        assertEquals(1, service.getSummaryGraph(spanishCourse.getId()).size());
    }

    @Test
    void createWordToAddLater() {
        // create
        service.createWordToAddLater(spanishCourse.getId(), "justo");
        service.createWordToAddLater(spanishCourse.getId(), "todo");

        // delete
        service.deleteWordsToAddLater(spanishCourse.getId(), "justo");

        // getALl
        var actualResult = service.getAllWordsToAddLater(spanishCourse.getId());

        assertEquals(1, actualResult.size());
        assertEquals("todo", actualResult.get(0).getWord());
        assertEquals(1, actualResult.get(0).getTimesAdded());
    }

    @Test
    @Transactional
    void wordsToAddLaterAreIsolatedBetweenCourses() {
        service.createWordToAddLater(spanishCourse.getId(), "table");
        service.createWordToAddLater(englishCourse.getId(), "table");

        assertEquals(1, service.getAllWordsToAddLater(spanishCourse.getId()).size());
        assertEquals(1, service.getAllWordsToAddLater(englishCourse.getId()).size());

        service.deleteWordsToAddLater(spanishCourse.getId(), "table");

        assertEquals(0, service.getAllWordsToAddLater(spanishCourse.getId()).size());
        assertEquals(1, service.getAllWordsToAddLater(englishCourse.getId()).size());
    }

    @Test
    @Transactional
    void deletingWordOfOneCourseDoesNotAffectAnotherCourse() {
        service.createWord(spanishCourse.getId(), requestOf("table", meaningDto("таблица", null)));
        service.createWord(englishCourse.getId(), requestOf("table", meaningDto("стол", null)));

        service.delete(spanishCourse.getId(), "table");

        assertFalse(service.checkIfExist(spanishCourse.getId(), "table"));
        assertTrue(service.checkIfExist(englishCourse.getId(), "table"));
    }
}
