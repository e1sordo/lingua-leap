package es.e1sordo.lingualeap.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@Entity
@Table(name = SM2WordMeaningMeta.TABLE_NAME)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SM2WordMeaningMeta {

    static final String TABLE_NAME = "sm2_meta";

    public static SM2WordMeaningMeta createDefault(WordMeaning word) {
        return new SM2WordMeaningMeta(
                null,
                word,
                0,
                0,
                0,
                2.5,
                LocalDate.now(),
                LocalDate.now()
        );
    }

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "word_id")
    private WordMeaning word;

    private int previousUserGrade;
    private int repetitionNumber; // only success
    private int totalRepetitions; // total
    private double easinessFactor;
    private LocalDate previousReview;
    private LocalDate plannedReview;

    public int getInterRepetitionInterval() {
        return (int) DAYS.between(previousReview, plannedReview);
    }

    private void updatePlannedReview(int interval) {
        previousReview = LocalDate.now();
        plannedReview = previousReview.plusDays(interval);
    }

    public void updateWithNewGrade(int userGrade) {
        if (userGrade < 0 || userGrade > 5) {
            throw new IllegalArgumentException("userGrade must be in range of 0 to 5");
        }

        totalRepetitions++;

        // SM2 algorithm: https://en.wikipedia.org/wiki/SuperMemo
        if (userGrade >= 3) { // i.e correct response
            updatePlannedReview(switch (repetitionNumber) {
                case 0 -> 1;
                case 1 -> 6;
                default -> (int) Math.ceil(getInterRepetitionInterval() * easinessFactor);
            });
            ++repetitionNumber;
        } else { // incorrect response
            repetitionNumber = 0;
            updatePlannedReview(1);
        }

        final var newEasinessFactor = easinessFactor + (0.1 - (5 - userGrade) * (0.08 + (5 - userGrade) * 0.02));
        easinessFactor = Math.max(1.3, newEasinessFactor);

        previousUserGrade = userGrade;
    }

    public String textState() {
        return "userGrade: %d, repetitions: %d (total: %d), EF: %f, nextReview: %s, ID: %d".formatted(
                previousUserGrade,
                repetitionNumber,
                totalRepetitions,
                easinessFactor,
                plannedReview,
                id
        );
    }
}
