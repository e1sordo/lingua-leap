package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.CreateCourseRequestDto;
import es.e1sordo.lingualeap.models.CourseTranslationLanguage;
import es.e1sordo.lingualeap.models.Language;
import es.e1sordo.lingualeap.models.LanguageCourse;
import es.e1sordo.lingualeap.repositories.CourseTranslationLanguagesRepository;
import es.e1sordo.lingualeap.repositories.LanguageCoursesRepository;
import es.e1sordo.lingualeap.repositories.LanguagesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ForeignWordsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private LanguagesRepository languagesRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordsToAddLaterRepository wordsToAddLaterRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.VocabularyListsRepository vocabularyListsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.ForeignWordsRepository wordsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordMeaningsRepository meaningsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordMeaningCollocationsRepository collocationsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.WordMeaningContextsRepository contextsRepository;
    @Autowired
    private es.e1sordo.lingualeap.repositories.SM2WordMeaningMetaRepository sm2Repository;
    @Autowired
    private LanguageCoursesRepository coursesRepository;
    @Autowired
    private CourseTranslationLanguagesRepository courseLanguagesRepository;

    private Long courseId;

    @BeforeEach
    void setUp() {
        List.<CrudRepository<?, ?>>of(
                collocationsRepository,
                contextsRepository,
                sm2Repository,
                meaningsRepository,
                vocabularyListsRepository,
                wordsToAddLaterRepository,
                wordsRepository,
                courseLanguagesRepository,
                coursesRepository,
                languagesRepository
        ).forEach(CrudRepository::deleteAll);

        languagesRepository.save(new Language("es", "Spanish", "es-ES"));
        languagesRepository.save(new Language("en", "English", "en-US"));
        languagesRepository.save(new Language("ru", "Russian", "ru-RU"));

        final LanguageCourse course = new LanguageCourse();
        course.setTargetLanguage(languagesRepository.findById("es").orElseThrow());
        course.setCreatedAt(LocalDate.now());
        final var ru = new CourseTranslationLanguage();
        ru.setLanguage(languagesRepository.findById("ru").orElseThrow());
        ru.setDisplayOrder(0);
        course.addTranslationLanguage(ru);
        final var en = new CourseTranslationLanguage();
        en.setLanguage(languagesRepository.findById("en").orElseThrow());
        en.setDisplayOrder(1);
        course.addTranslationLanguage(en);
        courseId = coursesRepository.save(course).getId();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateWordToAddLater() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/courses/{courseId}/words/later/{word}", courseId, "example")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllWordsToAddLater() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/courses/{courseId}/words/later", courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDeleteWordToAddLater() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/courses/{courseId}/words/later/{word}", courseId, "example")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void oldGlobalWordsUrlIsNotUsedAnymore() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/words/later")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void unknownCourseReturnsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/courses/{courseId}/words/total", 999999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createWordReturnsTranslationsMap() throws Exception {
        final String body = """
                {
                  "word": "aburrido",
                  "addedDate": "2026-11-12",
                  "meanings": [
                    {
                      "pos": "ADJECTIVE",
                      "gender": null,
                      "translations": { "ru": "скучный", "en": "boring" },
                      "definition": null,
                      "frequency": 1,
                      "collocations": [],
                      "contexts": [],
                      "lists": []
                    }
                  ]
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/courses/{courseId}/words", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/courses/{courseId}/words/{word}", courseId, "aburrido")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.meanings[0].translations.ru").value("скучный"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.meanings[0].translations.en").value("boring"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void languagesAndCoursesEndpointsWork() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/languages")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].code").exists());

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].targetLanguageCode").value("es"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].translationLanguageCodes[0]").value("ru"));

        final String createBody = """
                { "targetLanguageCode": "en", "translationLanguageCodes": ["ru"] }
                """;
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.targetLanguageCode").value("en"));
    }
}
