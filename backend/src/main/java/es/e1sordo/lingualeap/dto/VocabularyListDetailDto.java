package es.e1sordo.lingualeap.dto;

import java.util.List;

public record VocabularyListDetailDto(long id,
                                      String name,
                                      List<WordMeaningDto> words) {
}
