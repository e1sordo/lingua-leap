package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.CreateCourseRequestDto;
import es.e1sordo.lingualeap.models.CourseTranslationLanguage;
import es.e1sordo.lingualeap.models.Language;
import es.e1sordo.lingualeap.models.LanguageCourse;
import es.e1sordo.lingualeap.repositories.CourseTranslationLanguagesRepository;
import es.e1sordo.lingualeap.repositories.LanguageCoursesRepository;
import es.e1sordo.lingualeap.repositories.LanguagesRepository;
import es.e1sordo.lingualeap.services.LanguageCoursesService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class LanguageCoursesServiceImplTest {

    @Autowired
    private LanguagesRepository languagesRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordsToAddLaterRepository wordsToAddLaterRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.ForeignWordsRepository wordsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordMeaningsRepository meaningsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.VocabularyListsRepository vocabularyListsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.SM2WordMeaningMetaRepository sm2Repository;
    @Autowired
    private LanguageCoursesRepository coursesRepository;
    @Autowired
    private CourseTranslationLanguagesRepository courseLanguagesRepository;
    @Autowired
    private LanguageCoursesService service;

    @BeforeEach
    void setUp() {
        List.<CrudRepository<?, ?>>of(
                courseLanguagesRepository,
                vocabularyListsRepository,
                meaningsRepository,
                sm2Repository,
                wordsRepository,
                wordsToAddLaterRepository,
                coursesRepository,
                languagesRepository
        ).forEach(CrudRepository::deleteAll);

        languagesRepository.save(new Language("es", "Spanish", "es-ES"));
        languagesRepository.save(new Language("en", "English", "en-US"));
        languagesRepository.save(new Language("fr", "French", "fr-FR"));
        languagesRepository.save(new Language("ru", "Russian", "ru-RU"));
    }

    @Test
    void createEnglishCourseWithRussianTranslations() {
        final var dto = service.createCourse(new CreateCourseRequestDto("en", List.of("ru")));

        assertNotNull(dto.id());
        assertEquals("en", dto.targetLanguageCode());
        assertEquals(List.of("ru"), dto.translationLanguageCodes());
        assertEquals("en-US", dto.defaultSpeechLocale());
        assertEquals(0, dto.totalWords());
        assertEquals(0, dto.dueToday());
    }

    @Test
    @Transactional
    void translationOrderIsPreserved() {
        final var dto = service.createCourse(new CreateCourseRequestDto("fr", List.of("ru", "en")));

        assertEquals(List.of("ru", "en"), dto.translationLanguageCodes());

        final LanguageCourse entity = coursesRepository.findById(dto.id()).orElseThrow();
        final List<CourseTranslationLanguage> languages = entity.getTranslationLanguages();
        assertEquals(2, languages.size());
        assertEquals("ru", languages.get(0).getLanguage().getCode());
        assertEquals(0, languages.get(0).getDisplayOrder());
        assertEquals("en", languages.get(1).getLanguage().getCode());
        assertEquals(1, languages.get(1).getDisplayOrder());
    }

    @Test
    void emptyTranslationLanguagesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createCourse(new CreateCourseRequestDto("en", List.of())));
        assertThrows(IllegalArgumentException.class,
                () -> service.createCourse(new CreateCourseRequestDto("en", null)));
    }

    @Test
    void targetAmongTranslationsIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createCourse(new CreateCourseRequestDto("en", List.of("en", "ru"))));
    }

    @Test
    void duplicateTranslationsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createCourse(new CreateCourseRequestDto("fr", List.of("ru", "ru"))));
    }

    @Test
    void unknownLanguageCodeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createCourse(new CreateCourseRequestDto("de", List.of("ru"))));
        assertThrows(IllegalArgumentException.class,
                () -> service.createCourse(new CreateCourseRequestDto("en", List.of("ru", "de"))));
    }

    @Test
    void getAllLanguagesReturnsSeededLanguages() {
        final var languages = service.getAllLanguages();
        assertEquals(4, languages.size());
    }

    @Test
    void getAllCoursesReturnsCounters() {
        service.createCourse(new CreateCourseRequestDto("en", List.of("ru")));
        service.createCourse(new CreateCourseRequestDto("fr", List.of("ru", "en")));

        final var courses = service.getAllCourses();
        assertEquals(2, courses.size());
        assertTrue(courses.stream().allMatch(course -> course.totalWords() == 0 && course.dueToday() == 0));
    }
}
