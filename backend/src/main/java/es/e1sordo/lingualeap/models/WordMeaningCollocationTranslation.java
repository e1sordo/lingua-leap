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
@Table(name = WordMeaningCollocationTranslation.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(columnNames = {"collocation_id", "language_code"}))
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WordMeaningCollocationTranslation {

    static final String TABLE_NAME = "collocation_translations";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "collocation_id")
    @ToString.Exclude
    private WordMeaningCollocation collocation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_code")
    private Language language;

    private String text;

    public WordMeaningCollocationTranslation(final WordMeaningCollocation collocation,
                                             final Language language,
                                             final String text) {
        this.collocation = collocation;
        this.language = language;
        this.text = text;
    }
}
