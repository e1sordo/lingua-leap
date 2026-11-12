package es.e1sordo.lingualeap.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = WordToAddLater.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WordToAddLater {

    static final String TABLE_NAME = "words_to_add_later";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    @ToString.Exclude
    private LanguageCourse course;

    private String word;

    @Column(columnDefinition = "integer default 1")
    private int timesAdded;

    public WordToAddLater(final LanguageCourse course, final String word) {
        this.course = course;
        this.word = word;
        this.timesAdded = 0;
    }

    public void incrementTimesAdded() {
        this.timesAdded++;
    }
}
