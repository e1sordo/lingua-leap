package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.word.WordMeaningDto;
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
@RequestMapping("/api/repetition")
@RequiredArgsConstructor
public class SpacedRepetitionController {

    private final SpacedRepetitionService service;

    @GetMapping("/today/total")
    public Map<String, Integer> getTotalForToday() {
        return Map.of("total", service.getTodayCount());
    }

    @GetMapping("/today")
    public List<WordMeaningDto> getWordsToRepeatToday() {
        return service.getTodayWords().stream().map(Mappings::mapToDto).toList();
    }

    @PutMapping("/meanings/{meaningId}")
    public void scoreMeaning(@PathVariable Long meaningId, @RequestParam Integer newScore) {
        service.updateWordMeaningInfo(meaningId, newScore);
    }
}
