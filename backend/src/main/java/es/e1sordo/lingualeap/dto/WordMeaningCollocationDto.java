package es.e1sordo.lingualeap.dto;

public record WordMeaningCollocationDto(Long id,
                                        String pattern,
                                        String resolvedPattern,
                                        String translationRussian,
                                        String translationEnglish) {
}
