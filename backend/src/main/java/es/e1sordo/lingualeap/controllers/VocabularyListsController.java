package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.UpsertVocabularyListRequestDto;
import es.e1sordo.lingualeap.dto.VocabularyListDetailDto;
import es.e1sordo.lingualeap.dto.VocabularyListDto;
import es.e1sordo.lingualeap.mapping.Mappings;
import es.e1sordo.lingualeap.services.VocabularyListsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/courses/{courseId}/lists")
@RequiredArgsConstructor
public class VocabularyListsController {

    private final VocabularyListsService service;

    @PostMapping
    public VocabularyListDto create(@PathVariable Long courseId, @RequestBody UpsertVocabularyListRequestDto request) {
        log.info("Upsert List Request in course {}", courseId);
        return Mappings.mapToDto(service.upsertList(courseId, request));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VocabularyListDto> getAll(@PathVariable Long courseId,
                                          @RequestParam(required = false) Boolean excludeSmart) {
        log.info("Get all added lists of course {} (exclude smart: {})", courseId, excludeSmart);
        return service.getAll(courseId).stream()
                .filter(list -> excludeSmart == null || !list.isSmart())
                .map(Mappings::mapToDto)
                .toList();
    }

    @GetMapping(value = "/{listId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VocabularyListDetailDto getListDetail(@PathVariable Long courseId, @PathVariable Long listId) {
        log.info("Get list by id '{}' of course {}", listId, courseId);
        return Mappings.mapToDetailDto(service.getBy(courseId, listId));
    }

    @GetMapping(value = "/pos/{pos}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VocabularyListDetailDto getListDetailByPos(@PathVariable Long courseId, @PathVariable String pos) {
        log.info("Get list by pos '{}' of course {}", pos, courseId);
        return Mappings.mapToDetailDto(service.getByPos(courseId, pos));
    }

    @GetMapping(value = "/date/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VocabularyListDetailDto getListDetailByDate(@PathVariable Long courseId, @PathVariable LocalDate date) {
        log.info("Get list by date '{}' of course {}", date, courseId);
        return Mappings.mapToDetailDto(service.getByDate(courseId, date));
    }

    @DeleteMapping(value = "/{listId}/words/{wordMeaningId}")
    public void removeWordFromList(@PathVariable Long courseId,
                                   @PathVariable Long listId,
                                   @PathVariable Long wordMeaningId) {
        log.info("Remove word '{}' from list by id '{}' of course {}", wordMeaningId, listId, courseId);
        service.removeWordFromList(courseId, listId, wordMeaningId);
    }

    @DeleteMapping(value = "/{listId}")
    public void deleteList(@PathVariable Long courseId, @PathVariable Long listId) {
        log.info("Delete list by id '{}' of course {}", listId, courseId);
        service.delete(courseId, listId);
    }
}
