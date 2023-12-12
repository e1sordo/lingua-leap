package es.e1sordo.lingualeap.mapping;

import es.e1sordo.lingualeap.dto.AppSettingDto;
import es.e1sordo.lingualeap.dto.word.ForeignWordDetailDto;
import es.e1sordo.lingualeap.dto.word.VocabularyListDetailDto;
import es.e1sordo.lingualeap.dto.word.VocabularyListDto;
import es.e1sordo.lingualeap.dto.word.WordMeaningContextDto;
import es.e1sordo.lingualeap.dto.word.WordMeaningDto;
import es.e1sordo.lingualeap.models.AppSettingEntity;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.VocabularyList;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.models.WordMeaningContext;

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
        return new VocabularyListDto(entity.getId(), entity.getName());
    }


    public static AppSettingDto mapToDto(final AppSettingEntity entity) {
        return new AppSettingDto(
                entity.getKey(),
                entity.getValue()
        );
    }
}
