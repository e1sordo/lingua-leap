package es.e1sordo.lingualeap.mapping;

import es.e1sordo.lingualeap.dto.CountDateDto;
import es.e1sordo.lingualeap.dto.CourseDto;
import es.e1sordo.lingualeap.dto.ForeignWordDetailDto;
import es.e1sordo.lingualeap.dto.LanguageDto;
import es.e1sordo.lingualeap.dto.VocabularyListDetailDto;
import es.e1sordo.lingualeap.dto.VocabularyListDto;
import es.e1sordo.lingualeap.dto.WordMeaningCollocationDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.dto.WordMeaningDto;
import es.e1sordo.lingualeap.dto.WordToAddLaterDto;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.Language;
import es.e1sordo.lingualeap.models.LanguageCourse;
import es.e1sordo.lingualeap.models.VocabularyList;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningCollocation;
import es.e1sordo.lingualeap.models.WordMeaningContext;
import es.e1sordo.lingualeap.models.WordMeaningTranslation;
import es.e1sordo.lingualeap.models.WordToAddLater;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Mappings {

    public static WordToAddLaterDto mapToDto(WordToAddLater entity) {
        return new WordToAddLaterDto(entity.getWord(), entity.getTimesAdded());
    }

    public static LanguageDto mapToDto(final Language entity) {
        return new LanguageDto(entity.getCode(), entity.getDefaultName(), entity.getDefaultSpeechLocale());
    }

    public static CourseDto mapToDto(final LanguageCourse entity, final long totalWords, final int dueToday) {
        return new CourseDto(
                entity.getId(),
                entity.getTargetLanguage().getCode(),
                entity.getTargetLanguage().getDefaultName(),
                entity.getTargetLanguage().getDefaultSpeechLocale(),
                entity.getTranslationLanguages().stream()
                        .sorted(Comparator.comparingInt(translationLanguage -> translationLanguage.getDisplayOrder()))
                        .map(translationLanguage -> translationLanguage.getLanguage().getCode())
                        .toList(),
                totalWords,
                dueToday
        );
    }

    public static ForeignWordDetailDto mapToDto(final ForeignWord entity) {
        return new ForeignWordDetailDto(
                entity.getId(),
                entity.getWord(),
                entity.getAdded(),
                entity.getMeanings().stream().map(Mappings::mapToDto).toList()
        );
    }

    public static WordMeaningDto mapToDto(final WordMeaning entity) {
        return new WordMeaningDto(
                entity.getId(),
                entity.getWord().getWord(),
                entity.getPos(),
                entity.getGender(),
                entity.getImageUrl(),
                mapTranslations(entity),
                entity.getDefinition(),
                entity.getFrequency(),
                entity.getLearningStatus(),
                entity.getListsContaining().stream().map(Mappings::mapToDto).toList(),
                entity.getCollocations().stream().map(Mappings::mapToDto).toList(),
                entity.getContexts().stream().map(Mappings::mapToDto).toList()
        );
    }

    public static Map<String, String> mapTranslations(final WordMeaning entity) {
        final List<String> courseLanguages = entity.getWord().getCourse().getTranslationLanguages().stream()
                .sorted(Comparator.comparingInt(translationLanguage -> translationLanguage.getDisplayOrder()))
                .map(translationLanguage -> translationLanguage.getLanguage().getCode())
                .toList();

        final Map<String, String> translations = new LinkedHashMap<>();
        for (final String code : courseLanguages) {
            final String text = entity.getTranslation(code);
            if (text != null) {
                translations.put(code, text);
            }
        }
        entity.getTranslations().stream()
                .map(WordMeaningTranslation::getLanguage)
                .map(Language::getCode)
                .filter(code -> !translations.containsKey(code))
                .sorted()
                .forEach(code -> translations.put(code, entity.getTranslation(code)));
        return translations;
    }

    public static Map<String, String> mapTranslations(final WordMeaningCollocation entity,
                                                      final WordMeaning ownerMeaning) {
        final Map<String, String> translations = new LinkedHashMap<>();
        for (final String code : mapTranslations(ownerMeaning).keySet()) {
            final String text = entity.getTranslation(code);
            if (text != null) {
                translations.put(code, text);
            }
        }
        entity.getTranslations().stream()
                .map(translation -> translation.getLanguage().getCode())
                .filter(code -> !translations.containsKey(code))
                .sorted()
                .forEach(code -> translations.put(code, entity.getTranslation(code)));
        return translations;
    }

    public static WordMeaningCollocationDto mapToDto(final WordMeaningCollocation entity) {
        return new WordMeaningCollocationDto(
                entity.getId(),
                entity.getPattern(),
                entity.getResolvedPattern(),
                mapTranslations(entity, entity.getWordMeaning())
        );
    }

    public static WordMeaningContextDto mapToDto(final WordMeaningContext entity) {
        return new WordMeaningContextDto(
                entity.getId(),
                entity.getSentence(),
                entity.getTranslation()
        );
    }

    public static VocabularyListDetailDto mapToDetailDto(final VocabularyList entity) {
        return new VocabularyListDetailDto(
                entity.getId(),
                entity.getName(),
                entity.getMeanings().stream().map(Mappings::mapToDto).toList()
        );
    }

    public static VocabularyListDto mapToDto(final VocabularyList entity) {
        return new VocabularyListDto(entity.getId(), entity.getName(), entity.isSmart());
    }

    public static CountDateDto mapToDto(Map.Entry<LocalDate, Integer> localDateIntegerEntry) {
        return new CountDateDto(localDateIntegerEntry.getValue(), localDateIntegerEntry.getKey());
    }
}
