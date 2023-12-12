package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.word.UpsertVocabularyListRequestDto;
import es.e1sordo.lingualeap.dto.word.VocabularyListDetailDto;
import es.e1sordo.lingualeap.dto.word.VocabularyListDto;
import es.e1sordo.lingualeap.mapping.Mappings;
import es.e1sordo.lingualeap.services.VocabularyListsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class VocabularyListsController {

    private final VocabularyListsService service;

    // TODO add smart list for Latest words, тогда мы сможем работать над обучением в терминах Списков и только в них

    @PostMapping
    public VocabularyListDto create(@RequestBody UpsertVocabularyListRequestDto request) {
        log.info("Upsert List Request");
        return Mappings.mapToDto(service.upsertList(request));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VocabularyListDto> getAll() {
        log.info("Get all added lists");
        return service.getAll().stream()
                .map(entity -> new VocabularyListDto(entity.getId(), entity.getName()))
                .toList();
    }

    @GetMapping(value = "/{listId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VocabularyListDetailDto getListDetail(@PathVariable Long listId) {
        log.info("Get list by id '{}'", listId);
        return Mappings.mapToDetailDto(service.getBy(listId));
    }
}
