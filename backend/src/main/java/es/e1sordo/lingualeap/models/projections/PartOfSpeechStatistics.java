package es.e1sordo.lingualeap.models.projections;

import es.e1sordo.lingualeap.enums.PartOfSpeech;

public record PartOfSpeechStatistics(PartOfSpeech pos, long count) {
}
