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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class VocabularyListsController {

    private final VocabularyListsService service;

    @PostMapping
    public VocabularyListDto create(@RequestBody UpsertVocabularyListRequestDto request) {
        log.info("Upsert List Request");
        return Mappings.mapToDto(service.upsertList(request));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VocabularyListDto> getAll(@RequestParam(required = false) Boolean excludeSmart) {
        log.info("Get all added lists (exclude smart: {})", excludeSmart);
        return service.getAll().stream()
                .filter(list -> excludeSmart == null || !list.isSmart())
                .map(Mappings::mapToDto)
                .toList();
    }

    @GetMapping(value = "/{listId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VocabularyListDetailDto getListDetail(@PathVariable Long listId) {
        log.info("Get list by id '{}'", listId);
        return Mappings.mapToDetailDto(service.getBy(listId));
    }

    @GetMapping(value = "/pos/{pos}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VocabularyListDetailDto getListDetailByPos(@PathVariable String pos) {
        log.info("Get list by pos '{}'", pos);
        return Mappings.mapToDetailDto(service.getByPos(pos));
    }

    @DeleteMapping(value = "/{listId}")
    public void deleteList(@PathVariable Long listId) {
        log.info("Delete list by id '{}'", listId);
        service.delete(listId);
    }
}
