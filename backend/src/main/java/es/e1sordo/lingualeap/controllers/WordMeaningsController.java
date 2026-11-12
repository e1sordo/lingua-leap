package es.e1sordo.lingualeap.controllers;

import es.e1sordo.lingualeap.dto.EditWordMeaningTranslationsDto;
import es.e1sordo.lingualeap.dto.PatchEditOneFieldDto;
import es.e1sordo.lingualeap.dto.WordMeaningCollocationDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.mapping.Mappings;
import es.e1sordo.lingualeap.models.projections.PartOfSpeechStatistics;
import es.e1sordo.lingualeap.services.WordMeaningsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/courses/{courseId}/meanings")
@RequiredArgsConstructor
public class WordMeaningsController {

    private final WordMeaningsService service;

    @PostMapping("/{meaningId}/contexts")
    public WordMeaningContextDto linkContext(@PathVariable Long courseId,
                                             @PathVariable Long meaningId,
                                             @RequestBody WordMeaningContextDto request) {
        log.info("Link new Context with existed Meaning {} of course {}", meaningId, courseId);
        return Mappings.mapToDto(service.linkContext(courseId, meaningId, request));
    }

    @PostMapping("/{meaningId}/collocations")
    public WordMeaningCollocationDto linkCollocation(@PathVariable Long courseId,
                                                     @PathVariable Long meaningId,
                                                     @RequestBody WordMeaningCollocationDto request) {
        log.info("Link new Collocation with existed Meaning {} of course {}", meaningId, courseId);
        return Mappings.mapToDto(service.linkCollocation(courseId, meaningId, request));
    }

    @PatchMapping("/{meaningId}/translations")
    public void editTranslations(@PathVariable Long courseId,
                                 @PathVariable Long meaningId,
                                 @RequestBody EditWordMeaningTranslationsDto request) {
        log.info("Edit Translations of Meaning {} of course {}", meaningId, courseId);
        service.editTranslations(courseId, meaningId, request);
    }

    @PatchMapping("/{meaningId}/definition")
    public void editDefinition(@PathVariable Long courseId,
                               @PathVariable Long meaningId,
                               @RequestBody PatchEditOneFieldDto request) {
        log.info("Edit Definition of Meaning {} of course {}", meaningId, courseId);
        service.editDefinition(courseId, meaningId, request.newValue());
    }

    @PatchMapping("/{meaningId}/image")
    public void editImageUrl(@PathVariable Long courseId,
                             @PathVariable Long meaningId,
                             @RequestBody PatchEditOneFieldDto request) {
        log.info("Edit ImageUrl of Meaning {} of course {}", meaningId, courseId);
        service.editImageUrl(courseId, meaningId, request.newValue());
    }

    @PutMapping("/{meaningId}/collocations/{collocationId}")
    public WordMeaningCollocationDto editCollocation(@PathVariable Long courseId,
                                                     @PathVariable Long meaningId,
                                                     @PathVariable Long collocationId,
                                                     @RequestBody WordMeaningCollocationDto request) {
        log.info("Edit Collocation {} of Meaning {} of course {}", collocationId, meaningId, courseId);
        return Mappings.mapToDto(service.editCollocation(courseId, meaningId, collocationId, request));
    }

    @DeleteMapping("/{meaningId}/collocations/{collocationId}")
    public void deleteCollocation(@PathVariable Long courseId,
                                  @PathVariable Long meaningId,
                                  @PathVariable Long collocationId) {
        log.info("Delete Collocation {} of Meaning {} of course {}", collocationId, meaningId, courseId);
        service.deleteCollocation(courseId, meaningId, collocationId);
    }

    @GetMapping("/statistics")
    public List<PartOfSpeechStatistics> countMeaningsByPartOfSpeech(@PathVariable Long courseId) {
        return service.countMeaningsByPartOfSpeech(courseId);
    }
}
