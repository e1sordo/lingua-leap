package es.e1sordo.lingualeap.dto;

import java.util.List;

public record CreateCourseRequestDto(String targetLanguageCode,
                                     List<String> translationLanguageCodes) {
}
