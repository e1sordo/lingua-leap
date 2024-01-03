package es.e1sordo.lingualeap.dto;

import java.util.List;

public record RecentlyAddedForeignWordsPageDto(boolean isLast,
                                               List<ForeignWordDto> data) {
}
