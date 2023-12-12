package es.e1sordo.lingualeap.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = ForeignWord.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ForeignWord {

    static final String TABLE_NAME = "words";

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String word;

    private LocalDate added;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "word", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<WordMeaning> meanings = new ArrayList<>();

    public void addMeaning(WordMeaning meaning) {
        this.meanings.add(meaning);
    }
}
