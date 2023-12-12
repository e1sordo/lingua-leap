package es.e1sordo.lingualeap.dto.word;

import es.e1sordo.lingualeap.enums.GrammaticalGender;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;

import java.util.List;

public record WordMeaningDto(Long id,
                             String word,
                             PartOfSpeech pos,
                             GrammaticalGender gender,
                             String imageUrl,
                             String russianVariant,
                             String englishVariant,
                             String definition,
                             Integer frequency,
                             LearningStatus learningStatus,
                             List<VocabularyListDto> lists,
                             List<WordMeaningContextDto> contexts) {
}
