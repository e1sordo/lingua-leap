package es.e1sordo.lingualeap.models;

import es.e1sordo.lingualeap.enums.PartOfSpeech;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WordMeaningCollocationTest {

    @Test
    void getResolvedPattern() {
        final var entity = new WordMeaningCollocation(
                23L,
                new WordMeaning(
                        99L,
                        new ForeignWord(
                                11L,
                                "MY WORD",
                                LocalDate.now(),
                                List.of()
                        ),
                        PartOfSpeech.ADVERB,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        emptyList(),
                        emptyList(),
                        null,
                        emptySet()
                ),
                "first {|lowercase} second",
                "translate1",
                "translate2"
        );

        assertEquals("first <span class=\"main-word\">my word</span> second", entity.getResolvedPattern());
    }
}
