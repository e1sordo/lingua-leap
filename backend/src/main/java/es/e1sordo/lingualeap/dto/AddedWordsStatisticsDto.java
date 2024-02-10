package es.e1sordo.lingualeap.dto;

import java.util.List;

public record AddedWordsStatisticsDto(List<CountDateDto> summary,
                                      int addedThisWeek,
                                      int addedPreviousWeek,
                                      int addedThisMonth,
                                      int addedPreviousMonth) {
}
