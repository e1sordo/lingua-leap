package es.e1sordo.lingualeap.models;

import es.e1sordo.lingualeap.enums.PartOfSpeech;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WordMeaningCollocationTest {

    @Test
    void getResolvedPattern() {
        final WordMeaning cruelWordMeaning = new WordMeaning(
                99L,
                new ForeignWord(11L, "CRUel", LocalDate.now(), List.of()),
                PartOfSpeech.ADVERB,
                null, null, null, null, null, null, emptyList(), emptyList(), null, emptySet()
        );

        Map.of(
                "penas {} mano", "penas <span class=\"main-word\">CRUel</span> mano", // as is
                "penas {crueles}", "penas <span class=\"main-word\">crueles</span>", // use word inside braces
                "first {|lowercase} second", "first <span class=\"main-word\">cruel</span> second"
        ).forEach((pattern, resolvedPattern) -> {
            final var entity = new WordMeaningCollocation(
                    23L,
                    cruelWordMeaning,
                    pattern,
                    "translate1",
                    "translate2"
            );

            assertEquals(resolvedPattern, entity.getResolvedPattern());
        });
    }
}
