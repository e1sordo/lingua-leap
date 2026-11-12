package es.e1sordo.lingualeap.dto;

import java.util.List;

public record CourseDto(Long id,
                        String targetLanguageCode,
                        String targetLanguageName,
                        String defaultSpeechLocale,
                        List<String> translationLanguageCodes,
                        long totalWords,
                        int dueToday) {
}
