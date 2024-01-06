package es.e1sordo.lingualeap.mapping;

import es.e1sordo.lingualeap.dto.ForeignWordDetailDto;
import es.e1sordo.lingualeap.dto.CountDateDto;
import es.e1sordo.lingualeap.dto.VocabularyListDetailDto;
import es.e1sordo.lingualeap.dto.VocabularyListDto;
import es.e1sordo.lingualeap.dto.WordMeaningContextDto;
import es.e1sordo.lingualeap.dto.WordMeaningDto;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.VocabularyList;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningContext;

import java.time.LocalDate;
import java.util.Map;

public final class Mappings {


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
                entity.getRussianVariant(),
                entity.getEnglishVariant(),
                entity.getDefinition(),
                entity.getFrequency(),
                entity.getLearningStatus(),
                entity.getListsContaining().stream().map(Mappings::mapToDto).toList(),
                entity.getContexts().stream().map(Mappings::mapToDto).toList()
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
