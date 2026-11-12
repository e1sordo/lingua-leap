package es.e1sordo.lingualeap.dto;

import es.e1sordo.lingualeap.enums.GrammaticalGender;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;

import java.time.LocalDate;
import java.util.Map;

public record ForeignWordDto(long id,
                             String word,
                             Map<String, String> translations,
                             PartOfSpeech pos,
                             String imageUrl,
                             GrammaticalGender gender,
                             LocalDate addedDate,
                             LearningStatus learningStatus) {
}
