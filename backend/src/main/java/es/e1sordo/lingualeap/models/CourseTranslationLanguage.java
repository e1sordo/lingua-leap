package es.e1sordo.lingualeap.models;

import jakarta.persistence.Column;
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
@Table(name = CourseTranslationLanguage.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "language_code"}))
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CourseTranslationLanguage {

    static final String TABLE_NAME = "course_translation_languages";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    @ToString.Exclude
    private LanguageCourse course;

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_code")
    private Language language;

    @Column(name = "display_order")
    private int displayOrder;
}
