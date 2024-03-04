package es.e1sordo.lingualeap.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    private String word;

    @Column(columnDefinition = "integer default 1")
    private int timesAdded;

    public void incrementTimesAdded() {
        this.timesAdded++;
    }
}
