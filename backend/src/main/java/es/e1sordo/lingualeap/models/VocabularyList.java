package es.e1sordo.lingualeap.models;

import es.e1sordo.lingualeap.enums.SmartListType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = VocabularyList.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyList {

    static final String TABLE_NAME = "vocabulary_lists";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private boolean smart;

    @Enumerated(value = EnumType.STRING)
    private SmartListType smartType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    @ToString.Exclude
    private LanguageCourse course;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "listsContaining")
    @ToString.Exclude
    private Set<WordMeaning> meanings = new HashSet<>();

    private LocalDate added;

    public void addMeaning(WordMeaning meaning) {
        this.meanings.add(meaning);
    }
}
