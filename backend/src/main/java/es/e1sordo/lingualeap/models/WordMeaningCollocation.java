package es.e1sordo.lingualeap.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.stream.Collectors;

@Entity
@Table(name = WordMeaningCollocation.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WordMeaningCollocation {

    static final String TABLE_NAME = "meaning_collocations";

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "word_meaning_id")
    private WordMeaning wordMeaning;

    private String pattern;

    private String translationRussian;
    private String translationEnglish;

    public String getResolvedPattern() {
        return Arrays.stream(pattern.split(" "))
                .map(part -> {
                    if (!part.startsWith("{") || !part.endsWith("}")) {
                        return part;
                    }

                    String text = wordMeaning.getWord().getWord();

                    String numberAndModification = part.substring(1, part.length() - 1);

                    final String[] numberAndModificationParts = numberAndModification.split("\\|");

                    if (numberAndModificationParts.length >= 2) {
                        String partOfMeaningWord = numberAndModificationParts[0]; // don't need now
                        String modification = numberAndModificationParts[1];
                        if ("lowercase".equals(modification)) {
                            text = text.toLowerCase();
                        } else if ("uppercase".equals(modification)) {
                            text = text.toUpperCase();
                        } else if ("capitalize".equals(modification)) {
                            text = text.substring(0, 1).toUpperCase() + text.substring(1);
                        }
                    }

                    return "<span class=\"main-word\">" + text + "</span>";
                })
                .collect(Collectors.joining(" "));
    }
}
