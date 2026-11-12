package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.UpsertVocabularyListRequestDto;
import es.e1sordo.lingualeap.dto.WordMeaningDto;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import es.e1sordo.lingualeap.enums.SmartListType;
import es.e1sordo.lingualeap.models.CourseTranslationLanguage;
import es.e1sordo.lingualeap.models.Language;
import es.e1sordo.lingualeap.models.LanguageCourse;
import es.e1sordo.lingualeap.models.VocabularyList;
import es.e1sordo.lingualeap.repositories.CourseTranslationLanguagesRepository;
import es.e1sordo.lingualeap.repositories.ForeignWordsRepository;
import es.e1sordo.lingualeap.repositories.LanguageCoursesRepository;
import es.e1sordo.lingualeap.repositories.LanguagesRepository;
import es.e1sordo.lingualeap.repositories.VocabularyListsRepository;
import es.e1sordo.lingualeap.repositories.WordMeaningsRepository;
import es.e1sordo.lingualeap.services.ForeignWordsService;
import es.e1sordo.lingualeap.services.NotFoundException;
import es.e1sordo.lingualeap.services.VocabularyListsService;
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
class VocabularyListsServiceImplTest {

    @Autowired
    private VocabularyListsRepository listsRepository;
    @Autowired
    private WordMeaningsRepository meaningsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordsToAddLaterRepository wordsToAddLaterRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordMeaningCollocationsRepository collocationsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordMeaningContextsRepository contextsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.SM2WordMeaningMetaRepository sm2Repository;
    @Autowired
    private ForeignWordsRepository wordsRepository;
    @Autowired
    private LanguagesRepository languagesRepository;
    @Autowired
    private LanguageCoursesRepository coursesRepository;
    @Autowired
    private CourseTranslationLanguagesRepository courseLanguagesRepository;
    @Autowired
    private VocabularyListsService listsService;
    @Autowired
    private ForeignWordsService wordsService;

    private LanguageCourse spanishCourse;
    private LanguageCourse englishCourse;

    @BeforeEach
    void setUp() {
        List.<CrudRepository<?, ?>>of(
                listsRepository,
                meaningsRepository,
                collocationsRepository,
                contextsRepository,
                sm2Repository,
                wordsRepository,
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

    private void createWord(final LanguageCourse course, final String word, final String russianTranslation) {
        final var meaningDto = new WordMeaningDto(
                null, null, PartOfSpeech.NOUN, null, null, Map.of("ru", russianTranslation),
                null, 1, LearningStatus.NEW, List.of(), List.of(), List.of());
        wordsService.createWord(course.getId(), new CreateWordRequestDto(LocalDate.now(), word, List.of(meaningDto)));
    }

    @Test
    @Transactional
    void regularListsAreSeparatedBetweenCourses() {
        listsService.upsertList(spanishCourse.getId(), new UpsertVocabularyListRequestDto(null, "Verbos"));
        listsService.upsertList(englishCourse.getId(), new UpsertVocabularyListRequestDto(null, "Verbos"));

        assertEquals(1, listsService.getAll(spanishCourse.getId()).size());
        assertEquals(1, listsService.getAll(englishCourse.getId()).size());
    }

    @Test
    @Transactional
    void eachCourseGetsOwnRecentlyAddedList() {
        createWord(spanishCourse, "hola", "привет");
        createWord(englishCourse, "hello", "привет");

        final VocabularyList spanishSmart = listsService.getSmartListOfRecentlyAdded(spanishCourse.getId());
        final VocabularyList englishSmart = listsService.getSmartListOfRecentlyAdded(englishCourse.getId());

        assertEquals(SmartListType.RECENTLY_ADDED, spanishSmart.getSmartType());
        assertEquals(SmartListType.RECENTLY_ADDED, englishSmart.getSmartType());
        assertNotEquals(spanishSmart.getId(), englishSmart.getId());

        // owning-side verification: recently added smart list of each course contains only its own meaning
        final var spanishMeaning = meaningsRepository
                .findAllByWordCourseIdAndPos(spanishCourse.getId(), PartOfSpeech.NOUN).get(0);
        final var englishMeaning = meaningsRepository
                .findAllByWordCourseIdAndPos(englishCourse.getId(), PartOfSpeech.NOUN).get(0);

        assertTrue(spanishMeaning.getListsContaining().stream()
                .anyMatch(list -> list.getId().equals(spanishSmart.getId())));
        assertTrue(englishMeaning.getListsContaining().stream()
                .anyMatch(list -> list.getId().equals(englishSmart.getId())));
        assertTrue(spanishMeaning.getListsContaining().stream()
                .noneMatch(list -> list.getId().equals(englishSmart.getId())));
    }

    @Test
    @Transactional
    void eachCourseGetsOwnProblemWordsList() {
        assertEquals(SmartListType.PROBLEM_WORDS,
                listsService.getSmartListOfProblemWords(spanishCourse.getId()).getSmartType());
        assertEquals(SmartListType.PROBLEM_WORDS,
                listsService.getSmartListOfProblemWords(englishCourse.getId()).getSmartType());
        assertEquals(2, listsRepository.findAll().stream().filter(VocabularyList::isSmart).count());
    }

    @Test
    @Transactional
    void meaningOfAnotherCourseCannotBeRemovedFromList() {
        createWord(spanishCourse, "hola", "привет");
        final var list = listsService.upsertList(spanishCourse.getId(), new UpsertVocabularyListRequestDto(null, "Favoritos"));

        final Long meaningId = meaningsRepository.findAll().get(0).getId();

        assertThrows(NotFoundException.class,
                () -> listsService.removeWordFromList(englishCourse.getId(), list.getId(), meaningId));
    }

    @Test
    @Transactional
    void posListsAreFilteredByCourse() {
        createWord(spanishCourse, "hola", "привет");
        createWord(englishCourse, "hello", "привет");

        assertEquals(1, listsService.getByPos(spanishCourse.getId(), "NOUN").getMeanings().size());
        assertEquals(1, listsService.getByPos(englishCourse.getId(), "NOUN").getMeanings().size());
    }

    @Test
    @Transactional
    void dateListsAreFilteredByCourse() {
        final var today = LocalDate.now();
        createWord(spanishCourse, "hola", "привет");
        createWord(englishCourse, "hello", "привет");

        assertEquals(1, listsService.getByDate(spanishCourse.getId(), today).getMeanings().size());
        assertEquals(1, listsService.getByDate(englishCourse.getId(), today).getMeanings().size());
    }

    @Test
    @Transactional
    void listCannotBeChangedThroughForeignCourseId() {
        final var list = listsService.upsertList(spanishCourse.getId(), new UpsertVocabularyListRequestDto(null, "Favoritos"));

        assertThrows(NotFoundException.class, () -> listsService.getBy(englishCourse.getId(), list.getId()));
        assertThrows(NotFoundException.class, () -> listsService.delete(englishCourse.getId(), list.getId()));
    }
}
