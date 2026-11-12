package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.LanguageDto;
import es.e1sordo.lingualeap.mapping.Mappings;
import es.e1sordo.lingualeap.services.LanguageCoursesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/languages")
@RequiredArgsConstructor
public class LanguagesController {

    private final LanguageCoursesService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LanguageDto> getAll() {
        log.info("Get all available languages");
        return service.getAllLanguages().stream().map(Mappings::mapToDto).toList();
    }
}
