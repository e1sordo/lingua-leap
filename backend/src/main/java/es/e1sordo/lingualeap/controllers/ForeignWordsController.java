package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.AddedWordsStatisticsDto;
import es.e1sordo.lingualeap.dto.CountDateDto;
import es.e1sordo.lingualeap.dto.CreateWordRequestDto;
import es.e1sordo.lingualeap.dto.ForeignWordDetailDto;
import es.e1sordo.lingualeap.dto.ForeignWordDto;
import es.e1sordo.lingualeap.dto.RecentlyAddedForeignWordsPageDto;
import es.e1sordo.lingualeap.dto.WordToAddLaterDto;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

@Slf4j
@RestController
@RequestMapping("/api/courses/{courseId}/words")
@RequiredArgsConstructor
public class ForeignWordsController {

    private final ForeignWordsService service;

    @PostMapping
    public void create(@PathVariable Long courseId, @RequestBody CreateWordRequestDto request) {
        log.info("Create new Word Request for course {}", courseId);
        service.createWord(courseId, request);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public RecentlyAddedForeignWordsPageDto getRecentlyAdded(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam(defaultValue = "-1") long startsFromId,
            @RequestParam(defaultValue = "false") boolean rollOut
    ) {
        log.info("Get all recently added words. Course: {}, Page: {}, pageSize: {}, rollOut: {}", courseId, page, pageSize, rollOut);
        final Page<ForeignWord> pageOfWords = service.getRecentlyAdded(courseId, page, pageSize);
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
                                Mappings.mapTranslations(meaning),
                                ofNullable(meaning.getPos()).orElse(PartOfSpeech.PHRASE),
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
                        Mappings.mapTranslations(firstMeaning),
                        ofNullable(firstMeaning.getPos()).orElse(PartOfSpeech.PHRASE),
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
    public List<String> getAutoSuggestions(@PathVariable Long courseId, @RequestParam String word) {
        log.info("Get auto suggestions for '{}' in course {}", word, courseId);
        return service.getAutoSuggestions(courseId, word).stream().map(ForeignWord::getWord).toList();
    }

    @GetMapping(value = "/{word}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ForeignWordDetailDto getWordDetail(@PathVariable Long courseId, @PathVariable String word) {
        log.info("Get word '{}' in course {}", word, courseId);
        return Mappings.mapToDto(service.getBy(courseId, word));
    }

    @DeleteMapping(value = "/{word}")
    public void deleteWord(@PathVariable Long courseId, @PathVariable String word) {
        log.info("Delete word '{}' in course {}", word, courseId);
        service.delete(courseId, word);
    }

    @GetMapping(value = "/total", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> getTotalNumber(@PathVariable Long courseId) {
        log.info("Get total number of words in course {}", courseId);
        return Map.of("total", service.getTotalNumber(courseId));
    }

    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Boolean> checkIfExist(@PathVariable Long courseId, @RequestParam String word) {
        log.info("Check if word '{}' exists in DB of course {}", word, courseId);
        return Map.of("exist", service.checkIfExist(courseId, word));
    }

    @PostMapping(value = "/later/{word}")
    public void createWordToAddLater(@PathVariable Long courseId, @PathVariable String word) {
        log.info("Create new Word To Add Later Request in course {}", courseId);
        service.createWordToAddLater(courseId, word);
    }

    @GetMapping(value = "/later")
    public List<WordToAddLaterDto> getAllWordsToAddLater(@PathVariable Long courseId) {
        log.info("Get all words to add later in course {}", courseId);
        return service.getAllWordsToAddLater(courseId).stream()
                .map(Mappings::mapToDto)
                .toList();
    }

    @DeleteMapping(value = "/later/{word}")
    public void deleteWordToAddLater(@PathVariable Long courseId, @PathVariable String word) {
        log.info("Delete word to add later in course {}", courseId);
        service.deleteWordsToAddLater(courseId, word);
    }

    @GetMapping("/statistics")
    public AddedWordsStatisticsDto getStatistics(@PathVariable Long courseId) {
        log.info("Get added words statistics in course {}", courseId);
        final AtomicInteger addedThisWeek = new AtomicInteger();
        final AtomicInteger addedPreviousWeek = new AtomicInteger();
        final AtomicInteger addedThisMonth = new AtomicInteger();
        final AtomicInteger addedPreviousMonth = new AtomicInteger();

        final List<CountDateDto> data = service.getSummaryGraph(courseId).entrySet().stream()
                .map(Mappings::mapToDto)
                .peek(dto -> {
                    final LocalDate date = dto.date();

                    if (date.isAfter(LocalDate.now().minusDays(7))) {
                        addedThisWeek.addAndGet(dto.count());
                    } else if (date.isAfter(LocalDate.now().minusDays(14))) {
                        addedPreviousWeek.addAndGet(dto.count());
                    }

                    if (date.isAfter(LocalDate.now().minusDays(30))) {
                        addedThisMonth.addAndGet(dto.count());
                    } else if (date.isAfter(LocalDate.now().minusDays(60))) {
                        addedPreviousMonth.addAndGet(dto.count());
                    }
                })
                .toList();

        return new AddedWordsStatisticsDto(
                data,
                addedThisWeek.get(),
                addedPreviousWeek.get(),
                addedThisMonth.get(),
                addedPreviousMonth.get()
        );
    }
}
