package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.dto.CourseDto;
import es.e1sordo.lingualeap.dto.CreateCourseRequestDto;
import es.e1sordo.lingualeap.mapping.Mappings;
import es.e1sordo.lingualeap.models.CourseTranslationLanguage;
import es.e1sordo.lingualeap.models.Language;
import es.e1sordo.lingualeap.models.LanguageCourse;
import es.e1sordo.lingualeap.repositories.ForeignWordsRepository;
import es.e1sordo.lingualeap.repositories.LanguagesRepository;
import es.e1sordo.lingualeap.repositories.LanguageCoursesRepository;
import es.e1sordo.lingualeap.repositories.SM2WordMeaningMetaRepository;
import es.e1sordo.lingualeap.services.LanguageCoursesService;
import es.e1sordo.lingualeap.services.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageCoursesServiceImpl implements LanguageCoursesService {

    private final LanguagesRepository languagesRepository;
    private final LanguageCoursesRepository coursesRepository;
    private final ForeignWordsRepository wordsRepository;
    private final SM2WordMeaningMetaRepository sm2Repository;

    @Override
    public List<Language> getAllLanguages() {
        return languagesRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        return coursesRepository.findAll().stream()
                .map(course -> Mappings.mapToDto(
                        course,
                        wordsRepository.countByCourseId(course.getId()),
                        getDueTodayCount(course.getId())))
                .toList();
    }

    private int getDueTodayCount(final Long courseId) {
        return sm2Repository.countByWordWordCourseIdAndPlannedReviewLessThanEqual(courseId, LocalDate.now());
    }

    @Override
    @Transactional
    public CourseDto createCourse(final CreateCourseRequestDto request) {
        final String targetCode = validateTargetLanguageCode(request.targetLanguageCode());

        final List<String> translationCodes = request.translationLanguageCodes();
        if (translationCodes == null || translationCodes.isEmpty()) {
            throw new IllegalArgumentException("Course must have at least one translation language");
        }
        if (translationCodes.stream().anyMatch(code -> code == null || code.isBlank())) {
            throw new IllegalArgumentException("Translation language codes cannot be empty");
        }
        if (translationCodes.contains(targetCode)) {
            throw new IllegalArgumentException("Target language cannot be among translation languages");
        }
        if (new HashSet<>(translationCodes).size() != translationCodes.size()) {
            throw new IllegalArgumentException("Translation languages must not contain duplicates");
        }

        final LanguageCourse course = new LanguageCourse();
        course.setTargetLanguage(getLanguageOrThrow(targetCode));
        course.setCreatedAt(LocalDate.now());

        for (int i = 0; i < translationCodes.size(); i++) {
            final var translationLanguage = new CourseTranslationLanguage();
            translationLanguage.setLanguage(getLanguageOrThrow(translationCodes.get(i)));
            translationLanguage.setDisplayOrder(i);
            course.addTranslationLanguage(translationLanguage);
        }

        final LanguageCourse saved = coursesRepository.save(course);
        log.info("Created new course: target '{}', translations {}", targetCode, translationCodes);
        return Mappings.mapToDto(saved, 0, 0);
    }

    private String validateTargetLanguageCode(final String targetLanguageCode) {
        if (targetLanguageCode == null || targetLanguageCode.isBlank()) {
            throw new IllegalArgumentException("Target language code is required");
        }
        getLanguageOrThrow(targetLanguageCode);
        return targetLanguageCode;
    }

    private Language getLanguageOrThrow(final String code) {
        return languagesRepository.findById(code.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Unknown language code: " + code));
    }

    @Override
    public LanguageCourse getCourseById(final Long courseId) {
        return coursesRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));
    }
}
