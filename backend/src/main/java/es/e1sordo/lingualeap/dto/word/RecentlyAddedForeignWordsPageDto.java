package es.e1sordo.lingualeap.dto.word;

import java.util.List;

public record RecentlyAddedForeignWordsPageDto(boolean isLast,
                                               List<ForeignWordDto> data) {
}
