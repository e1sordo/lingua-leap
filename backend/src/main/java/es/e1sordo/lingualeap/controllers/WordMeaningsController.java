package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.EditWordMeaningVariantsDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.mapping.Mappings;
import es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics;
import es.e1sordo.lingualeap.services.WordMeaningsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/meanings")
@RequiredArgsConstructor
public class WordMeaningsController {

    private final WordMeaningsService service;

    @PostMapping("/{meaningId}")
    public WordMeaningContextDto linkContext(@PathVariable Long meaningId, @RequestBody WordMeaningContextDto request) {
        log.info("Link new Context with existed Meaning {}", meaningId);
        return Mappings.mapToDto(service.linkContext(meaningId, request));
    }

    @PatchMapping("/{meaningId}/variants")
    public void editVariants(@PathVariable Long meaningId, @RequestBody EditWordMeaningVariantsDto request) {
        log.info("Edit Variants of Meaning {}", meaningId);
        service.editVariants(meaningId, request);
    }

    @GetMapping("/statistics")
    public List<PartOfSpeechStatistics> countMeaningsByPartOfSpeech() {
        return service.countMeaningsByPartOfSpeech();
    }
}
