package es.e1sordo.lingualeap.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = Language.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Language {

    static final String TABLE_NAME = "languages";

    @Id
    private String code;

    private String defaultName;

    private String defaultSpeechLocale;
}
