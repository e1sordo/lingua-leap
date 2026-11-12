package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.CountDateDto;
import es.e1sordo.lingualeap.dto.WordMeaningDto;
import es.e1sordo.lingualeap.mapping.Mappings;
import es.e1sordo.lingualeap.services.SpacedRepetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses/{courseId}/repetition")
@RequiredArgsConstructor
public class SpacedRepetitionController {

    private final SpacedRepetitionService service;

    @GetMapping("/today/total")
    public Map<String, Integer> getTotalForToday(@PathVariable Long courseId) {
        return Map.of("total", service.getTodayCount(courseId));
    }

    @GetMapping("/today")
    public List<WordMeaningDto> getWordsToRepeatToday(@PathVariable Long courseId) {
        return service.getTodayWords(courseId).stream().map(Mappings::mapToDto).toList();
    }

    @GetMapping("/summary/graph")
    public List<CountDateDto> getSummaryGraph(@PathVariable Long courseId) {
        return service.getSummaryGraph(courseId).entrySet().stream().map(Mappings::mapToDto).toList();
    }

    @PutMapping("/meanings/{meaningId}")
    public void scoreMeaning(@PathVariable Long courseId,
                             @PathVariable Long meaningId,
                             @RequestParam Integer newScore) {
        service.updateWordMeaningInfo(courseId, meaningId, newScore);
    }
}
