package es.e1sordo.lingualeap.dto;

import java.time.LocalDate;
import java.util.List;

public record ForeignWordDetailDto(long id,
                                   String word,
                                   LocalDate addedDate,
                                   List<WordMeaningDto> meanings) {
}
