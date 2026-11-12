package es.e1sordo.lingualeap.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = WordMeaningTranslation.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(columnNames = {"meaning_id", "language_code"}))
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WordMeaningTranslation {

    static final String TABLE_NAME = "meaning_translations";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "meaning_id")
    @ToString.Exclude
    private WordMeaning meaning;

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_code")
    private Language language;

    private String text;

    public WordMeaningTranslation(final WordMeaning meaning, final Language language, final String text) {
        this.meaning = meaning;
        this.language = language;
        this.text = text;
    }
}
