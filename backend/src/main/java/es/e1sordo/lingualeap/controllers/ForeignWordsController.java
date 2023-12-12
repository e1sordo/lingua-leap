package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.word.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.word.ForeignWordDetailDto;
import es.e1sordo.lingualeap.dto.word.ForeignWordDto;
import es.e1sordo.lingualeap.mapping.Mappings;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.services.ForeignWordsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class ForeignWordsController {

    private final ForeignWordsService service;

    @PostMapping
    public void create(@RequestBody CreateWordRequestDto request) {
        log.info("Create new Word Request");
        service.createWord(request);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ForeignWordDto> getRecentlyAdded(@RequestParam(defaultValue = "false") boolean rollOut) {
        log.info("Get all recently added words");
        final Stream<ForeignWord> entities = service.getRecentlyAdded().stream();
        if (rollOut) {
            return entities.flatMap(entity -> {
                final String word = entity.getWord();
                final LocalDate added = entity.getAdded();
                return entity.getMeanings().stream()
                        .map(meaning -> new ForeignWordDto(
                                meaning.getId(),
                                word,
                                meaning.getRussianVariant(),
                                meaning.getEnglishVariant(),
                                meaning.getPos(),
                                meaning.getImageUrl(),
                                meaning.getGender(),
                                added,
                                meaning.getLearningStatus()
                        ));
            }).toList();
        } else {
            return entities.map(entity -> {
                var firstMeaning = entity.getMeanings().get(0);
                return new ForeignWordDto(
                        entity.getId(),
                        entity.getWord(),
                        firstMeaning.getRussianVariant(),
                        firstMeaning.getEnglishVariant(),
                        firstMeaning.getPos(),
                        firstMeaning.getImageUrl(),
                        firstMeaning.getGender(),
                        entity.getAdded(),
                        firstMeaning.getLearningStatus()
                );
            }).toList();
        }
    }

    @GetMapping(value = "/{word}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ForeignWordDetailDto getWordDetail(@PathVariable String word) {
        log.info("Get word '{}'", word);
        return Mappings.mapToDto(service.getBy(word));
    }

    @GetMapping(value = "/total", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> getTotalNumber() {
        log.info("Get total number of words");
        return Map.of("total", service.getTotalNumber());
    }

    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Boolean> checkIfExist(@RequestParam String word) {
        log.info("Check if word '{}' exists in DB", word);
        return Map.of("exist", service.checkIfExist(word));
    }
}
