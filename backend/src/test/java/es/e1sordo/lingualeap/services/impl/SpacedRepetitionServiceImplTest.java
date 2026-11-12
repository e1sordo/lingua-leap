package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.WordMeaningDto;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.enums.SmartListType;
import es.e1sordo.lingualeap.models.CourseTranslationLanguage;
import es.e1sordo.lingualeap.models.Language;
import es.e1sordo.lingualeap.models.LanguageCourse;
import es.e1sordo.lingualeap.repositories.CourseTranslationLanguagesRepository;
import es.e1sordo.lingualeap.repositories.ForeignWordsRepository;
import es.e1sordo.lingualeap.repositories.LanguageCoursesRepository;
import es.e1sordo.lingualeap.repositories.LanguagesRepository;
import es.e1sordo.lingualeap.repositories.SM2WordMeaningMetaRepository;
import es.e1sordo.lingualeap.repositories.VocabularyListsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningsRepository;
import es.e1sordo.lingualeap.services.ForeignWordsService;
import es.e1sordo.lingualeap.services.NotFoundException;
import es.e1sordo.lingualeap.services.SpacedRepetitionService;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class SpacedRepetitionServiceImplTest {

    @Autowired
    private SM2WordMeaningMetaRepository sm2Repository;
    @Autowired
    private WordMeaningsRepository meaningsRepository;
    @Autowired
    private ForeignWordsRepository wordsRepository;
    @Autowired
    private VocabularyListsRepository listsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordsToAddLaterRepository wordsToAddLaterRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordMeaningCollocationsRepository collocationsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordMeaningContextsRepository contextsRepository;
    @Autowired
    private LanguagesRepository languagesRepository;
    @Autowired
    private LanguageCoursesRepository coursesRepository;
    @Autowired
    private CourseTranslationLanguagesRepository courseLanguagesRepository;
    @Autowired
    private SpacedRepetitionService repetitionService;
    @Autowired
    private ForeignWordsService wordsService;

    private LanguageCourse spanishCourse;
    private LanguageCourse englishCourse;

    @BeforeEach
    void setUp() {
        List.<CrudRepository<?, ?>>of(
                sm2Repository,
                meaningsRepository,
                collocationsRepository,
                contextsRepository,
                wordsRepository,
                listsRepository,
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

    private Long createWordWithMeaning(final LanguageCourse course, final String word) {
        final var meaningDto = new WordMeaningDto(
                null, null, PartOfSpeech.NOUN, null, null, Map.of("ru", "перевод " + word),
                null, 1, LearningStatus.NEW, List.of(), List.of(), List.of());
        wordsService.createWord(course.getId(), new CreateWordRequestDto(LocalDate.now(), word, List.of(meaningDto)));
        return meaningsRepository.findAllByWordCourseIdAndPos(course.getId(), PartOfSpeech.NOUN).stream()
                .filter(m -> m.getWord().getWord().equals(word))
                .findFirst()
                .orElseThrow()
                .getId();
    }

    @Test
    @Transactional
    void todayCountIsSeparatePerCourse() {
        createWordWithMeaning(spanishCourse, "hola");
        createWordWithMeaning(englishCourse, "hello");

        assertEquals(1, repetitionService.getTodayCount(spanishCourse.getId()));
        assertEquals(1, repetitionService.getTodayCount(englishCourse.getId()));
        assertEquals(1, repetitionService.getTodayWords(spanishCourse.getId()).size());
        assertEquals(1, repetitionService.getTodayWords(englishCourse.getId()).size());
    }

    @Test
    @Transactional
    void summaryGraphIsSeparatePerCourse() {
        createWordWithMeaning(spanishCourse, "hola");
        createWordWithMeaning(englishCourse, "hello");

        assertEquals(1, repetitionService.getSummaryGraph(spanishCourse.getId()).size());
        assertEquals(1, repetitionService.getSummaryGraph(englishCourse.getId()).size());
    }

    @Test
    @Transactional
    void scoreThroughRightCourseChangesOnlySelectedMeaning() {
        final Long spanishMeaningId = createWordWithMeaning(spanishCourse, "hola");
        final Long englishMeaningId = createWordWithMeaning(englishCourse, "hello");

        repetitionService.updateWordMeaningInfo(spanishCourse.getId(), spanishMeaningId, 5);

        assertEquals(1, sm2Repository.findByWordId(spanishMeaningId).orElseThrow().getTotalRepetitions());
        assertEquals(0, sm2Repository.findByWordId(englishMeaningId).orElseThrow().getTotalRepetitions());
    }

    @Test
    @Transactional
    void scoreThroughForeignCourseIsNotFoundAndDoesNotChangeSm2() {
        final Long spanishMeaningId = createWordWithMeaning(spanishCourse, "hola");

        assertThrows(NotFoundException.class,
                () -> repetitionService.updateWordMeaningInfo(englishCourse.getId(), spanishMeaningId, 5));

        assertEquals(0, sm2Repository.findByWordId(spanishMeaningId).orElseThrow().getTotalRepetitions());
    }

    @Test
    @Transactional
    void problemWordsAreAddedToListOfSameCourse() {
        final Long spanishMeaningId = createWordWithMeaning(spanishCourse, "hola");

        repetitionService.updateWordMeaningInfo(spanishCourse.getId(), spanishMeaningId, 1);
        repetitionService.updateWordMeaningInfo(spanishCourse.getId(), spanishMeaningId, 2);

        final var problemList = listsRepository
                .findByCourseIdAndSmartType(spanishCourse.getId(), SmartListType.PROBLEM_WORDS)
                .orElseThrow();

        // owning-side verification: meaning was added to the problem list of its own course
        final var meaning = meaningsRepository.findById(spanishMeaningId).orElseThrow();
        assertTrue(meaning.getListsContaining().stream()
                .anyMatch(list -> list.getId().equals(problemList.getId())));
        assertThrows(Exception.class,
                () -> listsRepository.findByCourseIdAndSmartType(englishCourse.getId(), SmartListType.PROBLEM_WORDS)
                        .orElseThrow());
    }

    @Test
    @Transactional
    void scoreInvalidGradeIsRejected() {
        final Long meaningId = createWordWithMeaning(spanishCourse, "hola");

        assertThrows(IllegalArgumentException.class,
                () -> repetitionService.updateWordMeaningInfo(spanishCourse.getId(), meaningId, 6));

        final var meta = sm2Repository.findByWordId(meaningId).orElseThrow();
        assertNotEquals(6, meta.getPreviousUserGrade());
        assertEquals(0, meta.getTotalRepetitions());
    }
}
