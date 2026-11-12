package es.e1sordo.lingualeap.services;

import es.e1sordo.lingualeap.dto.CourseDto;
import es.e1sordo.lingualeap.dto.CreateCourseRequestDto;
import es.e1sordo.lingualeap.models.Language;
import es.e1sordo.lingualeap.models.LanguageCourse;

import java.util.List;

public interface LanguageCoursesService {

    List<Language> getAllLanguages();

    List<CourseDto> getAllCourses();

    CourseDto createCourse(CreateCourseRequestDto request);

    LanguageCourse getCourseById(Long courseId);
}
