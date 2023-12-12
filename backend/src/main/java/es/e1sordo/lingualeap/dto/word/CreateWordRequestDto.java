package es.e1sordo.lingualeap.dto.word;

import java.time.LocalDate;
import java.util.List;

public record CreateWordRequestDto(LocalDate addedDate,
                                   String word,
                                   List<WordMeaningDto> meanings) {
}
