package es.e1sordo.lingualeap.models;

import es.e1sordo.lingualeap.enums.PartOfSpeech;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WordMeaningCollocationTest {

    private static WordMeaning meaningOf(final String word) {
        final var foreignWord = new ForeignWord();
        foreignWord.setId(11L);
        foreignWord.setWord(word);
        foreignWord.setAdded(LocalDate.now());

        final var meaning = new WordMeaning();
        meaning.setId(99L);
        meaning.setWord(foreignWord);
        meaning.setPos(PartOfSpeech.ADVERB);
        return meaning;
    }

    private static WordMeaningCollocation collocationOf(final String pattern, final WordMeaning meaning) {
        final var collocation = new WordMeaningCollocation();
        collocation.setId(23L);
        collocation.setWordMeaning(meaning);
        collocation.setPattern(pattern);
        return collocation;
    }

    @Test
    void getResolvedPattern() {
        final WordMeaning cruelWordMeaning = meaningOf("CRUel");

        Map.of(
                "penas {} mano", "penas <span class=\"main-word\">CRUel</span> mano", // as is
                "penas {crueles}", "penas <span class=\"main-word\">crueles</span>", // use word inside braces
                "first {|lowercase} second", "first <span class=\"main-word\">cruel</span> second"
        ).forEach((pattern, resolvedPattern) -> {
            final var entity = collocationOf(pattern, cruelWordMeaning);

            assertEquals(resolvedPattern, entity.getResolvedPattern());
        });
    }

    @Test
    void getResolvedPatternForMultiWord() {
        final WordMeaning cruelWordMeaning = meaningOf("¿Quien sabe?");

        final var entity = collocationOf("pero {quien sabe}", cruelWordMeaning);

        assertEquals("pero <span class=\"main-word\">quien sabe</span>", entity.getResolvedPattern());
    }
}
