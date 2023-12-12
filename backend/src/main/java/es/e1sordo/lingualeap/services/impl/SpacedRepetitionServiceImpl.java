package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.models.SM2WordMeaningMeta;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.repositories.SM2WordMeaningMetaRepository;
import es.e1sordo.lingualeap.services.SpacedRepetitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpacedRepetitionServiceImpl implements SpacedRepetitionService {

    private final SM2WordMeaningMetaRepository repository;

    @Override
    public int getTodayCount() {
        return repository.countByPlannedReviewLessThanEqual(LocalDate.now());
    }

    @Override
    public List<WordMeaning> getTodayWords() {
        return repository.findByPlannedReviewLessThanEqual(LocalDate.now()).stream()
                .map(SM2WordMeaningMeta::getWord)
                .toList();
    }

    @Override
    public void updateWordMeaningInfo(final Long wordMeaningId, final int userGrade) {
        log.info("Looking for meta by wordMeaningId {}", wordMeaningId);
        final SM2WordMeaningMeta meta = repository.findByWordId(wordMeaningId).get();

        if (meta.getPlannedReview().isAfter(LocalDate.now())) {
            log.info("Word '{}' is scheduled for a later date and should not be scored now. {}", meta.getWord().getWord().getWord(), meta.textState());
            return;
        }

        final var previousGrade = meta.getPreviousUserGrade();
        log.info("Word '{}' was rated {} points at latest knowledge test", meta.getWord().getWord().getWord(), userGrade);
        meta.updateWithNewGrade(userGrade);
        log.info("Updated state of word: Previous userGrade: {}, {}", previousGrade, meta.textState());
        updateLearningStatusOfWord(meta);

        // todo если слово дважды подряд приходит с score = 0 или 1, то отправлять его в список СЛОЖНЫЕ СЛОВА

        repository.save(meta);
    }

    @Override
    public void addWordMeaning(final WordMeaning meaning) {
        final SM2WordMeaningMeta meta = SM2WordMeaningMeta.createDefault(meaning);
        repository.save(meta);
        log.info("New word '{}' was added to SpacedRepetition", meaning.getWord().getWord());
    }

    private void updateLearningStatusOfWord(final SM2WordMeaningMeta meta) {
        final int repetitionNumber = meta.getRepetitionNumber();

        LearningStatus learningStatus;
        if (repetitionNumber == 0) {
            learningStatus = LearningStatus.NEW;
        } else if (repetitionNumber <= 4) {
            learningStatus = LearningStatus.TO_STUDY;
        } else {
            learningStatus = LearningStatus.KNOWN;
        }

        meta.getWord().setLearningStatus(learningStatus);
    }
}
