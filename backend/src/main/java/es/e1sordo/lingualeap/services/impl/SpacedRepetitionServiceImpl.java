package es.e1sordo.lingualeap.services.impl;

import es.e1sordo.lingualeap.enums.LearningStatus;
import es.e1sordo.lingualeap.models.ForeignWord;
import es.e1sordo.lingualeap.models.SM2WordMeaningMeta;
import es.e1sordo.lingualeap.models.WordMeaning;
import es.e1sordo.lingualeap.repositories.SM2WordMeaningMetaRepository;
import es.e1sordo.lingualeap.services.NotFoundException;
import es.e1sordo.lingualeap.services.SpacedRepetitionService;
import es.e1sordo.lingualeap.services.VocabularyListsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpacedRepetitionServiceImpl implements SpacedRepetitionService {

    private final SM2WordMeaningMetaRepository repository;
    private final VocabularyListsService vocabularyListsService;

    @Override
    public int getTodayCount(final Long courseId) {
        return repository.countByWordWordCourseIdAndPlannedReviewLessThanEqual(courseId, LocalDate.now());
    }

    @Override
    public List<WordMeaning> getTodayWords(final Long courseId) {
        final Supplier<Stream<SM2WordMeaningMeta>> allPlanedWordsStream = ()
                -> repository.findByWordWordCourseIdAndPlannedReviewLessThanEqual(courseId, LocalDate.now()).stream();

        var allPlanedWords = allPlanedWordsStream.get();

        final boolean hasNotNewlyCreatedWords = allPlanedWordsStream.get().anyMatch(meta -> meta.getTotalRepetitions() > 0);
        if (hasNotNewlyCreatedWords) {
            allPlanedWords = allPlanedWords.filter(meta -> meta.getTotalRepetitions() > 0);
        }

        return allPlanedWords.map(SM2WordMeaningMeta::getWord).toList();
    }

    @Override
    public Map<LocalDate, Integer> getSummaryGraph(final Long courseId) {
        final LocalDate today = LocalDate.now();
        final Map<LocalDate, Integer> summary = new HashMap<>();

        final var repositoryResult = repository.getPlannedWordsStatistics(today.plusYears(1), courseId);
        for (var result : repositoryResult) {
            if (result.date().isEqual(today)) {
                summary.put(today, getTodayCount(courseId));
            } else if (result.date().isAfter(today)) {
                summary.put(result.date(), result.count().intValue());
            }
        }

        return summary;
    }

    @Override
    @Transactional
    public void updateWordMeaningInfo(final Long courseId, final Long wordMeaningId, final int userGrade) {
        log.info("Looking for meta by wordMeaningId {}", wordMeaningId);
        final SM2WordMeaningMeta meta = repository.findByWordId(wordMeaningId)
                .orElseThrow(() -> new NotFoundException("WordMeaning not found: " + wordMeaningId));

        if (!Objects.equals(meta.getWord().getWord().getCourse().getId(), courseId)) {
            throw new NotFoundException("WordMeaning not found: " + wordMeaningId);
        }

        final ForeignWord word = meta.getWord().getWord();
        final String textWord = word.getWord();

        if (meta.getPlannedReview().isAfter(LocalDate.now())) {
            log.info("Word '{}' is scheduled for a later date and should not be scored now. {}", textWord, meta.textState());
            return;
        }

        final var previousGrade = meta.getPreviousUserGrade();
        log.info("Word '{}' was rated {} points at latest knowledge test", word.getWord(), userGrade);
        meta.updateWithNewGrade(userGrade);
        log.info("Updated state of word: Previous userGrade: {}, {}", previousGrade, meta.textState());
        updateLearningStatusOfWord(meta);

        if (userGrade <= 3 && previousGrade <= 3 || userGrade <= 2) {
            log.info("Word '{}' was rated less than 4 points two times in a row. Adding it to list of problem words", textWord);
            meta.getWord().addList(vocabularyListsService.getSmartListOfProblemWords(courseId));
        }

        if (word.getAdded().isBefore(LocalDate.now().minusDays(14)) && meta.getTotalRepetitions() >= 2) {
            log.info("Word '{}' was added more than 14 days ago. Removing it from list of recently added words", textWord);
            meta.getWord().removeList(vocabularyListsService.getSmartListOfRecentlyAdded(courseId));
        }

        repository.save(meta);
    }

    @Override
    public void addWordMeaning(final WordMeaning meaning) {
        final SM2WordMeaningMeta meta = SM2WordMeaningMeta.createDefault(meaning);
        repository.save(meta);
        log.info("New word '{}' was added to SpacedRepetition", meaning.getWord().getWord());
    }

    @Override
    public void deleteWordMeaning(final Long wordMeaningId) {
        final SM2WordMeaningMeta meta = repository.findByWordId(wordMeaningId).get();
        repository.delete(meta);
        log.info("Word '{}' was removed from SpacedRepetition", meta.getWord().getWord());
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
