package es.e1sordo.lingualeap.dto;

import es.e1sordo.lingualeap.enums.GrammaticalGender;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;

import java.util.List;
import java.util.Map;

public record WordMeaningDto(Long id,
                             String word,
                             PartOfSpeech pos,
                             GrammaticalGender gender,
                             String imageUrl,
                             Map<String, String> translations,
                             String definition,
                             Integer frequency,
                             LearningStatus learningStatus,
                             List<VocabularyListDto> lists,
                             List<WordMeaningCollocationDto> collocations,
                             List<WordMeaningContextDto> contexts) {
}
