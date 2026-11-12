package es.e1sordo.lingualeap.dto;

import java.util.Map;

public record WordMeaningCollocationDto(Long id,
                                        String pattern,
                                        String resolvedPattern,
                                        Map<String, String> translations) {
}
