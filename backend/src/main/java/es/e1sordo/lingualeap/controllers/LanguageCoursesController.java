package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.CourseDto;
import es.e1sordo.lingualeap.dto.CreateCourseRequestDto;
import es.e1sordo.lingualeap.services.LanguageCoursesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class LanguageCoursesController {

    private final LanguageCoursesService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CourseDto> getAll() {
        log.info("Get all language courses");
        return service.getAllCourses();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDto create(@RequestBody CreateCourseRequestDto request) {
        log.info("Create new language course request: {}", request);
        return service.createCourse(request);
    }
}
