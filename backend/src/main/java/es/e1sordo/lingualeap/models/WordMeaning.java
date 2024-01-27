package es.e1sordo.lingualeap.models;

import es.e1sordo.lingualeap.enums.GrammaticalGender;
import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.enums.PartOfSpeech;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = WordMeaning.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WordMeaning {

    static final String TABLE_NAME = "meanings";

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "word_id")
    private ForeignWord word;

    @Enumerated(value = EnumType.STRING)
    private PartOfSpeech pos;

    @Enumerated(value = EnumType.STRING)
    private GrammaticalGender gender;

    private String imageUrl;
    private String russianVariant;
    private String englishVariant;
    private String definition;
    private Integer frequency;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "wordMeaning")
    @ToString.Exclude
    private List<WordMeaningCollocation> collocations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "wordMeaning")
    @ToString.Exclude
    private List<WordMeaningContext> contexts = new ArrayList<>();

    @Enumerated(value = EnumType.ORDINAL)
    private LearningStatus learningStatus;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "meaning_to_lists",
            joinColumns = @JoinColumn(name = "meaning_id"),
            inverseJoinColumns = @JoinColumn(name = "list_id")
    )
    @ToString.Exclude
    private Set<VocabularyList> listsContaining = new HashSet<>();

    public void addCollocation(WordMeaningCollocation collocation) {
        this.collocations.add(collocation);
    }

    public void addContext(WordMeaningContext context) {
        this.contexts.add(context);
    }

    public void addList(VocabularyList list) {
        this.listsContaining.add(list);
    }

    public void removeList(VocabularyList list) {
        this.listsContaining.remove(list);
    }
}
