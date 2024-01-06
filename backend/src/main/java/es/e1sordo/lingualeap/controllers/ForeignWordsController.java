package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.CountDateDto;
import es.e1sordo.lingualeap.dto.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.ForeignWordDetailDto;
import es.e1sordo.lingualeap.dto.ForeignWordDto;
import es.e1sordo.lingualeap.dto.RecentlyAddedForeignWordsPageDto;
import es.e1sordo.lingualeap.mapping.Mappings;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.WordToAddLater;
import es.e1sordo.lingualeap.services.ForeignWordsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
    public RecentlyAddedForeignWordsPageDto getRecentlyAdded(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(defaultValue = "-1") long startsFromId,
            @RequestParam(defaultValue = "false") boolean rollOut
    ) {
        log.info("Get all recently added words. Page: {}, pageSize: {}, rollOut: {}", page, pageSize, rollOut);
        final Page<ForeignWord> pageOfWords = service.getRecentlyAdded(page, pageSize);
        if (pageOfWords.isLast()) {
            log.info("No more words -- last page");
        }
        final Stream<ForeignWord> entities = pageOfWords.getContent().stream();

        final List<ForeignWordDto> data;
        if (rollOut) {
            data = entities.flatMap(entity -> {
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
            data = entities.map(entity -> {
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

        if (page != 0) {
            final List<ForeignWordDto> filteredData = data.stream().dropWhile(entity -> entity.id() != startsFromId).toList();
            if (!filteredData.isEmpty()) {
                return new RecentlyAddedForeignWordsPageDto(pageOfWords.isLast(), filteredData);
            }
        }

        return new RecentlyAddedForeignWordsPageDto(pageOfWords.isLast(), data);
    }

    @GetMapping(value = "/autosuggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getAutoSuggestions(@RequestParam String word) {
        log.info("Get auto suggestions for '{}'", word);
        return service.getAutoSuggestions(word).stream().map(ForeignWord::getWord).toList();
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

    @PostMapping(value = "/later/{word}")
    public void createWordToAddLater(@PathVariable String word) {
        log.info("Create new Word To Add Later Request");
        service.createWordToAddLater(word);
    }

    @GetMapping(value = "/later")
    public List<String> getAllWordsToAddLater() {
        log.info("Get all words to add later");
        return service.getAllWordsToAddLater().stream()
                .map(WordToAddLater::getWord)
                .toList();
    }

    @DeleteMapping(value = "/later/{word}")
    public void deleteWordToAddLater(@PathVariable String word) {
        log.info("Delete word to add later");
        service.deleteWordsToAddLater(word);
    }

    @GetMapping("/summary/graph")
    public List<CountDateDto> getSummaryGraph() {
        log.info("Get summary graph");
        return service.getSummaryGraph().entrySet().stream().map(Mappings::mapToDto).toList();
    }
}
