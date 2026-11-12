package es.e1sordo.lingualeap.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "word_meaning_id")
    private WordMeaning wordMeaning;

    private String pattern;

    @OneToMany(mappedBy = "collocation", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<WordMeaningCollocationTranslation> translations = new ArrayList<>();

    public String getTranslation(final String languageCode) {
        return translations.stream()
                .filter(translation -> translation.getLanguage().getCode().equals(languageCode))
                .map(WordMeaningCollocationTranslation::getText)
                .findFirst()
                .orElse(null);
    }

    public void putTranslation(final Language language, final String text) {
        final var existing = translations.stream()
                .filter(translation -> translation.getLanguage().getCode().equals(language.getCode()))
                .findFirst();
        if (existing.isPresent()) {
            existing.get().setText(text);
        } else {
            translations.add(new WordMeaningCollocationTranslation(this, language, text));
        }
    }

    public boolean removeTranslation(final String languageCode) {
        return translations.removeIf(translation -> translation.getLanguage().getCode().equals(languageCode));
    }

    public String getResolvedPattern(boolean withHtml) {
        return Arrays.stream(splitString(pattern))
                .map(part -> {
                    if (!part.startsWith("{") || !part.endsWith("}")) {
                        return part;
                    }

                    String text = wordMeaning.getWord().getWord();
                    String rules = part.substring(1, part.length() - 1);
                    final String[] rulesParts = rules.split("\\|");

                    if (rulesParts.length == 1 && hasText(rulesParts[0])) {
                        text = rulesParts[0];
                    } else if (rulesParts.length >= 2) {
                        String modification = rulesParts[1];
                        if ("lowercase".equals(modification)) {
                            text = text.toLowerCase();
                        } else if ("uppercase".equals(modification)) {
                            text = text.toUpperCase();
                        } else if ("capitalize".equals(modification)) {
                            text = text.substring(0, 1).toUpperCase() + text.substring(1);
                        }
                    }

                    return withHtml ? "<span class=\"main-word\">" + text + "</span>" : text;
                })
                .collect(Collectors.joining(" "));
    }

    public String getResolvedPattern() {
        return getResolvedPattern(true);
    }

    private String[] splitString(String input) {
        // Регулярное выражение для разделения строки по пробелу,
        // но игнорирование пробелов внутри фигурных скобок
        String regex = " (?![^\\{]*\\})";

        return input.split(regex);
    }
}
